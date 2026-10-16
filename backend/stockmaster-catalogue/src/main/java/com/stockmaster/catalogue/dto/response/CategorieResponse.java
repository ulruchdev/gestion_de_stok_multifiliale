package com.stockmaster.catalogue.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * US-027 — réponse de création de catégorie.
 * US-028/029/030 — réponse commune aux opérations catalogue (lecture, modification,
 * suppression logique).
 *
 * <p>Aucun champ secret : tout est public dans le périmètre du groupe.</p>
 */
@Getter
@Builder
public class CategorieResponse {

    private Long id;
    private String code;
    private String designation;
    private BigDecimal tauxTva;
    private Long groupId;
}
