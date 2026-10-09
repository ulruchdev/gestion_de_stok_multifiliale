package com.stockmaster.catalogue.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * US-027 — réponse de création de catégorie. Aucun champ secret : tout est
 * public dans le périmètre du groupe.
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
