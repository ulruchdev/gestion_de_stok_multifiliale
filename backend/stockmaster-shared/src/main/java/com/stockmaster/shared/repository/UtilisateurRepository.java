package com.stockmaster.shared.repository;

import com.stockmaster.shared.domain.entity.Utilisateur;
import com.stockmaster.shared.domain.enums.RoleUtilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    boolean existsByEmail(String email);

    Optional<Utilisateur> findByEmail(String email);

    long countByEntrepriseGroupeIdAndSupprimeFalse(Long groupeId);

    /**
     * US-101 : décompte pour la limite d'utilisateurs (DEC-015) — actifs et non
     * supprimés du groupe, toutes filiales confondues (symétrique de
     * {@code countByGroupeIdAndSiteOperationnelTrueAndActifTrueAndSupprimeFalse}).
     */
    long countByEntrepriseGroupeIdAndActifTrueAndSupprimeFalse(Long groupeId);

    /** US-017 : nombre d'employés d'une filiale (même convention que le compteur groupe : pas de filtre actif). */
    long countByEntrepriseIdAndSupprimeFalse(Long entrepriseId);

    /**
     * US-023 — utilisateurs d'une filiale précise. Jointure sur {@code entreprise}
     * pour éviter le N+1 lors du mapping vers la réponse. Périmètre et filtres
     * optionnels portés par les paramètres nullables (convention {@code US-017}).
     */
    @Query("SELECT u FROM Utilisateur u JOIN FETCH u.entreprise "
            + "WHERE u.entreprise.id = :entrepriseId AND u.supprime = false "
            + "AND (:role IS NULL OR u.role = :role) "
            + "AND (:actif IS NULL OR u.actif = :actif)")
    Page<Utilisateur> findByEntrepriseId(@Param("entrepriseId") Long entrepriseId,
            @Param("role") RoleUtilisateur role, @Param("actif") Boolean actif, Pageable pageable);

    /**
     * US-023 — utilisateurs de toutes les filiales d'un groupe (Admin Groupe).
     * {@code entrepriseId} nullable pour resserrer sur une filiale du groupe
     * (pré-validée par le service). Même convention de jointure.
     */
    @Query("SELECT u FROM Utilisateur u JOIN FETCH u.entreprise "
            + "WHERE u.entreprise.groupe.id = :groupeId AND u.supprime = false "
            + "AND (:entrepriseId IS NULL OR u.entreprise.id = :entrepriseId) "
            + "AND (:role IS NULL OR u.role = :role) "
            + "AND (:actif IS NULL OR u.actif = :actif)")
    Page<Utilisateur> findByEntrepriseGroupeId(@Param("groupeId") Long groupeId,
            @Param("entrepriseId") Long entrepriseId, @Param("role") RoleUtilisateur role,
            @Param("actif") Boolean actif, Pageable pageable);
}
