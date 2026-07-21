package com.stockmaster.shared.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration JWT externalisée via {@code application.yml}.
 *
 * <p>Préfixe : {@code stockmaster.jwt}</p>
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "stockmaster.jwt")
public class JwtProperties {

    /** Clé secrète pour signer les tokens (HS256, ≥ 256 bits en base64). */
    @NotBlank
    private String secret;

    /** Durée de validité de l'access token en secondes (défaut : 15 minutes). */
    @Positive
    private long accessTokenExpiration = 900;

    /** Durée de validité du refresh token en secondes (défaut : 7 jours). */
    @Positive
    private long refreshTokenExpiration = 604800;

    /** Issuer du token. */
    @NotBlank
    private String issuer = "stockmaster";
}
