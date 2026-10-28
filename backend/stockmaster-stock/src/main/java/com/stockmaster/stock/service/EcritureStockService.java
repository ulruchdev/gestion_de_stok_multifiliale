package com.stockmaster.stock.service;

import com.stockmaster.catalogue.domain.entity.Article;
import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.shared.domain.entity.Utilisateur;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.exception.InsufficientStockException;
import com.stockmaster.stock.domain.entity.MouvementStock;
import com.stockmaster.stock.domain.enums.OrigineType;
import com.stockmaster.stock.domain.enums.TypeMouvement;
import com.stockmaster.stock.port.EcritureStockPort;
import com.stockmaster.stock.repository.MouvementStockRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Implémentation du port d'écriture du stock — ticket GS-1240.
 *
 * <p>Voir {@link EcritureStockPort} pour le contrat et les invariants garantis
 * ({@code DEC-023} + addendum : stock jamais négatif, sérialisation explicite,
 * journal immuable). Le calcul du stock réel et le sens de l'effet sont délégués
 * à {@link CalculStockService} — règle unique {@code DEC-023}.</p>
 *
 * <p>⇇ Isolation multi-tenant : la requête de référence d'écriture vérifie en
 * une fois que le couple {@code (article, utilisateur)} appartient bien à
 * l'entreprise passée (article du même groupe que l'entreprise + utilisateur
 * actif de cette entreprise) — sinon {@code RES_001} (404), même pattern
 * uniforme que US-033/US-052 pour ne jamais révéler l'existence d'une
 * ressource d'un autre tenant.</p>
 */
@Service
@RequiredArgsConstructor
public class EcritureStockService implements EcritureStockPort {

    private final MouvementStockRepository mouvementStockRepository;
    private final EntityManager entityManager;

    private static boolean estCorrection(TypeMouvement typeMouvement) {
        return typeMouvement == TypeMouvement.CORRECTION_POS
                || typeMouvement == TypeMouvement.CORRECTION_NEG;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // knowledge.md : @Transactional sur les écritures
    public MouvementEnregistre enregistrer(RequeteMouvement requete) {
        if (requete == null || requete.typeMouvement() == null || requete.quantite() == null
                || requete.quantite().signum() <= 0) {
            throw new BusinessException(ErrorCode.SYS_BAD_REQUEST,
                    "typeMouvement et quantite (> 0) sont obligatoires");
        }

        if (motifEstObligatoire(requete.typeMouvement()) && motifVide(requete.motif())) {
            throw new BusinessException(ErrorCode.STK_MOTIF_REQUIRED);
        }

        if (estCorrection(requete.typeMouvement())
                && requete.origineType() != OrigineType.CORRECTION) {
            throw new BusinessException(ErrorCode.SYS_BAD_REQUEST,
                    "origineType doit être CORRECTION pour un mouvement CORRECTION_*");
        }

        if (requete.entrepriseId() == null) {
            throw new BusinessException(ErrorCode.SYS_BAD_REQUEST, "entrepriseId est obligatoire");
        }

        // DEC-023 addendum : sérialisation explicite avant toute lecture du stock —
        // advisory-lock transactionnel sur le couple (entreprise, article).
        mouvementStockRepository.verrouilleCouple(requete.entrepriseId(), requete.articleId());

        Long utilisateurId = requete.utilisateurId();
        boolean referenceValide = utilisateurId != null && mouvementStockRepository.referenceValide(
                requete.articleId(), requete.entrepriseId(), utilisateurId);
        if (!referenceValide) {
            // 404 uniforme : on ne révèle pas l'existence d'une ressource d'un autre tenant.
            throw new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND,
                    "Article ou utilisateur introuvable dans cette entreprise");
        }

        BigDecimal stockReel = mouvementStockRepository
                .calculerStockReel(requete.entrepriseId(), requete.articleId());

        int signe = CalculStockService.signeDe(requete.typeMouvement());

        MouvementStock mouvement = MouvementStock.builder()
                .entreprise(entityManager.getReference(Entreprise.class, requete.entrepriseId()))
                .article(entityManager.getReference(Article.class, requete.articleId()))
                .utilisateur(entityManager.getReference(Utilisateur.class, utilisateurId))
                .typeMouvement(requete.typeMouvement())
                .quantite(requete.quantite())
                .dateMouvement(Instant.now())
                .origineType(requete.origineType())
                .origineId(requete.origineId())
                .motif(requete.motif())
                .build();

        if (signe < 0) {
            // Recalculé sous le même verrou, dans la même transaction → stock final garanti >= 0
            BigDecimal stockApres = stockReel.subtract(requete.quantite());
            if (stockApres.signum() < 0) {
                throw new InsufficientStockException(java.util.List.of(
                        new InsufficientStockException.StockShortage(
                                requete.articleId(), "article " + requete.articleId(),
                                "stock disponible : " + stockReel.toPlainString(),
                                stockReel.intValue(),
                                requete.quantite().intValue())));
            }
        }

        MouvementStock saved = mouvementStockRepository.save(mouvement);
        BigDecimal stockApres = stockReel.add(
                requete.quantite().multiply(BigDecimal.valueOf(signe)));

        return new MouvementEnregistre(saved.getId(), stockApres);
    }

    private boolean motifEstObligatoire(TypeMouvement typeMouvement) {
        return typeMouvement == TypeMouvement.CORRECTION_POS
                || typeMouvement == TypeMouvement.CORRECTION_NEG;
    }

    private boolean motifVide(String motif) {
        return motif == null || motif.isBlank();
    }
}
