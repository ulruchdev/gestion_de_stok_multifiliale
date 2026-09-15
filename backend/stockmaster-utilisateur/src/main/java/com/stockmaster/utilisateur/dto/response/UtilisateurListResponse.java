package com.stockmaster.utilisateur.dto.response;

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
}
