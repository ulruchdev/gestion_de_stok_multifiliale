package com.stockmaster.shared.storage;

import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Service MinIO commun — upload d'images (logos, pièces jointes...) réutilisable
 * par tout module fonctionnel (US-014 logo groupe, US-081 logo entreprise/filiale...).
 *
 * <p>Un seul bucket applicatif ({@code stockmaster.minio.bucket}), les objets sont
 * préfixés par domaine appelant (ex. {@code groupe/<id>/<uuid>.png}) pour éviter les
 * collisions entre modules sans multiplier les buckets.</p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MinioService {

    /** 2 Mo — suffisant pour un logo, évite les abus (aucune limite documentée ailleurs dans le projet). */
    private static final long MAX_FILE_SIZE = 2L * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/png", "image/jpeg", "image/webp");

    private final MinioClient minioClient;
    private final MinioProperties properties;

    /**
     * Upload une image et retourne son URL publique persistante.
     *
     * @param prefix préfixe logique de l'objet (ex. {@code "groupe/42"}), sans slash final
     * @throws BusinessException {@code SYS_BAD_REQUEST} si le fichier est vide,
     *         {@code SYS_FILE_TOO_LARGE} si &gt; 2 Mo,
     *         {@code SYS_MEDIA_TYPE_NOT_SUPPORTED} si le type n'est pas PNG/JPEG/WebP,
     *         {@code SYS_INTERNAL_ERROR} en cas d'échec MinIO
     */
    public String uploadImage(String prefix, MultipartFile file) {
        validate(file);
        ensureBucketExists();

        String objectKey = prefix + "/" + UUID.randomUUID() + extensionFrom(file.getContentType());
        try (InputStream in = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .stream(in, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
        } catch (Exception e) {
            log.error("event=minio_upload_failed objectKey={}", objectKey, e);
            throw new BusinessException(ErrorCode.SYS_INTERNAL_ERROR, e);
        }

        return properties.getEndpoint() + "/" + properties.getBucket() + "/" + objectKey;
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.SYS_BAD_REQUEST, "Le fichier est vide");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.SYS_FILE_TOO_LARGE);
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new BusinessException(ErrorCode.SYS_MEDIA_TYPE_NOT_SUPPORTED);
        }
    }

    /** Crée le bucket au premier usage (paresseux) et l'ouvre en lecture publique (assets publics type logo). */
    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(properties.getBucket())
                    .build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(properties.getBucket())
                        .build());
                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                        .bucket(properties.getBucket())
                        .config(publicReadPolicy(properties.getBucket()))
                        .build());
            }
        } catch (Exception e) {
            log.error("event=minio_bucket_check_failed bucket={}", properties.getBucket(), e);
            throw new BusinessException(ErrorCode.SYS_INTERNAL_ERROR, e);
        }
    }

    private String extensionFrom(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }

    private String publicReadPolicy(String bucket) {
        return """
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Effect": "Allow",
                      "Principal": {"AWS": ["*"]},
                      "Action": ["s3:GetObject"],
                      "Resource": ["arn:aws:s3:::%s/*"]
                    }
                  ]
                }
                """.formatted(bucket);
    }
}
