package com.stockmaster.reporting.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * US-F092 / GS-1255 — réponse de {@code GET /api/v1/reporting/filiales}.
 *
 * <p>Une ligne par entreprise du groupe du JWT, <b>y compris celles sans
 * vente</b> (0 explicite plutôt qu'une omission silencieuse). Le périmètre
 * n'est jamais fourni par le client : il est déduit du JWT.</p>
 */
@Getter
@Builder
@AllArgsConstructor
public class ComparaisonFilialesResponse {

    private List<LigneFiliale> filiales;

    /**
     * Indicateurs d'un site sur la période.
     *
     * @param entrepriseId identifiant du site (entreprise du groupe)
     * @param nom          nom du site
     * @param caHt         CA HT facturé (XAF entier, DEC-020)
     * @param nbVentes     nombre de ventes distinctes (une vente peut avoir plusieurs lignes)
     * @param panierMoyen  CA TTC / nbVentes, arrondi HALF_UP (0 si aucune vente)
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class LigneFiliale {
        private Long entrepriseId;
        private String nom;
        private long caHt;
        private long nbVentes;
        private long panierMoyen;
    }
}
