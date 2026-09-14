package com.stockmaster.shared.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Enveloppe de pagination standard — remplace {@code Page<T>} de Spring Data
 * dans les réponses API pour un format JSON stable, indépendant de la version
 * de spring-data-commons (US-017, premier endpoint paginé du projet).
 *
 * @param <T> le type des éléments de la page
 */
@Getter
@Builder
public class PageResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
