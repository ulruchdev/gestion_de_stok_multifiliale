package com.stockmaster.tiers.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * US-040 — réponse du CRUD clients ({@code POST}/{@code PUT}/{@code DELETE}).
 *
 * <p>{@code entrepriseId} est dérivé de l'association {@code Client.entreprise}
 * (tenant du JWT) — jamais saisi en entrée.</p>
 *
 * <p>{@code supprime} (US-043) distingue un archivage réussi d'une
 * modification : le {@code DELETE} renvoie {@code 200} + cette même enveloppe,
 * convention US-029/035/039. Aucun champ secret ni historique de commandes
 * n'est exposé.</p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponse {

    private Long id;
    private String nom;
    private String prenom;
    private String telephone;
    private String email;
    private String adresseVille;
    private String adresseQuartier;
    private Long entrepriseId;

    /** {@code true} si la fiche est archivée (soft delete, US-043). */
    private Boolean supprime;
}
