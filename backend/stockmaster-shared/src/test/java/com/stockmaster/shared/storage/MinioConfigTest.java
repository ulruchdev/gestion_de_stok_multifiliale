package com.stockmaster.shared.storage;

import io.minio.MinioClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MinioConfig — construction du bean {@link MinioClient} à partir de {@link MinioProperties}.
 *
 * <p>Le constructeur {@code MinioClient.builder()...build()} ne fait aucun appel réseau
 * (le client est paresseux) — ce test valide uniquement le câblage, pas la connectivité.</p>
 */
@DisplayName("MinioConfig.minioClient()")
class MinioConfigTest {

    @Test
    @DisplayName("Construit un MinioClient non-null à partir des propriétés configurées")
    void shouldBuildMinioClientFromProperties() {
        MinioProperties properties = new MinioProperties();
        properties.setEndpoint("http://localhost:9000");
        properties.setAccessKey("stockmaster");
        properties.setSecretKey("stockmaster");
        properties.setBucket("stockmaster");

        MinioClient client = new MinioConfig(properties).minioClient();

        assertThat(client).isNotNull();
    }
}
