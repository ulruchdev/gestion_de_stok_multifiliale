package com.stockmaster.shared.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Hiérarchie d'exceptions métier partagée (codes ErrorCode + statut HTTP)")
class SharedExceptionsTest {

    @Test
    @DisplayName("BusinessException(ErrorCode) → message du code + statut HTTP du code")
    void shouldBuildFromErrorCodeOnly() {
        BusinessException e = new BusinessException(ErrorCode.GRP_FILIALE_LIMIT_REACHED);
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.GRP_FILIALE_LIMIT_REACHED);
        assertThat(e.getMessage()).isEqualTo("Limite de filiales atteinte pour votre plan d'abonnement");
        assertThat(e.getHttpStatus()).isEqualTo(403);
        assertThat(e.getArgs()).isEmpty();
    }

    @Test
    @DisplayName("BusinessException(ErrorCode, detail) → message = detail")
    void shouldUseDetailAsMessage() {
        BusinessException e = new BusinessException(ErrorCode.SEC_INVALID_PASSWORD, "Ancien mot de passe erroné");
        assertThat(e.getMessage()).isEqualTo("Ancien mot de passe erroné");
        assertThat(e.getHttpStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("BusinessException(ErrorCode, detail, args...) → args portés pour l'i18n")
    void shouldCarryArgs() {
        BusinessException e = new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND, "Article {0} absent", 7L, "x");
        assertThat(e.getArgs()).containsExactly(7L, "x");
    }

    @Test
    @DisplayName("BusinessException(ErrorCode, cause) → cause chaînée")
    void shouldChainCause() {
        IllegalStateException cause = new IllegalStateException("boom");
        BusinessException e = new BusinessException(ErrorCode.SEC_STORE_UNAVAILABLE, cause);
        assertThat(e.getCause()).isSameAs(cause);
        assertThat(e.getHttpStatus()).isEqualTo(503);
    }

    @Test
    @DisplayName("EntityNotFoundException(id) → message contenant l'id, 404, accès aux champs")
    void entityNotFoundById() {
        EntityNotFoundException e = new EntityNotFoundException("Article", 42L);
        assertThat(e.getHttpStatus()).isEqualTo(404);
        assertThat(e.getEntityName()).isEqualTo("Article");
        assertThat(e.getEntityId()).isEqualTo(42L);
        assertThat(e.getMessage()).contains("Article").contains("42");
    }

    @Test
    @DisplayName("EntityNotFoundException(champ, valeur) → entityId null, message contenant la valeur")
    void entityNotFoundByField() {
        EntityNotFoundException e = new EntityNotFoundException("Article", "code", "ART-001");
        assertThat(e.getEntityId()).isNull();
        assertThat(e.getMessage()).contains("code").contains("ART-001");
    }

    @Test
    @DisplayName("InsufficientStockException → 409, message avec le nombre d'articles, shortages portés")
    void insufficientStockCarriesShortages() {
        InsufficientStockException.StockShortage s1 = new InsufficientStockException.StockShortage(
                1L, "ART-001", "Huile 5L", 2, 10);
        InsufficientStockException e = new InsufficientStockException(List.of(s1));
        assertThat(e.getHttpStatus()).isEqualTo(409);
        assertThat(e.getMessage()).contains("1 article");
        assertThat(e.getShortages()).hasSize(1);
        assertThat(e.getShortages().get(0).getArticleId()).isEqualTo(1L);
        assertThat(e.getShortages().get(0).getCodeArticle()).isEqualTo("ART-001");
        assertThat(e.getShortages().get(0).getDesignation()).isEqualTo("Huile 5L");
        assertThat(e.getShortages().get(0).getStockDisponible()).isEqualTo(2);
        assertThat(e.getShortages().get(0).getQuantiteDemandee()).isEqualTo(10);
    }
}
