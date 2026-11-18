package com.stockmaster.vente.repository;

import com.stockmaster.vente.domain.entity.LigneVente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface LigneVenteRepository extends JpaRepository<LigneVente, Long> {

    List<LigneVente> findAllByVenteIdAndSupprimeFalse(Long venteId);

    List<LigneVente> findAllByVenteIdInAndSupprimeFalse(List<Long> venteIds);

    /**
     * Lecture reporting des lignes de vente de une ou plusieurs filiales — GS-1251.
     *
     * <p>Projection brute consommée par {@code LectureVenteService} :
     * {@code [entrepriseId, articleId, articleLibelle, clientId, clientNom,
     * quantite, prixUnitaire, tauxTva, dateVente]}.</p>
     *
     * <p>{@code left join} sur le client : les ventes au comptoir (client de
     * passage, {@code client_id} nullable — DEC-009) doivent être comptées dans
     * le CA, pas exclues par une jointure interne.</p>
     *
     * <p>Période en bornes semi-ouvertes {@code [debut, fin)} et statut
     * {@code PAYEE} uniquement : une vente annulée ou remboursée ne contribue
     * pas au chiffre d'affaires. Une filiale unique passe par la même requête
     * avec une liste d'un élément — une seule projection à maintenir.</p>
     */
    @Query("""
            select v.entreprise.id, a.id, a.designation, c.id, c.nom,
                   lv.quantite, lv.prixUnitaire, lv.tauxTvaSnapshot, v.dateVente
            from LigneVente lv
            join lv.vente v
            join lv.article a
            left join v.client c
            where lv.supprime = false
              and v.supprime = false
              and v.statut = com.stockmaster.vente.domain.enums.StatutVente.PAYEE
              and v.entreprise.id in :entrepriseIds
              and v.dateVente >= :debut
              and v.dateVente < :fin
            """)
    List<Object[]> lignesVendues(@Param("entrepriseIds") List<Long> entrepriseIds,
                                 @Param("debut") Instant debut,
                                 @Param("fin") Instant fin);
}
