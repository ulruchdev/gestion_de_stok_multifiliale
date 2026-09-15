package com.stockmaster.groupe.service;

import com.stockmaster.groupe.dto.request.FilialeCreateRequest;
import com.stockmaster.groupe.dto.request.FilialeStatutRequest;
import com.stockmaster.groupe.dto.request.FilialeUpdateRequest;
import com.stockmaster.groupe.dto.response.FilialeResponse;
import com.stockmaster.shared.config.PaginationProperties;
import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.shared.domain.entity.TenantGroup;
import com.stockmaster.shared.domain.enums.TypeEntreprise;
import com.stockmaster.shared.dto.response.PageResponse;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.repository.EntrepriseRepository;
import com.stockmaster.shared.repository.TenantGroupRepository;
import com.stockmaster.shared.repository.UtilisateurRepository;
import com.stockmaster.shared.security.StockMasterPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * US-016 — Créer une filiale dans le groupe (P0).
 * US-017 — Lister les filiales du groupe, paginé (P0).
 * US-018 — Modifier une filiale (P0).
 * US-019 — Activer / désactiver une filiale (P1).
 *
 * <p>Isolation multi-tenant structurelle : les filiales manipulées sont toujours
 * celles du groupe du JWT ({@code groupId} du principal) — aucun {@code groupId}
 * n'est accepté en entrée.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class FilialeService {

    private final TenantGroupRepository groupeRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ControleLimiteFilialesService controleLimiteFilialesService;
    private final PaginationProperties paginationProperties;

    /**
     * Crée une filiale dans le groupe du principal connecté.
     *
     * @throws BusinessException {@code GRP_CROSS_GROUP_FORBIDDEN} si le principal
     *         ne porte pas de {@code groupId} ;
     *         {@code RES_ENTITY_NOT_FOUND} si le groupe n'existe pas ou est soft-supprimé ;
     *         {@code GRP_FILIALE_LIMIT_REACHED} si la limite du plan (DEC-015) est atteinte ;
     *         {@code RES_DUPLICATE_FILIALE_CODE} si {@code codeFiliale} existe déjà dans le groupe ;
     *         {@code SYS_INTERNAL_ERROR} si la maison mère du groupe est introuvable (invariant violé).
     */
    public FilialeResponse creer(StockMasterPrincipal principal, FilialeCreateRequest request) {
        Long groupId = groupIdDuPrincipal(principal);

        TenantGroup groupe = groupeRepository.findById(groupId)
                .filter(g -> !Boolean.TRUE.equals(g.getSupprime()))
                .orElseThrow(() -> new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND));

        long sitesOperationnels = entrepriseRepository
                .countByGroupeIdAndSiteOperationnelTrueAndActifTrueAndSupprimeFalse(groupId);
        if (!controleLimiteFilialesService.peutAjouterFiliale(sitesOperationnels, groupe.getLimiteFiliales())) {
            throw new BusinessException(ErrorCode.GRP_FILIALE_LIMIT_REACHED);
        }

        if (entrepriseRepository.existsByGroupeIdAndCodeFiliale(groupId, request.getCodeFiliale())) {
            throw new BusinessException(ErrorCode.RES_DUPLICATE_FILIALE_CODE);
        }

        Entreprise maisonMere = entrepriseRepository.findFirstByGroupeIdAndTypeEntreprise(groupId, TypeEntreprise.MERE)
                .orElseThrow(() -> new BusinessException(ErrorCode.SYS_INTERNAL_ERROR,
                        "Maison mère introuvable pour le groupe " + groupId));

        Entreprise filiale = Entreprise.builder()
                .groupe(groupe)
                .parent(maisonMere)
                .typeEntreprise(TypeEntreprise.FILIALE)
                .nom(request.getNom())
                .codeFiliale(request.getCodeFiliale())
                .adresseVille(request.getVille())
                .adresseQuartier(request.getQuartier())
                .build();

        Entreprise sauvegardee = entrepriseRepository.save(filiale);

        return toResponse(sauvegardee, maisonMere.getId(), 0L);
    }

    /**
     * Liste les filiales du groupe du principal connecté, paginées, avec filtres
     * {@code actif}/{@code ville} optionnels.
     *
     * <p>La maison mère du groupe est résolue une seule fois (même parent pour
     * toutes les filiales de la page) plutôt que de traverser l'association
     * paresseuse {@code Entreprise.parent} pour chaque ligne.</p>
     *
     * @throws BusinessException {@code GRP_CROSS_GROUP_FORBIDDEN} si le principal
     *         ne porte pas de {@code groupId}
     */
    @Transactional(readOnly = true)
    public PageResponse<FilialeResponse> lister(StockMasterPrincipal principal, Boolean actif, String ville,
            Pageable pageable) {
        Long groupId = groupIdDuPrincipal(principal);

        Long parentId = entrepriseRepository.findFirstByGroupeIdAndTypeEntreprise(groupId, TypeEntreprise.MERE)
                .map(Entreprise::getId)
                .orElse(null);

        Page<Entreprise> filiales = entrepriseRepository.findFilialesDuGroupe(
                groupId, TypeEntreprise.FILIALE, actif, ville, bornerTaillePage(pageable));

        Page<FilialeResponse> reponse = filiales.map(filiale -> toResponse(filiale, parentId,
                utilisateurRepository.countByEntrepriseIdAndSupprimeFalse(filiale.getId())));

        return PageResponse.from(reponse);
    }

    /**
     * Modifie partiellement (sémantique PATCH) une filiale du groupe du principal connecté.
     *
     * @throws BusinessException {@code GRP_CROSS_GROUP_FORBIDDEN} si le principal
     *         ne porte pas de {@code groupId} ;
     *         {@code RES_ENTITY_NOT_FOUND} si la filiale n'existe pas, est soft-supprimée,
     *         n'est pas de type FILIALE, ou appartient à un autre groupe (jamais révéler
     *         l'existence — critère d'acceptation US-018) ;
     *         {@code RES_DUPLICATE_FILIALE_CODE} si le nouveau {@code codeFiliale} est
     *         déjà pris par une autre filiale du groupe.
     */
    @Transactional
    public FilialeResponse modifier(StockMasterPrincipal principal, Long filialeId, FilialeUpdateRequest request) {
        Long groupId = groupIdDuPrincipal(principal);
        Entreprise filiale = chargerFilialeDuGroupe(groupId, filialeId);

        if (StringUtils.hasText(request.getCodeFiliale())) {
            if (entrepriseRepository.existsByGroupeIdAndCodeFilialeAndIdNot(groupId, request.getCodeFiliale(), filialeId)) {
                throw new BusinessException(ErrorCode.RES_DUPLICATE_FILIALE_CODE);
            }
            filiale.setCodeFiliale(request.getCodeFiliale());
        }
        if (StringUtils.hasText(request.getNom())) {
            filiale.setNom(request.getNom());
        }
        if (StringUtils.hasText(request.getVille())) {
            filiale.setAdresseVille(request.getVille());
        }
        if (StringUtils.hasText(request.getQuartier())) {
            filiale.setAdresseQuartier(request.getQuartier());
        }

        Entreprise sauvegardee = entrepriseRepository.save(filiale);
        return toResponseAvecDetails(sauvegardee, groupId);
    }

    /**
     * Active ou désactive une filiale du groupe du principal connecté (US-019).
     * Ne touche à aucune autre donnée — les mouvements de stock/commandes n'existent
     * pas encore (EPICs 5-6) ; c'est à ces modules de refuser toute écriture sur un
     * site {@code actif = false} une fois construits.
     *
     * @throws BusinessException mêmes codes d'erreur que {@link #modifier}
     *         (isolation groupe identique, sans vérification de doublon)
     */
    @Transactional
    public FilialeResponse changerStatut(StockMasterPrincipal principal, Long filialeId, FilialeStatutRequest request) {
        Long groupId = groupIdDuPrincipal(principal);
        Entreprise filiale = chargerFilialeDuGroupe(groupId, filialeId);

        filiale.setActif(request.getActif());

        Entreprise sauvegardee = entrepriseRepository.save(filiale);
        return toResponseAvecDetails(sauvegardee, groupId);
    }

    private Long groupIdDuPrincipal(StockMasterPrincipal principal) {
        Long groupId = principal != null ? principal.getGroupId() : null;
        if (groupId == null) {
            throw new BusinessException(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
        }
        return groupId;
    }

    /**
     * Charge une filiale et vérifie son appartenance au groupe — {@code 404} uniforme
     * (jamais révéler l'existence) si elle n'existe pas, est soft-supprimée, n'est pas
     * de type FILIALE, ou appartient à un autre groupe.
     */
    private Entreprise chargerFilialeDuGroupe(Long groupId, Long filialeId) {
        return entrepriseRepository.findById(filialeId)
                .filter(e -> !Boolean.TRUE.equals(e.getSupprime()))
                .filter(e -> e.getTypeEntreprise() == TypeEntreprise.FILIALE)
                .filter(e -> e.getGroupe().getId().equals(groupId))
                .orElseThrow(() -> new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND));
    }

    private FilialeResponse toResponseAvecDetails(Entreprise filiale, Long groupId) {
        Long parentId = entrepriseRepository.findFirstByGroupeIdAndTypeEntreprise(groupId, TypeEntreprise.MERE)
                .map(Entreprise::getId)
                .orElse(null);
        long nombreEmployes = utilisateurRepository.countByEntrepriseIdAndSupprimeFalse(filiale.getId());
        return toResponse(filiale, parentId, nombreEmployes);
    }

    /** Protection contre les abus (DEC absente sur ce point) : jamais plus que {@code stockmaster.pagination.max-page-size}. */
    private Pageable bornerTaillePage(Pageable pageable) {
        int taille = Math.min(pageable.getPageSize(), paginationProperties.getMaxPageSize());
        return PageRequest.of(pageable.getPageNumber(), taille, pageable.getSort());
    }

    private FilialeResponse toResponse(Entreprise filiale, Long parentId, long nombreEmployes) {
        return FilialeResponse.builder()
                .id(filiale.getId())
                .nom(filiale.getNom())
                .codeFiliale(filiale.getCodeFiliale())
                .ville(filiale.getAdresseVille())
                .quartier(filiale.getAdresseQuartier())
                .actif(filiale.getActif())
                .siteOperationnel(filiale.getSiteOperationnel())
                .parentId(parentId)
                .nombreEmployes(nombreEmployes)
                .build();
    }
}
