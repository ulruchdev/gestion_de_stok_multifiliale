package com.stockmaster.shared.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Suit la durée d'indisponibilité continue de Redis, tous appelants confondus (US-085), pour
 * émettre une seule alerte technique (log {@code ERROR}) après 60s de panne — plutôt qu'une
 * alerte dupliquée par composant appelant (rate limiter, filtre JWT, service auth).
 *
 * <p>Chaque appelant doit signaler aussi bien ses échecs ({@link #recordFailure()}) que ses
 * succès ({@link #recordSuccess()}) — sans quoi le tracker ne peut pas distinguer "Redis est
 * rétabli" de "aucune requête n'a sollicité Redis récemment".</p>
 *
 * <p>Aucun canal d'alerte externe n'existe aujourd'hui (pas de Sentry, pas de module
 * notification) — ce log est le point d'ancrage pour un futur outil de monitoring.</p>
 */
@Slf4j
@Component
public class RedisHealthTracker {

    static final long ALERT_THRESHOLD_SECONDS = 60;

    private final Clock clock;
    private volatile Instant firstFailure;
    private final AtomicBoolean alertLogged = new AtomicBoolean(false);

    public RedisHealthTracker() {
        this(Clock.systemUTC());
    }

    RedisHealthTracker(Clock clock) {
        this.clock = clock;
    }

    /** À appeler par tout composant qui vient de rencontrer une erreur de connexion Redis. */
    public synchronized void recordFailure() {
        Instant now = Instant.now(clock);
        if (firstFailure == null) {
            firstFailure = now;
        }

        long downForSeconds = Duration.between(firstFailure, now).toSeconds();
        if (downForSeconds >= ALERT_THRESHOLD_SECONDS && alertLogged.compareAndSet(false, true)) {
            log.error("event=redis_unavailable_prolonged downForSeconds={} — Redis injoignable en continu depuis plus de {}s (aucun canal d'alerte configuré, log uniquement)",
                    downForSeconds, ALERT_THRESHOLD_SECONDS);
        }
    }

    /** À appeler par tout composant dont l'appel Redis vient de réussir — remet le compteur à zéro. */
    public synchronized void recordSuccess() {
        firstFailure = null;
        alertLogged.set(false);
    }
}
