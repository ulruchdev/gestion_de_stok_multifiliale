package com.stockmaster.shared.storage;

import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MinioService — service commun d'upload d'images (US-014, réutilisable par US-081...).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MinioService.uploadImage(prefix, file)")
class MinioServiceTest {

    @Mock
    private MinioClient minioClient;

    private MinioProperties properties;

    private MinioService minioService;

    @BeforeEach
    void setUp() {
        properties = new MinioProperties();
        properties.setEndpoint("http://localhost:9000");
        properties.setAccessKey("stockmaster");
        properties.setSecretKey("stockmaster");
        properties.setBucket("stockmaster");
        minioService = new MinioService(minioClient, properties);
    }

    private MockMultipartFile pngFile(String content) {
        return new MockMultipartFile("logo", "logo.png", "image/png", content.getBytes());
    }

    @Nested
    @DisplayName("✅ Upload valide")
    class UploadValide {

        @Test
        @DisplayName("Bucket déjà existant → upload direct, URL au format bucket/objet")
        void shouldUploadAndReturnUrlWhenBucketAlreadyExists() throws Exception {
            when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

            String url = minioService.uploadImage("groupe/1", pngFile("fake-png-bytes"));

            assertThat(url).startsWith("http://localhost:9000/stockmaster/groupe/1/");
            assertThat(url).endsWith(".png");
            verify(minioClient).putObject(any(PutObjectArgs.class));
            verify(minioClient, never()).makeBucket(any(MakeBucketArgs.class));
        }

        @Test
        @DisplayName("Bucket absent → création + politique publique avant l'upload")
        void shouldCreateBucketWithPublicPolicyWhenMissing() throws Exception {
            when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

            minioService.uploadImage("groupe/1", pngFile("fake-png-bytes"));

            verify(minioClient).makeBucket(any(MakeBucketArgs.class));
            verify(minioClient).setBucketPolicy(any(SetBucketPolicyArgs.class));
            verify(minioClient).putObject(any(PutObjectArgs.class));
        }
    }

    @Nested
    @DisplayName("❌ Validations")
    class Validations {

        @Test
        @DisplayName("Fichier vide → SYS_BAD_REQUEST (400)")
        void shouldThrowWhenFileEmpty() {
            MockMultipartFile fichierVide = new MockMultipartFile("logo", "logo.png", "image/png", new byte[0]);

            assertThatThrownBy(() -> minioService.uploadImage("groupe/1", fichierVide))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SYS_BAD_REQUEST);
        }

        @Test
        @DisplayName("Fichier > 2 Mo → SYS_FILE_TOO_LARGE (413)")
        void shouldThrowWhenFileTooLarge() {
            byte[] tropGros = new byte[2 * 1024 * 1024 + 1];
            MockMultipartFile fichier = new MockMultipartFile("logo", "logo.png", "image/png", tropGros);

            assertThatThrownBy(() -> minioService.uploadImage("groupe/1", fichier))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SYS_FILE_TOO_LARGE);
        }

        @Test
        @DisplayName("Type non autorisé (PDF) → SYS_MEDIA_TYPE_NOT_SUPPORTED (415)")
        void shouldThrowWhenContentTypeNotAllowed() {
            MockMultipartFile pdf = new MockMultipartFile("logo", "doc.pdf", "application/pdf", "contenu".getBytes());

            assertThatThrownBy(() -> minioService.uploadImage("groupe/1", pdf))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SYS_MEDIA_TYPE_NOT_SUPPORTED);
        }
    }

    @Nested
    @DisplayName("❌ Échec infrastructure MinIO")
    class EchecInfrastructure {

        @Test
        @DisplayName("Erreur lors du putObject → SYS_INTERNAL_ERROR (500), cause préservée")
        void shouldWrapMinioErrorAsInternalError() throws Exception {
            when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
            when(minioClient.putObject(any(PutObjectArgs.class))).thenThrow(new IOException("MinIO indisponible"));

            assertThatThrownBy(() -> minioService.uploadImage("groupe/1", pngFile("x")))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SYS_INTERNAL_ERROR)
                    .hasCauseInstanceOf(IOException.class);
        }
    }
}
