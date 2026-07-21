package com.stockmaster.shared.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration de la pagination externalisée via {@code application.yml}.
 *
 * <p>Préfixe : {@code stockmaster.pagination}</p>
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "stockmaster.pagination")
public class PaginationProperties {

    /** Taille de page par défaut. */
    @Positive
    private int defaultPageSize = 20;

    /** Taille de page maximale autorisée (protection contre les abus). */
    @Positive
    @Max(500)
    private int maxPageSize = 100;

    /** Valeur par défaut pour le tri (ex: dateCreation,desc). */
    private String defaultSort = "dateCreation,desc";
}
