package com.stockmaster.utilisateur.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * US-026 — part JSON de {@code PUT /api/v1/utilisateurs/profil}.
 *
 * <p>Sémantique PATCH (convention US-024) : champs {@code null} = inchangés.
 * <b>Email et rôle volontairement absents</b> (critère d'acceptation US-026) :
 * un utilisateur ne modifie jamais son email ni son rôle — la modification
 * admin passe par US-024, l'email et le rôle sont des attributs contrôlés.</p>
 *
 * <p>La photo voyage dans une part {@code photo} séparée (multipart) et est
 * traitée par {@code MinioService} — jamais en base64 dans ce JSON.</p>
 */
@Getter
@Setter
public class ProfilUtilisateurRequest {

    @Size(max = 100)
    private String prenom;

    @Size(max = 100)
    private String nom;

    /** Ville de résidence (colonne {@code adresse_ville} VARCHAR(100)). */
    @Size(max = 100)
    private String adresseVille;
}
