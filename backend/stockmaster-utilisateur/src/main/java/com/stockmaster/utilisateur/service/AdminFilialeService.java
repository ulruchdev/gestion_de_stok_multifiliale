package com.stockmaster.utilisateur.service;

import com.stockmaster.notification.port.CanalNotification;
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
import com.stockmaster.utilisateur.dto.request.AdminFilialeCreateRequest;
import com.stockmaster.utilisateur.dto.response.UtilisateurAdminResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

/**
 * US-021 — créer un Admin Filiale. Le service porte :
 *
 * <ul>
 *   <li><b>isolation multi-tenant</b> : la filiale cible doit appartenir au groupe
 *       du JWT — {@code 404 RES_ENTITY_NOT_FOUND} uniforme si elle n'existe pas, est
 *       soft-supprimée, appartient à un autre groupe ou n'est pas de type FILIALE
 *       (jamais révéler l'existence — même critère que US-018/019) ;</li>
 *   <li><b>rôle forcé</b> {@code ADMIN_FILIALE}, scope {@code FILIALE} — jamais lu
 *       de la requête (critère d'acceptation explicite) ;</li>
 *   <li><b>email unique plateforme</b> → {@code 409 AUTH_008} (unicité DB miroir) ;</li>
 *   <li><b>US-101</b> : limite d'utilisateurs du plan lue de
 *       {@code tenant_group.limite_utilisateurs} (jamais codée en dur), plan expiré
 *       dégradé GRATUIT (10), dépassement → {@code 403 USR_001} ;</li>
 *   <li><b>invitation</b> : token UUID stocké en Redis ({@code invitation:{token}} →
 *       userId, TTL 48 h) et envoyé par le canal EMAIL (port DEC-014). Le compte
 *       naît {@code actif=false, emailVerifie=false} avec un mot de passe aléatoire
 *       haché (aucune connexion possible avant l'activation de US-075) ;</li>
 *   <li><b>échec email toléré</b> : l'envoi passe par le canal asynchrone qui avale
 *       ses erreurs — le compte est créé même si l'email échoue (philosophie US-006).</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminFilialeService {

    static final String INVITATION_KEY_PREFIX = "invitation:";
    static final Duration INVITATION_TTL = Duration.ofHours(48);

    private final EntrepriseRepository entrepriseRepository;
    private final TenantGroupRepository groupeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CanalNotification canalEmail;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final ControleLimiteUtilisateursService controleLimiteUtilisateurs;

    /**
     * Crée le compte Admin Filiale et déclenche l'invitation.
     *
     * @throws BusinessException {@code GRP_CROSS_GROUP_FORBIDDEN} si le principal ne
     *         porte pas de groupId ; {@code RES_ENTITY_NOT_FOUND} si la filiale
     *         n'existe pas / est supprimée / est hors groupe / n'est pas une filiale ;
     *         {@code AUTH_EMAIL_ALREADY_EXISTS} si l'email existe (plateforme) ;
     *         {@code USR_USER_LIMIT_REACHED} si la limite du plan (DEC-015, US-101) est atteinte.
     */
    @Transactional
    public UtilisateurAdminResponse creer(StockMasterPrincipal principal, AdminFilialeCreateRequest request) {
        Long groupId = groupIdDuPrincipal(principal);
        Entreprise filiale = chargerFilialeDuGroupe(groupId, request.getFilialeId());

        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);
        }

        TenantGroup groupe = groupeRepository.findById(groupId)
                .filter(g -> !Boolean.TRUE.equals(g.getSupprime()))
                .orElseThrow(() -> new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND));
        verifierLimiteUtilisateurs(groupe);

        Utilisateur compte = Utilisateur.builder()
                .entreprise(filiale)
                .scope(ScopeUtilisateur.FILIALE)
                .role(RoleUtilisateur.ADMIN_FILIALE)
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                // mot de passe aléatoire : aucune connexion possible avant activation (US-075)
                .motDePasse(passwordEncoder.encode(motDePasseAleatoire()))
                .actif(false)
                .emailVerifie(false)
                .build();
        Utilisateur cree = utilisateurRepository.save(compte);

        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(INVITATION_KEY_PREFIX + token, String.valueOf(cree.getId()), INVITATION_TTL);

        envoyerInvitation(filiale, cree, token);

        log.info("US-021 : Admin Filiale créé id={} (email={}) pour la filiale {} du groupe {}",
                cree.getId(), cree.getEmail(), filiale.getId(), groupId);
        return UtilisateurAdminResponse.builder()
                .id(cree.getId())
                .email(cree.getEmail())
                .prenom(cree.getPrenom())
                .nom(cree.getNom())
                .role(cree.getRole())
                .actif(cree.getActif())
                .filialeId(filiale.getId())
                .build();
    }

    private void verifierLimiteUtilisateurs(TenantGroup groupe) {
        boolean planExpire = groupe.getDateExpirationPlan() != null
                && groupe.getDateExpirationPlan().isBefore(LocalDate.now());
        Integer limiteEffective = controleLimiteUtilisateurs.limiteUtilisateursEffective(
                groupe.getLimiteUtilisateurs(), planExpire);
        long utilisateursActifs = utilisateurRepository
                .countByEntrepriseGroupeIdAndActifTrueAndSupprimeFalse(groupe.getId());
        if (!controleLimiteUtilisateurs.peutAjouterUtilisateur(utilisateursActifs, limiteEffective)) {
            throw new BusinessException(ErrorCode.USR_USER_LIMIT_REACHED);
        }
    }

    private void envoyerInvitation(Entreprise filiale, Utilisateur cree, String token) {
        String sujet = "Invitation StockMaster — " + filiale.getNom();
        String corps = """
                Bonjour %s %s,

                Vous avez été désigné(e) administrateur (ADMIN_FILIALE) de « %s ».

                Pour activer votre compte et définir votre mot de passe, utilisez ce token \
                sous 48 heures : %s

                L'équipe StockMaster CM""".formatted(cree.getPrenom(), cree.getNom(), filiale.getNom(), token);
        // canal asynchrone, échecs avalés côté canal — mais le contrat reste tolérant
        // aux canaux synchrones (in-app) : l'invitation ne bloque jamais la création
        try {
            canalEmail.envoyer(filiale.getId(), cree.getId(), "INVITATION", sujet, corps);
        } catch (RuntimeException e) {
            log.warn("Invitation non envoyée pour le compte {} (créé malgré tout) : {}",
                    cree.getId(), e.getMessage());
        }
    }

    private String motDePasseAleatoire() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        return UUID.nameUUIDFromBytes(bytes).toString();
    }

    private Long groupIdDuPrincipal(StockMasterPrincipal principal) {
        Long groupId = principal != null ? principal.getGroupId() : null;
        if (groupId == null) {
            throw new BusinessException(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
        }
        return groupId;
    }

    /** Isolation : 404 uniforme si hors groupe, supprimée, inexistante ou type != FILIALE. */
    private Entreprise chargerFilialeDuGroupe(Long groupId, Long filialeId) {
        return entrepriseRepository.findById(filialeId)
                .filter(e -> !Boolean.TRUE.equals(e.getSupprime()))
                .filter(e -> e.getTypeEntreprise() == TypeEntreprise.FILIALE)
                .filter(e -> e.getGroupe() != null && groupId.equals(e.getGroupe().getId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND));
    }
}
