package com.stockmaster.shared.storage;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Construit le client MinIO à partir de {@link MinioProperties}.
 *
 * <p>Ne vérifie pas la disponibilité du bucket au démarrage — le service
 * ({@link MinioService}) le crée paresseusement au premier upload, pour ne
 * pas bloquer le boot applicatif si MinIO est momentanément indisponible
 * (l'upload de logo n'est pas sur le chemin critique de démarrage).</p>
 */
@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    private final MinioProperties properties;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }
}
