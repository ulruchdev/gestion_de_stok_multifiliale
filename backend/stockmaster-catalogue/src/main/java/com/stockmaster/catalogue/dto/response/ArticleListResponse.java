package com.stockmaster.catalogue.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * US-032 — réponse paginée de {@code GET /api/v1/articles}.
 */
@Getter
@Builder
@AllArgsConstructor
public class ArticleListResponse {

    private List<ArticleResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    public static ArticleListResponse from(org.springframework.data.domain.Page<ArticleResponse> page) {
        return ArticleListResponse.builder()
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
