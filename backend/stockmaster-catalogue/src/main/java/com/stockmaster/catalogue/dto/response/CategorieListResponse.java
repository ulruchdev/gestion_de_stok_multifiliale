package com.stockmaster.catalogue.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/**
 * US-028 — réponse paginée de {@code GET /api/v1/categories}.
 */
@Getter
@Builder
@AllArgsConstructor
public class CategorieListResponse {

    private List<CategorieResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    public static CategorieListResponse from(org.springframework.data.domain.Page<CategorieResponse> page) {
        return CategorieListResponse.builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
