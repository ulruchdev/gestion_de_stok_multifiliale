package com.stockmaster.shared.storage;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration MinIO externalisée via {@code application.yml}.
 *
 * <p>Préfixe : {@code stockmaster.minio}</p>
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "stockmaster.minio")
public class MinioProperties {

    /** URL du serveur MinIO (ex. {@code http://localhost:9000}). */
    @NotBlank
    private String endpoint;

    @NotBlank
    private String accessKey;

    @NotBlank
    private String secretKey;

    /** Bucket unique pour les fichiers applicatifs (logos, pièces jointes...), objets préfixés par domaine. */
    private String bucket = "stockmaster";
}
