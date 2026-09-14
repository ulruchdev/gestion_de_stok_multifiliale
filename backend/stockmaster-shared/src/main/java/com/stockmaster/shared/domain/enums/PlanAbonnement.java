package com.stockmaster.shared.domain.enums;

/**
 * Plans d'abonnement — {@code DEC-015} (référentiel GS-REF-2026-01).
 *
 * <p>Grille à trois valeurs : {@code STARTER} est supprimé et {@code ENTERPRISE}
 * devient {@code PERSONNALISE}. Les limites (4/15 filiales, 10/50 utilisateurs)
 * sont portées par {@code tenant_group.limite_filiales} / {@code limite_utilisateurs},
 * jamais codées en dur — c'est la faute que DEC-015 reprochait à l'existant.</p>
 */
public enum PlanAbonnement {
    GRATUIT,
    PRO,
    PERSONNALISE
}
