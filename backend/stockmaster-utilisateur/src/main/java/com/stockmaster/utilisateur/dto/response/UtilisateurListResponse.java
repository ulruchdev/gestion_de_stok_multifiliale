package com.stockmaster.utilisateur.dto.response;

import com.stockmaster.shared.domain.entity.Utilisateur;
import com.stockmaster.shared.domain.enums.RoleUtilisateur;
import lombok.Builder;
import lombok.Getter;

/**
 * US-023 — élément de la liste des utilisateurs du périmètre.
 *
 * <p>Par conception, cette classe ne porte <b>aucun champ secret</b> :
 * ni mot de passe (même haché), ni jetons — critère d'acceptation US-023
 * (« le mot de passe n'est JAMAIS retourné »).</p>
 */
@Getter
@Builder
public class UtilisateurListResponse {

    private Long id;
    private String email;
    private String prenom;
    private String nom;
    private RoleUtilisateur role;
    private Boolean actif;
    private Long entrepriseId;

    /**
     * Factory unique (ex-copies privées `toResponse` des services US-023/024/025,
     * duplication Sonar) — ne projette jamais de champ secret.
     */
    public static UtilisateurListResponse de(Utilisateur u) {
        return builder()
                .id(u.getId())
                .email(u.getEmail())
                .prenom(u.getPrenom())
                .nom(u.getNom())
                .role(u.getRole())
                .actif(u.getActif())
                .entrepriseId(u.getEntreprise() != null ? u.getEntreprise().getId() : null)
                .build();
    }
}
