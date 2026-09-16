package com.stockmaster.utilisateur.service;

import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.shared.domain.entity.TenantGroup;
import com.stockmaster.shared.domain.entity.Utilisateur;
import com.stockmaster.shared.domain.enums.RoleUtilisateur;
import com.stockmaster.shared.domain.enums.ScopeUtilisateur;
import com.stockmaster.shared.domain.enums.TypeEntreprise;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.repository.EntrepriseRepository;
import com.stockmaster.shared.repository.TenantGroupRepository;
import com.stockmaster.shared.repository.UtilisateurRepository;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.utilisateur.dto.request.CreerEmployeRequest;
import com.stockmaster.utilisateur.dto.response.EmployeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Set;

/**
 * US-022 — créer un employé. Porte :
 *
 * <ul>
 *   <li><b>isolation multi-tenant</b> : {@code entreprise_id} depuis le JWT
 *       (jamais du corps de la requête — DEC-019) ; l'Admin Filiale crée dans
 *       <em>sa</em> filiale ; l'Admin Groupe passe par son {@code entrepriseId}
 *       du JWT (s'il veut créer dans une autre filiale il passera par un
 *       endpoint avec contexte explicite — US-018 pattern) ;</li>
 *   <li><b>rôle limité</b> : seuls {@code GESTIONNAIRE_STOCK}, {@code RESP_ACHATS},
 *       {@code COMMERCIAL} et {@code CAISSIER} sont autorisés — jamais {@code ADMIN_*}
 *       ({@code 403 SEC_ACCESS_DENIED}) ;</li>
 *   <li><b>email unique plateforme</b> → {@code 409 AUTH_008} (DEC-008) ;</li>
 *   <li><b>US-101 (DEC-015)</b> : limite {@code limite_utilisateurs} lue du plan, jamais
 *       codée en dur, plan expiré dégradé GRATUIT (10) ;</li>
 *   <li><b>Option A</b> : compte créé {@code actif=true, emailVerifie=true}, mot de
 *       passe provisoire haché BCrypt et remis en main propre — connexion immédiate
 *       (usage opérationnel terrain camerounais, cohérent avec DEC-016 qui n'impose
 *       le token d'activation que pour l'inscription publique).</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeService {

    /** Rôles autorisés pour un employé — jamais un rôle Admin. */
    private static final Set<RoleUtilisateur> ROLES_EMPLOYE = Set.of(
            RoleUtilisateur.GESTIONNAIRE_STOCK,
            RoleUtilisateur.RESP_ACHATS,
            RoleUtilisateur.COMMERCIAL,
            RoleUtilisateur.CAISSIER
    );

    private final EntrepriseRepository entrepriseRepository;
    private final TenantGroupRepository groupeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final ControleLimiteUtilisateursService controleLimiteUtilisateurs;
    private final PerimetreAdminGuard perimetreAdminGuard;

    /**
     * Crée le compte employé avec un mot de passe provisoire (Option A).
     *
     * @throws BusinessException {@code GRP_CROSS_GROUP_FORBIDDEN} si le principal
     *         ne porte pas de groupId ; {@code RES_ENTITY_NOT_FOUND} si l'entreprise
     *         du JWT est introuvable ou supprimée ; {@code SEC_ACCESS_DENIED} si le
     *         rôle demandé est un rôle Admin ; {@code AUTH_EMAIL_ALREADY_EXISTS} si
     *         l'email existe déjà ; {@code USR_USER_LIMIT_REACHED} si la limite
     *         du plan est atteinte.
     */
    @Transactional
    public EmployeResponse creer(StockMasterPrincipal principal, CreerEmployeRequest request) {
        Long groupId = perimetreAdminGuard.groupIdDuPrincipal(principal);

        // Rôle guard — avant toute requête en base (fail-fast, pas d'I/O inutile)
        if (!ROLES_EMPLOYE.contains(request.getRole())) {
            throw new BusinessException(ErrorCode.SEC_ACCESS_DENIED);
        }

        // Isolation : l'employé est créé dans l'entreprise du JWT
        Entreprise entreprise = chargerEntrepriseDuPrincipal(principal.getEntrepriseId(), groupId);

        // Unicité email plateforme (DEC-008)
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);
        }

        // US-101 — limite utilisateurs du plan (DEC-015)
        TenantGroup groupe = groupeRepository.findById(groupId)
                .filter(g -> !Boolean.TRUE.equals(g.getSupprime()))
                .orElseThrow(() -> new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND));
        verifierLimiteUtilisateurs(groupe);

        // Création du compte — Option A : actif dès la création, mot de passe provisoire haché
        Utilisateur employe = Utilisateur.builder()
                .entreprise(entreprise)
                .scope(ScopeUtilisateur.FILIALE)
                .role(request.getRole())
                .prenom(request.getPrenom())
                .nom(request.getNom())
                .email(request.getEmail())
                .motDePasse(passwordEncoder.encode(request.getMotDePasseProvisoire()))
                .actif(true)
                .emailVerifie(true)   // connexion immédiate — Option A, remise en main propre
                .build();

        Utilisateur cree = utilisateurRepository.save(employe);

        log.info("US-022 : employé créé id={} role={} (email={}) pour l'entreprise {} du groupe {}",
                cree.getId(), cree.getRole(), cree.getEmail(), entreprise.getId(), groupId);

        return EmployeResponse.builder()
                .id(cree.getId())
                .email(cree.getEmail())
                .prenom(cree.getPrenom())
                .nom(cree.getNom())
                .role(cree.getRole())
                .actif(cree.getActif())
                .entrepriseId(entreprise.getId())
                .build();
    }

    // ─── helpers privés ─────────────────────────────────────────────────────

    private void verifierLimiteUtilisateurs(TenantGroup groupe) {
        boolean planExpire = groupe.getDateExpirationPlan() != null
                && groupe.getDateExpirationPlan().isBefore(LocalDate.now(Clock.systemDefaultZone()));
        Integer limiteEffective = controleLimiteUtilisateurs.limiteUtilisateursEffective(
                groupe.getLimiteUtilisateurs(), planExpire);
        long utilisateursActifs = utilisateurRepository
                .countByEntrepriseGroupeIdAndActifTrueAndSupprimeFalse(groupe.getId());
        if (!controleLimiteUtilisateurs.peutAjouterUtilisateur(utilisateursActifs, limiteEffective)) {
            throw new BusinessException(ErrorCode.USR_USER_LIMIT_REACHED);
        }
    }

    /**
     * Charge l'entreprise du principal avec isolation groupe.
     * {@code 404} uniforme si introuvable, supprimée ou hors groupe.
     */
    private Entreprise chargerEntrepriseDuPrincipal(Long entrepriseId, Long groupId) {
        return entrepriseRepository.findById(entrepriseId)
                .filter(e -> !Boolean.TRUE.equals(e.getSupprime()))
                .filter(e -> e.getGroupe() != null && groupId.equals(e.getGroupe().getId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND));
    }
}
