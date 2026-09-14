package com.stockmaster.shared.domain.enums;

/**
 * Périmètre d'action de l'utilisateur : GROUPE (lecture consolidée, Admin Groupe)
 * ou FILIALE (données transactionnelles de sa filiale).
 */
public enum ScopeUtilisateur {
    GROUPE,
    FILIALE
}
