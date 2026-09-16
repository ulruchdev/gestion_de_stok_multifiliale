package com.stockmaster.utilisateur.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * US-025 — corps de {@code PATCH /api/v1/utilisateurs/{id}/statut}.
 *
 * <p>Le champ {@code actif} est <b>obligatoire</b> : pas de bascule implicite —
 * l'admin exprime toujours explicitement l'état cible (désactiver par erreur un
 * employé actif parce que le corps était vide n'est pas une option).</p>
 */
@Getter
@Setter
public class StatutUtilisateurRequest {

    /** État cible du compte : {@code true} = réactiver, {@code false} = désactiver. */
    @NotNull(message = "Le champ actif est obligatoire (true = activer, false = désactiver)")
    private Boolean actif;
}
