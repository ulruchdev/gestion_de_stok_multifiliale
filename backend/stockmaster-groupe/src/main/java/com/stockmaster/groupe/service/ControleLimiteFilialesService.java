package com.stockmaster.groupe.service;

import org.springframework.stereotype.Service;

/**
 * Contrôle de la limite de filiales — {@code DEC-015}.
 *
 * <p>La limite est lue de {@code tenant_group.limite_filiales} (donnée du plan),
 * JAMAIS codée en dur — c'est précisément la faute que DEC-015 reprochait à
 * l'existant (grille de plans incohérente entre doc, code et diagrammes).</p>
 *
 * <p>{@code DEC-015} : seuls les sites {@code site_operationnel = TRUE}
 * comptent dans la limite — la maison mère "pur siège" n'en fait pas partie.</p>
 */
@Service
public class ControleLimiteFilialesService {

    /** True si la création d'une filiale supplémentaire est permise par le plan. */
    public boolean peutAjouterFiliale(long filialesOperationnellesActuelles, Integer limiteFiliales) {
        if (limiteFiliales == null || limiteFiliales <= 0) {
            return false; // plan sans filiale autorisée ou limite absente
        }
        return filialesOperationnellesActuelles < limiteFiliales;
    }

    /**
     * Nombre de sites comptant dans la limite : les entreprises actives,
     * non supprimées et {@code site_operationnel = TRUE} (DEC-015).
     */
    public long comptabilise(long entreprisesActivesNonSupprimees, long sitesNonOperationnels) {
        return Math.max(0, entreprisesActivesNonSupprimees - sitesNonOperationnels);
    }
}
