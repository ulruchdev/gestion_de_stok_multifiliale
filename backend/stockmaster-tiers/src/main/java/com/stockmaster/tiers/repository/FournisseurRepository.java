package com.stockmaster.tiers.repository;

import com.stockmaster.tiers.domain.entity.Fournisseur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FournisseurRepository extends JpaRepository<Fournisseur, Long> {

    Optional<Fournisseur> findByIdAndSupprimeFalse(Long id);

    Page<Fournisseur> findAllByEntrepriseIdAndSupprimeFalse(Long entrepriseId, Pageable pageable);

    /**
     * US-037 — recherche par nom (raison sociale) ou ville, scopée au tenant.
     *
     * <p>Bind params JPQL uniquement — aucune concaténation de chaînes
     * (anti-pattern interdit), miroir exact de
     * {@code ArticleRepository.rechercherParGroupeEtTexte} (US-032) :
     * {@code LOWER(...) LIKE LOWER('%x%')}, insensible à la casse, accents non
     * normalisés (convention repo). Le scoping {@code entreprise_id} n'est
     * jamais paramétrable par l'appelant — il vient du JWT.</p>
     */
    @Query("""
            SELECT f FROM Fournisseur f
            WHERE f.entreprise.id = :entrepriseId
              AND f.supprime = false
              AND (
                    LOWER(f.raisonSociale) LIKE LOWER(CONCAT('%', :recherche, '%'))
                 OR LOWER(f.adresseVille) LIKE LOWER(CONCAT('%', :recherche, '%'))
              )
            """)
    Page<Fournisseur> rechercherParEntrepriseEtTexte(
            @Param("entrepriseId") Long entrepriseId,
            @Param("recherche") String recherche,
            Pageable pageable);

    /** Contrôle d'unicité NIF au niveau entreprise (NIF NULL autorisé, cf. normalisation). */
    boolean existsByEntrepriseIdAndNifAndSupprimeFalse(Long entrepriseId, String nif);

    /** Chargement batch pour les listes de commandes fournisseur. */
    List<Fournisseur> findAllByIdInAndSupprimeFalse(List<Long> ids);

    long countByEntrepriseIdAndSupprimeFalse(Long entrepriseId);
}
