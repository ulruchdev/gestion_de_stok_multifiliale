package com.stockmaster.utilisateur.dto.response;

import com.stockmaster.shared.domain.enums.RoleUtilisateur;
import lombok.Builder;
import lombok.Getter;

/**
 * US-022 — réponse de création d'un employé.
 *
 * <p>Le mot de passe (même provisoire) et tout secret interne ne sont
 * JAMAIS retournés ici.</p>
 */
@Getter
@Builder
public class EmployeResponse {

    private Long id;
    private String email;
    private String prenom;
    private String nom;
    private RoleUtilisateur role;
    private Boolean actif;
    private Long entrepriseId;
}
