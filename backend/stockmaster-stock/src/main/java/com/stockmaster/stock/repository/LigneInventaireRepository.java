package com.stockmaster.stock.repository;

import com.stockmaster.stock.domain.entity.LigneInventaire;
import com.stockmaster.stock.domain.enums.StatutLigneInventaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LigneInventaireRepository extends JpaRepository<LigneInventaire, Long> {

    List<LigneInventaire> findAllBySessionInventaireIdAndSupprimeFalse(Long sessionId);

    List<LigneInventaire> findAllBySessionInventaireIdAndStatutAndSupprimeFalse(
            Long sessionId, StatutLigneInventaire statut);

    /** Une seule ligne par article dans une campagne (contrôle applicatif à l'ajout). */
    boolean existsBySessionInventaireIdAndArticleIdAndSupprimeFalse(Long sessionId, Long articleId);
}
