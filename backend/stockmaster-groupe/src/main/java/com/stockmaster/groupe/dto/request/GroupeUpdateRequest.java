package com.stockmaster.groupe.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * Requête US-014 — {@code PUT /api/v1/groupe} (multipart/form-data).
 *
 * <p>Modification partielle (sémantique PATCH) : seuls les champs non-{@code null}
 * (texte) ou non-vides (fichier) sont appliqués — {@code nomGroupe} vide ou absent
 * laisse le nom actuel inchangé, par exemple.</p>
 */
@Getter
@Setter
public class GroupeUpdateRequest {

    @Size(max = 100, message = "Le nom du groupe ne doit pas dépasser 100 caractères")
    private String nomGroupe;

    @Size(max = 150, message = "La raison sociale ne doit pas dépasser 150 caractères")
    private String raisonSociale;

    @Size(max = 20, message = "Le NIF ne doit pas dépasser 20 caractères")
    private String nif;

    /** Optionnel — remplace le logo existant si fourni (PNG/JPEG/WebP, 2 Mo max, voir {@code MinioService}). */
    private MultipartFile logo;
}
