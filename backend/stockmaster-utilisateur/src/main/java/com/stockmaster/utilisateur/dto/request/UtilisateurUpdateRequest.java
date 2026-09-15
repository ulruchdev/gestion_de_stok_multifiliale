package com.stockmaster.utilisateur.dto.request;

import com.stockmaster.shared.domain.enums.RoleUtilisateur;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * US-024 — corps de {@code PUT /api/v1/utilisateurs/{id}}.
 *
 * <p>Sémantique PATCH (convention US-018, endpoint PUT) : tous les champs sont
 * optionnels, {@code null} = inchangé. Le mot de passe n'est volontairement
 * <b>pas modifiable</b> via cet endpoint (flux dédié : US-011/012, EPIC 12).</p>
 *
 * <p>Le champ {@code role} est validé côté service : seuls les 4 rôles métier
 * sont acceptés — jamais une promotion vers {@code ADMIN_*} (US-021/022 pour
 * la création d'admins).</p>
 */
@Getter
@Setter
public class UtilisateurUpdateRequest {

    @Size(max = 100)
    private String prenom;

    @Size(max = 100)
    private String nom;

    @Email
    @Size(max = 150)
    private String email;

    private RoleUtilisateur role;
}
