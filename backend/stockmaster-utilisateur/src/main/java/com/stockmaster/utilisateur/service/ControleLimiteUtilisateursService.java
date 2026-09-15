package com.stockmaster.utilisateur.service;

import org.springframework.stereotype.Service;

/**
 * Contrôle de la limite d'utilisateurs — {@code DEC-015} (V5 §1 :
 * {@code limite_utilisateurs} = 10 GRATUIT / 50 PRO / négocié PERSONNALISE).
 * La valeur est lue du {@code tenant_group}, jamais codée en dur.
 *
 * <p>US-101 : la limite <b>effective</b> tient compte de l'expiration du plan —
 * un {@code date_expiration_plan} dépassé est traité comme GRATUIT (10) pour le
 * calcul, même si {@code limite_utilisateurs} porte encore une valeur supérieure.</p>
 */
@Service
public class ControleLimiteUtilisateursService {

    /** Limite du plan GRATUIT (DEC-015) — seul plafond codé en dur, car c'est le plan par défaut. */
    public static final int LIMITE_GRATUIT = 10;

    /** True si la création d'un utilisateur supplémentaire est permise par le plan. */
    public boolean peutAjouterUtilisateur(long utilisateursActifsActuels, Integer limiteUtilisateurs) {
        if (limiteUtilisateurs == null || limiteUtilisateurs <= 0) {
            return false;
        }
        return utilisateursActifsActuels < limiteUtilisateurs;
    }

    /**
     * US-101 — limite effective : la valeur stockée si le plan est actif, la
     * limite GRATUIT (10) si le plan est expiré — lecture littérale de
     * DEC-015/US-101 (« un plan expiré est traité comme GRATUIT pour le calcul
     * de la limite ») : le compte repasse sur la grille du plan gratuit.
     *
     * @param limiteStockee  {@code tenant_group.limite_utilisateurs} (peut être null)
     * @param planExpire     true si {@code date_expiration_plan} est dépassée
     * @return la limite à comparer au décompte (null = absente, refus fail-safe en aval)
     */
    public Integer limiteUtilisateursEffective(Integer limiteStockee, boolean planExpire) {
        if (planExpire) {
            return LIMITE_GRATUIT;
        }
        return limiteStockee;
    }
}
