package com.stockmaster.shared.repository;

import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.shared.domain.enums.TypeEntreprise;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EntrepriseRepository extends JpaRepository<Entreprise, Long> {

    boolean existsByNifAndSupprimeFalse(String nif);

    boolean existsByTelephoneAndSupprimeFalse(String telephone);

    boolean existsByEmailAndSupprimeFalse(String email);

    long countByGroupeIdAndSupprimeFalse(Long groupeId);
    long countByGroupeIdAndTypeEntrepriseAndActifTrueAndSupprimeFalse(Long groupeId, TypeEntreprise typeEntreprise);

    /**
     * US-016 : miroir exact de la contrainte DB {@code UNIQUE (group_id, code_filiale)}
     * (V1__init_schema.sql) — SANS filtre {@code supprime}, la contrainte n'en a pas non
     * plus : un code de filiale soft-supprimée reste réservé au niveau base.
     */
    boolean existsByGroupeIdAndCodeFiliale(Long groupeId, String codeFiliale);

    /**
     * US-016 : compte les sites qui comptent dans {@code limite_filiales} (DEC-015) —
     * TOUS les types (la maison mère compte si elle détient du stock), actifs,
     * opérationnels, non supprimés.
     */
    long countByGroupeIdAndSiteOperationnelTrueAndActifTrueAndSupprimeFalse(Long groupeId);

    /** US-016 : maison mère du groupe, pour rattacher {@code parent_id} d'une nouvelle filiale. */
    Optional<Entreprise> findFirstByGroupeIdAndTypeEntreprise(Long groupeId, TypeEntreprise typeEntreprise);

    /**
     * US-017 : filiales du groupe, paginées, avec filtres {@code actif}/{@code ville}
     * optionnels (motif {@code :param IS NULL OR ...} — un seul paramètre laissé
     * {@code null} désactive son filtre, évite de multiplier les méthodes dérivées).
     */
    @Query("SELECT e FROM Entreprise e WHERE e.groupe.id = :groupeId AND e.typeEntreprise = :type "
            + "AND e.supprime = false "
            + "AND (:actif IS NULL OR e.actif = :actif) "
            + "AND (:ville IS NULL OR e.adresseVille = :ville)")
    Page<Entreprise> findFilialesDuGroupe(@Param("groupeId") Long groupeId, @Param("type") TypeEntreprise type,
            @Param("actif") Boolean actif, @Param("ville") String ville, Pageable pageable);
}
