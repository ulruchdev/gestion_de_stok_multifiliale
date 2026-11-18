package com.stockmaster.reporting.architecture;

import com.stockmaster.vente.repository.LigneVenteRepository;

/**
 * Sonde de test — GS-1251.
 *
 * <p>Cette classe vit volontairement dans {@code com.stockmaster.reporting} et
 * dépend de {@code vente.repository} : elle n'est jamais chargée par
 * l'application, elle sert uniquement à prouver que la règle ArchUnit
 * {@code reportingReadsVenteThroughPortOnly} détecte bien une fuite
 * d'encapsulation (test négatif).</p>
 */
public final class VenteRepositoryLeakProbe {

    /** Dépendance interdite, matérialisée pour l'analyse ArchUnit. */
    public Class<?> dependanceInterdite() {
        return LigneVenteRepository.class;
    }
}
