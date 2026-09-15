package com.stockmaster.notification.service;

import com.stockmaster.notification.port.CanalNotification;
import com.stockmaster.shared.repository.UtilisateurRepository;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * US-021 (infrastructure) — première implémentation du port
 * {@link CanalNotification} (DEC-014) : canal EMAIL via
 * {@code spring-boot-starter-mail} (déjà en dépendance shared).
 *
 * <p>Choix structurants :</p>
 * <ul>
 *   <li><b>SMTP optionnel</b> : {@link ObjectProvider}&lt;JavaMailSender&gt; —
 *       sans {@code spring.mail.host} configuré (tests, environnement sans SMTP),
 *       le service est un no-op journalisé au lieu de casser le contexte. La
 *       health-mail est déjà désactivée dans tous les profils pour la même raison ;</li>
 *   <li><b>asynchrone</b> : même philosophie que US-006 — l'envoi ne bloque ni ne
 *       fait échouer la transaction métier de l'appelant. Un exécuteur dédié
 *       (2 threads, queue 200) évite le pool commun ;</li>
 *   <li><b>échec avalé</b> : une erreur SMTP est journalisée mais jamais propagée —
 *       le compte métier existe même si l'email échoue (critère US-006 repris
 *       par US-021) ; le retry systématique est une US EPIC 12 (US-074) ;</li>
 *   <li><b>adresse résolue</b> : le port ne prend que {@code destinataireId} ;
 *       l'adresse est résolue ici — aucune adresse n'est acceptée en clair
 *       (surface d'usurpation d'identité évitée).</li>
 * </ul>
 *
 * <p>Sert US-021/US-022 (invitations) dès maintenant, puis US-074/US-075
 * (bienvenue/invitation employé, EPIC 12) sans nouvelle infrastructure.</p>
 */
@Service
@Slf4j
public class EmailNotificationService implements CanalNotification {

    public static final String CANAL_EMAIL = "EMAIL";

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final UtilisateurRepository utilisateurRepository;
    private final ThreadPoolTaskExecutor mailExecutor;

    public EmailNotificationService(ObjectProvider<JavaMailSender> mailSenderProvider,
                                    UtilisateurRepository utilisateurRepository) {
        this.mailSenderProvider = mailSenderProvider;
        this.utilisateurRepository = utilisateurRepository;
        this.mailExecutor = new ThreadPoolTaskExecutor();
        this.mailExecutor.setCorePoolSize(2);
        this.mailExecutor.setMaxPoolSize(2);
        this.mailExecutor.setQueueCapacity(200);
        this.mailExecutor.setThreadNamePrefix("mail-notif-");
        this.mailExecutor.initialize();
    }

    @PostConstruct
    void logConfigurationState() {
        if (mailSenderProvider.getIfAvailable() == null) {
            log.info("EmailNotificationService : spring.mail.host non configuré — le canal EMAIL est en no-op.");
        }
    }

    @Override
    public void envoyer(Long entrepriseId, Long destinataireId, String typeAlerte, String sujet, String corps) {
        if (destinataireId == null) {
            log.warn("EMAIL abandonné : destinataireId null (type={}, entreprise={})", typeAlerte, entrepriseId);
            return;
        }
        utilisateurRepository.findById(destinataireId)
                .filter(u -> !Boolean.TRUE.equals(u.getSupprime()))
                .ifPresentOrElse(
                        destinataire -> {
                            String email = destinataire.getEmail();
                            mailExecutor.execute(() -> envoyerSync(email, sujet, corps));
                        },
                        () -> log.warn("EMAIL abandonné : destinataire {} inconnu ou supprimé (type={})",
                                destinataireId, typeAlerte));
    }

    /** Envoi effectif — avalé en cas d'échec SMTP (l'appelant métier ne doit jamais être interrompu). */
    private void envoyerSync(String email, String sujet, String corps) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.info("EMAIL no-op (SMTP non configuré) : to={}, sujet={}", email, sujet);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("no-reply@stockmaster.cm");
            message.setTo(email);
            message.setSubject(sujet);
            message.setText(corps);
            mailSender.send(message);
            log.debug("EMAIL envoyé à {} : {}", email, sujet);
        } catch (RuntimeException e) {
            log.error("Échec d'envoi EMAIL à {} (sujet={}) : {}", email, sujet, e.getMessage());
        }
    }

    @Override
    public String nom() {
        return CANAL_EMAIL;
    }

    @PreDestroy
    void shutdown() {
        mailExecutor.shutdown();
    }
}
