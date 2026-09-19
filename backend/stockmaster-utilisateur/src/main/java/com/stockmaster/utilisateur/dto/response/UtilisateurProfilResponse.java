package com.stockmaster.utilisateur.dto.response;

import com.stockmaster.shared.domain.entity.Utilisateur;
import com.stockmaster.shared.domain.enums.RoleUtilisateur;
import lombok.Builder;
import lombok.Getter;

/**
 * US-026 — profil de l'utilisateur connecté ({@code GET /utilisateurs/profil}).
 *
 * <p>Même discipline que {@link UtilisateurListResponse} : <b>aucun champ
 * secret</b> (ni mot de passe, ni jetons). S'y ajoutent les champs personnels
 * modifiables en self-service ({@code adresseVille}, {@code photo}).</p>
 */
@Getter
@Builder
public class UtilisateurProfilResponse {

    private Long id;
    private String email;
    private String prenom;
    private String nom;
    private RoleUtilisateur role;
    private Boolean actif;
    private Long entrepriseId;

    /** Champs personnels self-service. */
    private String adresseVille;
    private String photo;

    /**
     * Factory unique — projette le profil sans jamais exposer de champ secret.
     */
    public static UtilisateurProfilResponse de(Utilisateur u) {
        return builder()
                .id(u.getId())
                .email(u.getEmail())
                .prenom(u.getPrenom())
                .nom(u.getNom())
                .role(u.getRole())
                .actif(u.getActif())
                .entrepriseId(u.getEntreprise() != null ? u.getEntreprise().getId() : null)
                .adresseVille(u.getAdresseVille())
                .photo(u.getPhoto())
                .build();
    }
}
