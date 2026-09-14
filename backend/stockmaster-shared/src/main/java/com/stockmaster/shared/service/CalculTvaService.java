package com.stockmaster.shared.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calcul de TVA et du TTC — {@code DEC-003} : tous les montants sont des
 * entiers XAF ; le TTC est calculé {@code HT × (1 + TVA/100)} avec arrondi
 * HALF_UP (jamais saisi à la main, cf. commentaire de colonne V1).
 *
 * <p>Contient aussi les conversions d'unités achat ↔ gestion ({@code DEC-013}).
 * Service volontairement pur (aucune dépendance) : testable TDD sans contexte Spring.</p>
 */
@Service
public class CalculTvaService {

    private static final BigDecimal CENT = BigDecimal.valueOf(100);
    private static final int SCALE_QUANTITE = 3;

    /**
     * Calcule le prix TTC XAF : {@code HT × (1 + TVA/100)}, arrondi HALF_UP à l'unité.
     * Les prix HT, TVA et TTC sont des entiers XAF — jamais de centimes.
     */
    public Integer calculerTtc(Integer prixVenteHt, BigDecimal tauxTva) {
        if (prixVenteHt == null || tauxTva == null) {
            throw new IllegalArgumentException("prixVenteHt et tauxTva sont obligatoires");
        }
        if (prixVenteHt < 0) {
            throw new IllegalArgumentException("prixVenteHt doit être >= 0");
        }
        if (tauxTva.signum() < 0) {
            throw new IllegalArgumentException("tauxTva doit être >= 0");
        }
        BigDecimal ht = BigDecimal.valueOf(prixVenteHt);
        BigDecimal ttc = ht.multiply(BigDecimal.ONE.add(tauxTva.divide(CENT, 6, RoundingMode.HALF_UP)));
        return ttc.setScale(0, RoundingMode.HALF_UP).intValueExact();
    }

    /** Convertit une quantité exprimée en unité d'ACHAT vers l'unité de GESTION. */
    public BigDecimal achatVersGestion(BigDecimal quantiteAchat, BigDecimal facteurConversion) {
        return convertir(quantiteAchat, facteurConversion, "facteurConversion");
    }

    /**
     * Convertit une quantité exprimée en unité de GESTION vers l'unité d'ACHAT
     * (division — arrondie HALF_UP à 3 décimales, cohérent DECIMAL(12,3)).
     */
    public BigDecimal gestionVersAchat(BigDecimal quantiteGestion, BigDecimal facteurConversion) {
        if (quantiteGestion == null || facteurConversion == null) {
            throw new IllegalArgumentException("quantite et facteurConversion sont obligatoires");
        }
        if (quantiteGestion.signum() < 0) {
            throw new IllegalArgumentException("quantite doit être >= 0");
        }
        if (facteurConversion.signum() <= 0) {
            throw new IllegalArgumentException("facteurConversion doit être > 0");
        }
        return quantiteGestion
                .divide(facteurConversion, SCALE_QUANTITE, RoundingMode.HALF_UP);
    }

    private BigDecimal convertir(BigDecimal quantite, BigDecimal facteurConversion, String nomFacteur) {
        if (quantite == null || facteurConversion == null) {
            throw new IllegalArgumentException("quantite et facteurConversion sont obligatoires");
        }
        if (quantite.signum() < 0) {
            throw new IllegalArgumentException("quantite doit être >= 0");
        }
        if (facteurConversion.signum() <= 0) {
            throw new IllegalArgumentException(nomFacteur + " doit être > 0");
        }
        return quantite.multiply(facteurConversion).setScale(SCALE_QUANTITE, RoundingMode.HALF_UP);
    }
}
