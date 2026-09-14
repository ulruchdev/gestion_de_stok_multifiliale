package com.stockmaster.reporting.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Chiffre d'affaires — {@code DEC-020} (V5 §5/§10) : le reporting distingue
 * STRICTEMENT le CA HT <em>facturé</em> (commandes et ventes émises) du CA
 * <em>encaissé</em> (paiements reçus / {@code date_reglement} posée).
 *
 * <p>Service volontairement pur (record d'entrée, aucune dépendance module) :
 * les services de lecture (à implémenter) mapperont {@code ligne_vente} et
 * {@code ligne_commande_client} vers {@link LigneReport}.</p>
 */
@Service
public class CalculCaService {

    /** Ligne de reporting minimale : quantité × prix HT + taux de TVA snapshot. */
    public record LigneReport(Long articleId, java.math.BigDecimal quantite,
                              Integer prixUnitaireHt, java.math.BigDecimal tauxTva) {

        public LigneReport {
            if (quantite == null || quantite.signum() <= 0) {
                throw new IllegalArgumentException("quantite doit être > 0");
            }
            if (prixUnitaireHt == null || prixUnitaireHt < 0) {
                throw new IllegalArgumentException("prixUnitaireHt doit être >= 0");
            }
            if (tauxTva == null || tauxTva.signum() < 0) {
                throw new IllegalArgumentException("tauxTva doit être >= 0");
            }
        }

        /** Montant HT XAF de la ligne (entier, HALF_UP). */
        public int montantHt() {
            return quantite.multiply(BigDecimal.valueOf(prixUnitaireHt))
                    .setScale(0, RoundingMode.HALF_UP)
                    .intValueExact();
        }

        /** Part de TVA XAF de la ligne (entier, HALF_UP). */
        public int montantTva() {
            return BigDecimal.valueOf(montantHt())
                    .multiply(tauxTva.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP))
                    .setScale(0, RoundingMode.HALF_UP)
                    .intValueExact();
        }
    }

    /** CA HT facturé : somme des montants HT des lignes. */
    public long caHtFacture(List<LigneReport> lignes) {
        return lignes == null ? 0L : lignes.stream().mapToLong(LigneReport::montantHt).sum();
    }

    /** TVA totale facturée : somme des parts de TVA. */
    public long tvaFacturee(List<LigneReport> lignes) {
        return lignes == null ? 0L : lignes.stream().mapToLong(LigneReport::montantTva).sum();
    }

    /** CA TTC facturé = HT + TVA (XAF entiers, DEC-003). */
    public long caTtcFacture(List<LigneReport> lignes) {
        return caHtFacture(lignes) + tvaFacturee(lignes);
    }
}
