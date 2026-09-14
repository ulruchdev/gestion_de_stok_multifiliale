package com.stockmaster.utilisateur.service;

import org.springframework.stereotype.Service;

/**
 * Contrôle de la limite d'utilisateurs — {@code DEC-015} (V5 §1 :
 * {@code limite_utilisateurs} = 10 GRATUIT / 50 PRO / négocié PERSONNALISE).
 * La valeur est lue du {@code tenant_group}, jamais codée en dur.
 */
@Service
public class ControleLimiteUtilisateursService {

    /** True si la création d'un utilisateur supplémentaire est permise par le plan. */
    public boolean peutAjouterUtilisateur(long utilisateursActifsActuels, Integer limiteUtilisateurs) {
        if (limiteUtilisateurs == null || limiteUtilisateurs <= 0) {
            return false;
        }
        return utilisateursActifsActuels < limiteUtilisateurs;
    }
}
