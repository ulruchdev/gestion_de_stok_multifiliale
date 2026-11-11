package com.stockmaster.tiers.repository;

import com.stockmaster.tiers.domain.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByIdAndSupprimeFalse(Long id);

    Page<Client> findAllByEntrepriseIdAndSupprimeFalse(Long entrepriseId, Pageable pageable);

    /**
     * US-041 — recherche par nom, prénom ou téléphone, scopée au tenant.
     *
     * <p>Bind params JPQL uniquement — aucune concaténation de chaînes,
     * miroir de {@code FournisseurRepository.rechercherParEntrepriseEtTexte} :
     * {@code LOWER(...) LIKE LOWER('%x%')}, insensible à la casse, accents non
     * normalisés (convention repo). Le scoping {@code entreprise_id} n'est
     * jamais paramétrable par l'appelant — il vient du JWT. Un {@code prenom}
     * ou {@code telephone} NULL ne matche simplement pas.</p>
     */
    @Query("""
            SELECT c FROM Client c
            WHERE c.entreprise.id = :entrepriseId
              AND c.supprime = false
              AND (
                    LOWER(c.nom) LIKE LOWER(CONCAT('%', :recherche, '%'))
                 OR LOWER(c.prenom) LIKE LOWER(CONCAT('%', :recherche, '%'))
                 OR c.telephone LIKE CONCAT('%', :recherche, '%')
              )
            """)
    Page<Client> rechercherParEntrepriseEtTexte(
            @Param("entrepriseId") Long entrepriseId,
            @Param("recherche") String recherche,
            Pageable pageable);

    long countByEntrepriseIdAndSupprimeFalse(Long entrepriseId);
}
