package com.stockmaster.tiers.service;

import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.repository.EntrepriseRepository;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.tiers.domain.entity.Fournisseur;
import com.stockmaster.tiers.dto.request.CreerFournisseurRequest;
import com.stockmaster.tiers.dto.response.FournisseurResponse;
import com.stockmaster.tiers.mapper.FournisseurMapper;
import com.stockmaster.tiers.repository.FournisseurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
