package com.stockmaster.notification.service;

/**
 * Port d'envoi de notifications — {@code DEC-014} : le module notification est
 * P0 et abstrait ses canaux derrière cette interface pour rester extensible
 * (in-app et email en V1 ; SMS Mobile Money plus tard, sans toucher aux émetteurs).
 *
 * <p>Implémentations prévues (V1) :
 * <ul>
 *   <li>{@code NotificationInterneService} — persiste dans notification_alerte ;</li>
 *   <li>{@code EmailNotificationService} — adapte spring-boot-starter-mail
 *       (déjà en dépendance shared) ; consomme notamment l'événement
 *       {@code InscriptionSuccessEvent} du module auth (US-006/007 : email de
 *       bienvenue + vérification email, DEC-016/AUTH-07).</li>
 * </ul>
 * Les émetteurs (stock, vente…) dépendent de cette interface, jamais de
 * l'implémentation — inverser la dépendance est tout l'objet de DEC-014.</p>
 */
public interface CanalNotification {

    /**
     * Envoie une notification sur le canal.
     *
     * @param destinataireId   destinataire précis, ou NULL pour toute l'entreprise
     * @param entrepriseId     périmètre de la notification
     * @param typeAlerte       convention de type extensible (STOCK_BAS, SECURITY_ALERT…)
     * @param sujet            sujet lisible (email) / titre (in-app)
     * @param corps            message complet
     */
    void envoyer(Long entrepriseId, Long destinataireId, String typeAlerte, String sujet, String corps);

    /** Identifiant du canal (IN_APP, EMAIL…) — journalisation et désactivation ciblée. */
    String nom();
}
