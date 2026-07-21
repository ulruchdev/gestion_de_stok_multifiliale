package com.stockmaster.shared.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Catalogue centralisé des codes d'erreur métier de StockMaster CM.
 *
 * <p>Chaque enum définit un code unique, un titre lisible et un code HTTP associé.
 * Utilisé par {@link BusinessException} et {@code GlobalExceptionHandler}
 * pour produire des réponses RFC 7807 cohérentes.</p>
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ========== AUTH — Authentification ==========
    AUTH_INVALID_CREDENTIALS("AUTH_001", "Email ou mot de passe incorrect", 401),
    AUTH_ACCOUNT_DISABLED("AUTH_002", "Compte désactivé", 403),
    AUTH_TENANT_SUSPENDED("AUTH_003", "Groupe suspendu", 403),
    AUTH_TOKEN_EXPIRED("AUTH_004", "Token expiré", 401),
    AUTH_TOKEN_INVALID("AUTH_005", "Token invalide", 401),
    AUTH_REFRESH_TOKEN_INVALID("AUTH_006", "Refresh token invalide ou expiré", 401),
    AUTH_TOKEN_BLACKLISTED("AUTH_007", "Token révoqué", 401),
    AUTH_RATE_LIMIT("AUTH_429", "Trop de tentatives. Réessayez dans 15 minutes.", 429),
    AUTH_EMAIL_ALREADY_EXISTS("AUTH_008", "Cet email est déjà utilisé", 409),
    AUTH_RESET_TOKEN_INVALID("AUTH_009", "Token de réinitialisation invalide ou expiré", 400),
    AUTH_RESET_TOKEN_EXPIRED("AUTH_010", "Token de réinitialisation expiré", 400),

    // ========== RES — Ressources ==========
    RES_ENTITY_NOT_FOUND("RES_001", "Ressource non trouvée", 404),
    RES_DUPLICATE_CODE("RES_002", "Ce code existe déjà", 409),
    RES_DUPLICATE_EMAIL("RES_003", "Cet email existe déjà", 409),
    RES_DUPLICATE_FILIALE_CODE("RES_004", "Ce code filiale existe déjà dans le groupe", 409),
    RES_ENTITY_HAS_DEPENDENCIES("RES_005", "Cette ressource a des dépendances et ne peut pas être supprimée", 409),

    // ========== CMD — Commandes ==========
    CMD_ORDER_NOT_MODIFIABLE("CMD_001", "La commande n'est pas modifiable dans son état actuel", 409),
    CMD_ORDER_HAS_NO_LINES("CMD_002", "La commande doit contenir au moins une ligne", 400),
    CMD_ORDER_CANNOT_BE_DELETED("CMD_003", "La commande ne peut pas être supprimée dans son état actuel", 409),

    // ========== GRP — Groupe & Filiales ==========
    GRP_FILIALE_LIMIT_REACHED("GRP_001", "Limite de filiales atteinte pour votre plan d'abonnement", 403),
    GRP_CROSS_GROUP_FORBIDDEN("GRP_002", "Opération interdite entre des groupes différents", 403),
    GRP_SAME_SOURCE_AND_TARGET("GRP_003", "La filiale source et la filiale cible doivent être différentes", 400),

    // ========== STK — Stock ==========
    STK_INSUFFICIENT_STOCK("STK_001", "Stock insuffisant pour réaliser cette opération", 409),
    STK_MOTIF_REQUIRED("STK_002", "Un motif est obligatoire pour cette opération", 400),

    // ========== SEC — Sécurité ==========
    SEC_ACCESS_DENIED("SEC_001", "Accès refusé", 403),
    SEC_INVALID_PASSWORD("SEC_002", "Ancien mot de passe incorrect", 400),
    SEC_PASSWORD_WEAK("SEC_003", "Le mot de passe ne respecte pas les critères de robustesse", 400),

    // ========== SYS — Système / Générique ==========
    SYS_INTERNAL_ERROR("SYS_001", "Erreur interne du serveur", 500),
    SYS_BAD_REQUEST("SYS_002", "Requête invalide", 400),
    SYS_METHOD_NOT_ALLOWED("SYS_003", "Méthode non autorisée", 405),
    SYS_MEDIA_TYPE_NOT_SUPPORTED("SYS_004", "Type de média non supporté", 415),
    SYS_VALIDATION_ERROR("SYS_005", "Erreur de validation des champs", 400);

    private final String code;
    private final String message;
    private final int httpStatus;
}
