package com.stockmaster.tiers.service;

import com.stockmaster.shared.config.PaginationProperties;
import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.shared.dto.response.PageResponse;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.repository.EntrepriseRepository;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.tiers.domain.entity.Fournisseur;
import com.stockmaster.tiers.dto.request.CreerFournisseurRequest;
import com.stockmaster.tiers.dto.request.ModifierFournisseurRequest;
import com.stockmaster.tiers.dto.response.FournisseurResponse;
import com.stockmaster.tiers.mapper.FournisseurMapper;
import com.stockmaster.tiers.repository.FournisseurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * US-036 — créer un fournisseur (EPIC 6, Sprint 5).
 *
 * <p><b>CA1 — RBAC</b> : seuls {@code RESP_ACHATS}, {@code ADMIN_FILIALE} et
 * {@code ADMIN_GROUPE}. Le {@code @PreAuthorize} porte le refus côté contrôleur ;
 * la garde ci-dessous est la défense en profondeur (fail-fast, avant toute I/O)
 * au même titre que {@code CategorieService} / {@code EmployeService}.</p>
 *
 * <p><b>CA2 — isolation tenant</b> : le fournisseur est rattaché à
 * l'{@code entreprise_id} extrait du JWT, <b>jamais</b> lu du corps de la
 * requête (le DTO n'expose aucun champ tenant). L'agrégat tenant existant de
 * {@code stockmaster-shared} ({@code Entreprise} + {@code EntrepriseRepository})
 * fait foi : entreprise introuvable, soft-supprimée ou hors groupe du JWT →
 * {@code 404 RES_001} uniforme (jamais révéler l'existence).</p>
 *
 * <p><b>CA3 — validation</b> : portée par Jakarta Validation sur
 * {@link CreerFournisseurRequest} (raisonSociale obligatoire ; telephone au
 * format E.164 si fourni) → {@code 400 SYS_005} via le
 * {@code GlobalExceptionHandler} de {@code stockmaster-shared}.</p>
 *
 * <p><b>US-037</b> — {@link #lister} : listing paginé + recherche par nom ou
 * ville, strictement rattaché à l'entreprise du JWT.</p>
 *
 * <p><b>US-038</b> — {@link #modifier} : mise à jour des coordonnées, PATCH
 * sémantique, 404 uniforme hors tenant.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FournisseurService {

    /** Rôles autorisés à créer un fournisseur (CA1 — BACKLOG US-036). */
    private static final Set<String> ROLES_AUTORISES = Set.of(
            "RESP_ACHATS", "ADMIN_FILIALE", "ADMIN_GROUPE");

    private final FournisseurRepository fournisseurRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final FournisseurMapper fournisseurMapper;
    private final PaginationProperties paginationProperties;

    /**
     * Crée un fournisseur dans l'entreprise du JWT.
     *
     * @throws BusinessException {@code SEC_001} si le rôle n'est pas autorisé ou
     *         si le JWT ne porte pas d'{@code entrepriseId} ; {@code GRP_002} si
     *         le JWT ne porte pas de {@code groupId} ; {@code RES_001} si
     *         l'entreprise est introuvable, supprimée ou hors groupe.
     */
    @Transactional
    public FournisseurResponse creer(StockMasterPrincipal principal, CreerFournisseurRequest request) {
        verifierRole(principal);

        Entreprise entreprise = chargerEntrepriseDuPrincipal(principal);

        Fournisseur fournisseur = fournisseurMapper.toEntity(request);
        fournisseur.setEntreprise(entreprise);
        Fournisseur cree = fournisseurRepository.save(fournisseur);

        log.info("US-036 : fournisseur id={} ({}) créé pour l'entreprise {} par l'utilisateur {}",
                cree.getId(), cree.getRaisonSociale(), entreprise.getId(), principal.getUserId());

        return fournisseurMapper.toResponse(cree);
    }

    // ─── US-037 — lister / rechercher ─────────────────────────────

    /**
     * US-037 — liste paginée des fournisseurs de l'entreprise du JWT,
     * avec recherche optionnelle par nom ou ville.
     *
     * <p><b>Isolation tenant (règle n°1)</b> : aucun identifiant de tenant en
     * entrée — l'{@code entreprise_id} vient du JWT et porte la requête
     * repository ; l'agrégat {@code Entreprise} est d'abord validé (miroir
     * US-036) : introuvable, soft-supprimée ou hors groupe → {@code 404 RES_001}.
     * Un {@code entrepriseId} ajouté à la query string est ignoré.</p>
     *
     * <p><b>Arbitrages validés</b> : tri fixe {@code raisonSociale ASC} (pas de
     * tri dynamique) ; {@code size} borné à
     * {@code stockmaster.pagination.max-page-size} (100) côté service — le repo
     * borne silencieusement, jamais de 400 (convention {@code FilialeService}/
     * {@code ArticleService}) ; {@code search} vide ou blanc → listing complet.</p>
     */
    @Transactional(readOnly = true)
    public PageResponse<FournisseurResponse> lister(StockMasterPrincipal principal,
                                                    String search, int page, int size) {
        verifierRole(principal);
        Entreprise entreprise = chargerEntrepriseDuPrincipal(principal);
        Long entrepriseId = entreprise.getId();

        Pageable pageable = PageRequest.of(Math.max(0, page), borneTaille(size),
                Sort.by(Sort.Direction.ASC, "raisonSociale"));

        Page<Fournisseur> fournisseurs = (search == null || search.isBlank())
                ? fournisseurRepository.findAllByEntrepriseIdAndSupprimeFalse(entrepriseId, pageable)
                : fournisseurRepository.rechercherParEntrepriseEtTexte(
                        entrepriseId, search.trim(), pageable);

        PageResponse<FournisseurResponse> reponse =
                PageResponse.from(fournisseurs.map(fournisseurMapper::toResponse));

        log.info("US-037 : {} fournisseur(s) sur entreprise {} (page {}/{}, search={})",
                reponse.getTotalElements(), entrepriseId, reponse.getPage() + 1,
                Math.max(reponse.getTotalPages(), 1),
                (search == null || search.isBlank()) ? "<absent>" : search.trim());

        return reponse;
    }

    /**
     * US-038 — mise à jour des coordonnées d'un fournisseur du tenant du JWT.
     *
     * <p><b>Sémantique PATCH (arbitrage 1.a)</b> — convention {@code US-014}/
     * {@code US-018}/{@code US-024} sur un endpoint {@code PUT} : un champ absent
     * ou blanc laisse la valeur actuelle en place, un corps vide ne change rien.</p>
     *
     * <p><b>Isolation tenant (règle n°1, arbitrages 1.e/1.f/1.h)</b> : le
     * fournisseur est cherché par son id <b>puis</b> filtré sur
     * {@code entreprise_id} du JWT — jamais de lookup par id seul. Les trois cas
     * « id inconnu », « soft-supprimé » et « autre tenant » renvoient le
     * <b>même {@code 404 RES_001}</b> : l'existence d'un fournisseur étranger
     * n'est jamais révélée (miroir {@code FilialeService}/{@code ArticleService}).</p>
     *
     * <p><b>Aucun champ tenant dans le DTO</b> : {@code entreprise_id} n'est ni
     * lu ni modifiable, {@code id} est dans le chemin — les setters de l'entité
     * ne sont donc jamais atteints par le payload.</p>
     *
     * @throws BusinessException {@code SEC_001} si le rôle n'est pas autorisé ou
     *         si le JWT ne porte pas d'{@code entrepriseId} ; {@code GRP_002} si
     *         le JWT ne porte pas de {@code groupId} ; {@code RES_001} uniforme si
     *         l'entreprise ou le fournisseur est introuvable, supprimé ou hors
     *         périmètre.
     */
    @Transactional
    public FournisseurResponse modifier(StockMasterPrincipal principal, Long id,
                                        ModifierFournisseurRequest request) {
        verifierRole(principal);
        Entreprise entreprise = chargerEntrepriseDuPrincipal(principal);

        Fournisseur fournisseur = fournisseurRepository.findByIdAndSupprimeFalse(id)
                .filter(f -> f.getEntreprise() != null
                        && entreprise.getId().equals(f.getEntreprise().getId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND));

        appliquerModifications(fournisseur, request);
        Fournisseur misAJour = fournisseurRepository.save(fournisseur);

        log.info("US-038 : fournisseur id={} ({}) modifié pour l'entreprise {} par l'utilisateur {}",
                misAJour.getId(), misAJour.getRaisonSociale(), entreprise.getId(), principal.getUserId());

        return fournisseurMapper.toResponse(misAJour);
    }

    /**
     * PATCH sémantique — seule une valeur non nulle et non blanche est appliquée.
     * {@code entreprise}, {@code id} et {@code supprime} ne sont jamais touchés.
     */
    private void appliquerModifications(Fournisseur fournisseur, ModifierFournisseurRequest request) {
        if (StringUtils.hasText(request.getRaisonSociale())) {
            fournisseur.setRaisonSociale(request.getRaisonSociale());
        }
        if (StringUtils.hasText(request.getNif())) {
            fournisseur.setNif(request.getNif());
        }
        if (StringUtils.hasText(request.getContact())) {
            fournisseur.setContact(request.getContact());
        }
        if (StringUtils.hasText(request.getTelephone())) {
            fournisseur.setTelephone(request.getTelephone());
        }
        if (StringUtils.hasText(request.getEmail())) {
            fournisseur.setEmail(request.getEmail());
        }
        if (StringUtils.hasText(request.getAdresseVille())) {
            fournisseur.setAdresseVille(request.getAdresseVille());
        }
    }

    /** Bornes de pagination — convention repo : clamp silencieux, pas de 400. */
    private int borneTaille(int size) {
        return Math.min(Math.max(1, size), paginationProperties.getMaxPageSize());
    }

    // ─── helpers privés ──────────────────────────────────────────────────────

    /**
     * CA1 — garde fail-fast, avant toute requête en base (défense en profondeur
     * derrière le {@code @PreAuthorize} du contrôleur).
     */
    private void verifierRole(StockMasterPrincipal principal) {
        String role = principal != null ? principal.getRole() : null;
        if (role == null || role.isBlank() || !ROLES_AUTORISES.contains(role)) {
            throw new BusinessException(ErrorCode.SEC_ACCESS_DENIED);
        }
    }

    /**
     * Charge l'entreprise portée par le JWT avec isolation groupe (agrégat
     * tenant de {@code stockmaster-shared}). {@code 404} uniforme si
     * introuvable, soft-supprimée ou rattachée à un autre groupe.
     */
    private Entreprise chargerEntrepriseDuPrincipal(StockMasterPrincipal principal) {
        Long groupId = principal.getGroupId();
        if (groupId == null) {
            throw new BusinessException(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
        }
        Long entrepriseId = principal.getEntrepriseId();
        if (entrepriseId == null) {
            throw new BusinessException(ErrorCode.SEC_ACCESS_DENIED);
        }
        return entrepriseRepository.findById(entrepriseId)
                .filter(e -> !Boolean.TRUE.equals(e.getSupprime()))
                .filter(e -> e.getGroupe() != null && groupId.equals(e.getGroupe().getId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND));
    }
}
