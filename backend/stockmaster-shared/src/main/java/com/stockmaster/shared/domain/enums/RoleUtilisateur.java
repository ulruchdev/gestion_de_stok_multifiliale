package com.stockmaster.shared.domain.enums;

/**
 * Rôles métier des utilisateurs — 5 acteurs de GS-CDA-2026-01 §2.
 * Le RBAC frontend est le reflet strict de cette énumération, jamais la source de vérité.
 */
public enum RoleUtilisateur {
    SUPER_ADMIN,
    ADMIN_GROUPE,
    ADMIN_FILIALE,
    GESTIONNAIRE_STOCK,
    RESP_ACHATS,
    COMMERCIAL,
    CAISSIER
}
