package com.stockmaster.auth.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Publié quand un refresh token déjà invalidé (rotation antérieure) est présenté à nouveau —
 * signe probable de vol/interception. Toute la famille de tokens de l'utilisateur est révoquée.
 *
 * <p>Aucun listener aujourd'hui (module {@code stockmaster-notification} pas encore implémenté) —
 * même statut que {@link InscriptionSuccessEvent} : point d'ancrage pour l'envoi futur d'une
 * alerte de sécurité (email + in-app).</p>
 */
@Getter
public class RefreshTokenReuseDetectedEvent extends ApplicationEvent {

    private final Long userId;
    private final String ip;

    public RefreshTokenReuseDetectedEvent(Object source, Long userId, String ip) {
        super(source);
        this.userId = userId;
        this.ip = ip;
    }
}
