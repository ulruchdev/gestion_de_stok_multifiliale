package com.stockmaster.shared.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RedisHealthTracker — Tests unitaires (US-085)")
class RedisHealthTrackerTest {

    private ListAppender<ILoggingEvent> appender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(RedisHealthTracker.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
    }

    private List<ILoggingEvent> errorLogs() {
        return appender.list.stream().filter(e -> e.getLevel() == Level.ERROR).toList();
    }

    @Test
    @DisplayName("✅ N'émet aucune alerte tant que la panne dure moins de 60s")
    void shouldNotAlertBeforeThreshold() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        RedisHealthTracker tracker = new RedisHealthTracker(clock);

        tracker.recordFailure();
        clock.advanceSeconds(30);
        tracker.recordFailure();
        clock.advanceSeconds(29);
        tracker.recordFailure();

        assertThat(errorLogs()).isEmpty();
    }

    @Test
    @DisplayName("✅ Alerte une seule fois après 60s de panne continue, même si recordFailure() est rappelé ensuite")
    void shouldAlertOnceAfterThreshold() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        RedisHealthTracker tracker = new RedisHealthTracker(clock);

        tracker.recordFailure();
        clock.advanceSeconds(60);
        tracker.recordFailure(); // franchit le seuil de 60s → doit logger une fois
        clock.advanceSeconds(5);
        tracker.recordFailure(); // ne doit pas re-logger
        clock.advanceSeconds(5);
        tracker.recordFailure(); // ne doit pas re-logger

        assertThat(errorLogs()).hasSize(1);
        assertThat(errorLogs().get(0).getFormattedMessage()).contains("redis_unavailable_prolonged");
    }

    @Test
    @DisplayName("✅ recordSuccess() remet le compteur à zéro — la panne suivante repart de zéro")
    void shouldResetOnSuccess() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        RedisHealthTracker tracker = new RedisHealthTracker(clock);

        tracker.recordFailure();
        clock.advanceSeconds(50);
        tracker.recordSuccess(); // Redis rétabli avant le seuil de 60s
        clock.advanceSeconds(50);
        tracker.recordFailure(); // nouvelle panne qui vient de commencer, pas 100s de panne continue

        assertThat(errorLogs()).isEmpty();
    }

    @Test
    @DisplayName("✅ Alerte de nouveau si une deuxième panne distincte (après recordSuccess) dépasse aussi 60s")
    void shouldAlertAgainOnSeparateOutage() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        RedisHealthTracker tracker = new RedisHealthTracker(clock);

        tracker.recordFailure();
        clock.advanceSeconds(65);
        tracker.recordFailure(); // 1ère panne : alerte

        tracker.recordSuccess(); // Redis rétabli
        tracker.recordFailure(); // 2ème panne, nouvelle
        clock.advanceSeconds(65);
        tracker.recordFailure(); // 2ème panne dépasse aussi 60s : nouvelle alerte

        assertThat(errorLogs()).hasSize(2);
    }

    /** Horloge de test dont on avance manuellement le temps. */
    private static final class MutableClock extends Clock {
        private Instant instant;

        MutableClock(Instant instant) {
            this.instant = instant;
        }

        void advanceSeconds(long seconds) {
            instant = instant.plusSeconds(seconds);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
