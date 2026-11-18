package com.stockmaster.vente.service;

import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.vente.port.LectureVentePort;
import com.stockmaster.vente.repository.LigneVenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Implémentation du port de lecture des ventes — ticket GS-1251.
 *
 * <p>Voir {@link LectureVentePort} pour le contrat. Le service ne fait que
 * valider les bornes puis projeter les lignes brutes : aucune agrégation, aucun
 * calcul de CA (délégués à {@code reporting}, DEC-020).</p>
 *
 * <p>Lecture seule : {@code @Transactional(readOnly = true)} — knowledge.md
 * impose une transaction explicite sur les accès aux données, et interdit toute
 * écriture depuis un service de lecture.</p>
 */
@Service
@RequiredArgsConstructor
public class LectureVenteService implements LectureVentePort {

    private final LigneVenteRepository ligneVenteRepository;

    /**
     * {@inheritDoc}
     *
     * @throws BusinessException {@code SYS_BAD_REQUEST} si l'entreprise, une
     *         borne de période ou l'ordre des bornes est invalide
     */
    @Override
    @Transactional(readOnly = true)
    public List<LigneVenteAgregat> lignesVendues(Long entrepriseId, Instant debut, Instant fin) {
        verifierEntreprise(entrepriseId);
        verifierPeriode(debut, fin);
        return projeter(ligneVenteRepository.lignesVendues(List.of(entrepriseId), debut, fin));
    }

    /**
     * {@inheritDoc}
     *
     * @throws BusinessException {@code SYS_BAD_REQUEST} si la liste de filiales
     *         est vide, ou si une borne de période est invalide
     */
    @Override
    @Transactional(readOnly = true)
    public List<LigneVenteAgregat> lignesVenduesGroupe(List<Long> entrepriseIds,
                                                       Instant debut, Instant fin) {
        if (entrepriseIds == null || entrepriseIds.isEmpty()
                || entrepriseIds.stream().anyMatch(id -> id == null)) {
            throw new BusinessException(ErrorCode.SYS_BAD_REQUEST,
                    "La liste des filiales est obligatoire pour une lecture groupe");
        }
        verifierPeriode(debut, fin);
        return projeter(ligneVenteRepository.lignesVendues(entrepriseIds, debut, fin));
    }

    private void verifierEntreprise(Long entrepriseId) {
        if (entrepriseId == null) {
            throw new BusinessException(ErrorCode.SYS_BAD_REQUEST,
                    "entrepriseId est obligatoire pour une lecture de ventes");
        }
    }

    /** Bornes obligatoires et ordonnées — la borne haute est exclusive. */
    private void verifierPeriode(Instant debut, Instant fin) {
        if (debut == null || fin == null) {
            throw new BusinessException(ErrorCode.SYS_BAD_REQUEST,
                    "Les bornes debut et fin sont obligatoires");
        }
        if (!debut.isBefore(fin)) {
            throw new BusinessException(ErrorCode.SYS_BAD_REQUEST,
                    "La borne debut doit strictement précéder la borne fin");
        }
    }

    /** [venteId, entrepriseId, articleId, libelle, clientId, clientNom, quantite, PU, TVA, date]. */
    private List<LigneVenteAgregat> projeter(List<Object[]> lignes) {
        return lignes.stream()
                .map(ligne -> new LigneVenteAgregat(
                        (Long) ligne[0],
                        (Long) ligne[1],
                        (Long) ligne[2],
                        (String) ligne[3],
                        (Long) ligne[4],
                        (String) ligne[5],
                        (BigDecimal) ligne[6],
                        ((Number) ligne[7]).intValue(),
                        (BigDecimal) ligne[8],
                        (Instant) ligne[9]))
                .toList();
    }
}
