package com.stockmaster.auth.service.impl;

import com.stockmaster.auth.config.JwtTokenProvider;
import com.stockmaster.auth.config.StockMasterPrincipal;
import com.stockmaster.auth.domain.entity.Entreprise;
import com.stockmaster.auth.domain.entity.TenantGroup;
import com.stockmaster.auth.domain.entity.Utilisateur;
import com.stockmaster.auth.domain.enums.PlanAbonnement;
import com.stockmaster.auth.domain.enums.RoleUtilisateur;
import com.stockmaster.auth.domain.enums.ScopeUtilisateur;
import com.stockmaster.auth.domain.enums.TypeEntreprise;
import com.stockmaster.auth.dto.request.InscriptionEntrepriseUniqueRequest;
import com.stockmaster.auth.dto.request.InscriptionGroupeRequest;
import com.stockmaster.auth.dto.request.LoginRequest;
import com.stockmaster.auth.dto.request.RefreshTokenRequest;
import com.stockmaster.auth.dto.response.InscriptionResponse;
import com.stockmaster.auth.dto.response.LoginResponse;
import com.stockmaster.auth.dto.response.RefreshTokenResponse;
import com.stockmaster.auth.event.InscriptionSuccessEvent;
import com.stockmaster.auth.mapper.AuthMapper;
import com.stockmaster.auth.repository.EntrepriseRepository;
import com.stockmaster.auth.repository.TenantGroupRepository;
import com.stockmaster.auth.repository.UtilisateurRepository;
import com.stockmaster.auth.service.AuthService;
import com.stockmaster.shared.config.JwtProperties;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String REFRESH_KEY_PREFIX = "refresh:";

    private final TenantGroupRepository tenantGroupRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthMapper authMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    // ========================================================================
    // US-006 — Inscription entreprise unique
    // ========================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InscriptionResponse inscrireEntrepriseUnique(InscriptionEntrepriseUniqueRequest request) {

        // Vérifier unicité email
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            log.warn("Tentative d'inscription avec un email existant: {}", request.getEmail());
            throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS,
                    "Cet email est déjà utilisé");
        }

        // Création atomique : TenantGroup + Entreprise + Utilisateur
        TenantGroup groupe = TenantGroup.builder()
                .nomGroupe(request.getNomBoutique())
                .planAbonnement(PlanAbonnement.GRATUIT)
                .limiteFiliales(1)
                .actif(true)
                .build();
        groupe = tenantGroupRepository.save(groupe);
        log.debug("TenantGroup créé: id={}", groupe.getId());

        Entreprise entreprise = authMapper.toEntreprise(request);
        entreprise.setGroupe(groupe);
        entreprise.setTypeEntreprise(TypeEntreprise.MERE);
        entreprise = entrepriseRepository.save(entreprise);
        log.debug("Entreprise créée: id={}", entreprise.getId());

        String motDePasseHash = passwordEncoder.encode(request.getMotDePasse());

        Utilisateur utilisateur = Utilisateur.builder()
                .entreprise(entreprise)
                .scope(ScopeUtilisateur.GROUPE)
                .role(RoleUtilisateur.ADMIN_GROUPE)
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .motDePasse(motDePasseHash)
                .actif(true)
                .build();
        utilisateur = utilisateurRepository.save(utilisateur);
        log.debug("Utilisateur ADMIN_GROUPE créé: id={}", utilisateur.getId());

        // Publication événement asynchrone (email de bienvenue)
        eventPublisher.publishEvent(new InscriptionSuccessEvent(
                this,
                utilisateur.getEmail(),
                utilisateur.getPrenom(),
                entreprise.getNom()
        ));

        log.info("Inscription réussie — email={}, groupeId={}", request.getEmail(), groupe.getId());

        return InscriptionResponse.builder()
                .email(request.getEmail())
                .groupId(groupe.getId())
                .message("Votre espace a été créé. Vérifiez votre email pour activer votre compte.")
                .build();
    }

    // ========================================================================
    // US-007 — Inscription groupe multi-sites
    // ========================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InscriptionResponse inscrireGroupe(InscriptionGroupeRequest request) {

        // Vérifier unicité email admin
        if (utilisateurRepository.existsByEmail(request.getEmailAdmin())) {
            log.warn("Tentative d'inscription groupe avec un email admin existant: {}", request.getEmailAdmin());
            throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS,
                    "Cet email est déjà utilisé");
        }

        // Création atomique : TenantGroup + Entreprise + Utilisateur
        TenantGroup groupe = TenantGroup.builder()
                .nomGroupe(request.getNomGroupe())
                .planAbonnement(PlanAbonnement.GRATUIT)
                .limiteFiliales(5)
                .actif(true)
                .build();
        groupe = tenantGroupRepository.save(groupe);
        log.debug("TenantGroup créé pour groupe multi-sites: id={}", groupe.getId());

        Entreprise entreprise = authMapper.toEntrepriseFromGroupe(request);
        entreprise.setGroupe(groupe);
        entreprise = entrepriseRepository.save(entreprise);
        log.debug("Entreprise (siège) créée: id={}", entreprise.getId());

        String motDePasseHash = passwordEncoder.encode(request.getMotDePasse());

        Utilisateur utilisateur = Utilisateur.builder()
                .entreprise(entreprise)
                .scope(ScopeUtilisateur.GROUPE)
                .role(RoleUtilisateur.ADMIN_GROUPE)
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmailAdmin())
                .motDePasse(motDePasseHash)
                .actif(true)
                .build();
        utilisateur = utilisateurRepository.save(utilisateur);
        log.debug("Utilisateur ADMIN_GROUPE créé pour groupe: id={}", utilisateur.getId());

        // Publication événement asynchrone (email de bienvenue)
        eventPublisher.publishEvent(new InscriptionSuccessEvent(
                this,
                utilisateur.getEmail(),
                utilisateur.getPrenom(),
                entreprise.getNom()
        ));

        log.info("Inscription groupe réussie — emailAdmin={}, groupeId={}", request.getEmailAdmin(), groupe.getId());

        return InscriptionResponse.builder()
                .email(request.getEmailAdmin())
                .groupId(groupe.getId())
                .message("Votre groupe a été créé. Créez votre première filiale depuis le tableau de bord.")
                .build();
    }

    // ========================================================================
    // US-008 — Connexion JWT
    // ========================================================================

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        // Recherche utilisateur par email
        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Tentative de connexion avec email inconnu: {}", request.getEmail());
                    return new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
                });

        // Vérification mot de passe
        if (!passwordEncoder.matches(request.getMotDePasse(), utilisateur.getMotDePasse())) {
            log.warn("Mot de passe incorrect pour: {}", request.getEmail());
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        // Vérification compte actif
        if (!Boolean.TRUE.equals(utilisateur.getActif())) {
            log.warn("Compte désactivé: {}", request.getEmail());
            throw new BusinessException(ErrorCode.AUTH_ACCOUNT_DISABLED);
        }

        // Vérification groupe actif
        TenantGroup groupe = utilisateur.getEntreprise().getGroupe();
        if (!Boolean.TRUE.equals(groupe.getActif())) {
            log.warn("Groupe suspendu: {}", groupe.getId());
            throw new BusinessException(ErrorCode.AUTH_TENANT_SUSPENDED);
        }

        // Génération tokens
        Long userId = utilisateur.getId();
        Long entrepriseId = utilisateur.getEntreprise().getId();
        Long groupId = groupe.getId();
        String role = utilisateur.getRole().name();
        String scope = utilisateur.getScope().name();

        String accessToken = jwtTokenProvider.generateAccessToken(
                userId, entrepriseId, groupId, role, scope);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

        // Stocker le refresh token dans Redis avec clé refresh:{userId}
        String redisKey = REFRESH_KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(redisKey, refreshToken,
                jwtProperties.getRefreshTokenExpiration(), TimeUnit.SECONDS);

        log.info("Connexion réussie — userId={}, role={}", userId, role);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtProperties.getAccessTokenExpiration())
                .role(role)
                .scope(scope)
                .build();
    }

    // ========================================================================
    // US-009 — Refresh token
    // ========================================================================

    // ========================================================================
    // US-010 — Déconnexion (révocation du refresh token)
    // ========================================================================

    @Override
    public void logout() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof StockMasterPrincipal principal)) {
            log.warn("Tentative de déconnexion sans authentification valide");
            throw new BusinessException(ErrorCode.AUTH_TOKEN_INVALID);
        }

        Long userId = principal.getUserId();
        String redisKey = REFRESH_KEY_PREFIX + userId;

        redisTemplate.delete(redisKey);
        log.info("Refresh token révoqué pour userId={}", userId);

        // Nettoyer le contexte de sécurité
        SecurityContextHolder.clearContext();
    }

    // ========================================================================
    // US-009 — Refresh token
    // ========================================================================

    @Override
    @Transactional(readOnly = true)
    public RefreshTokenResponse refreshAccessToken(RefreshTokenRequest request) {

        Long userId;
        try {
            userId = jwtTokenProvider.getUserIdFromToken(request.getRefreshToken());
        } catch (ExpiredJwtException e) {
            log.warn("Refresh token expiré");
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        } catch (JwtException e) {
            log.warn("Refresh token invalide: {}", e.getMessage());
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        // Vérifier que le refresh token existe dans Redis et correspond
        String redisKey = REFRESH_KEY_PREFIX + userId;
        String storedRefreshToken = redisTemplate.opsForValue().get(redisKey);

        if (storedRefreshToken == null) {
            log.warn("Refresh token non trouvé dans Redis pour userId={}", userId);
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        if (!storedRefreshToken.equals(request.getRefreshToken())) {
            log.warn("Refresh token ne correspond pas pour userId={}", userId);
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        // Charger l'utilisateur
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Utilisateur introuvable pour refresh token: userId={}", userId);
                    return new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
                });

        // Vérifications de sécurité
        if (!Boolean.TRUE.equals(utilisateur.getActif())) {
            log.warn("Compte désactivé lors du refresh: userId={}", userId);
            throw new BusinessException(ErrorCode.AUTH_ACCOUNT_DISABLED);
        }

        TenantGroup groupe = utilisateur.getEntreprise().getGroupe();
        if (!Boolean.TRUE.equals(groupe.getActif())) {
            log.warn("Groupe suspendu lors du refresh: groupId={}", groupe.getId());
            throw new BusinessException(ErrorCode.AUTH_TENANT_SUSPENDED);
        }

        // Générer un nouveau access token avec les mêmes claims
        Long entrepriseId = utilisateur.getEntreprise().getId();
        Long groupId = groupe.getId();
        String role = utilisateur.getRole().name();
        String scope = utilisateur.getScope().name();

        String newAccessToken = jwtTokenProvider.generateAccessToken(
                userId, entrepriseId, groupId, role, scope);

        log.info("Refresh token accepté — nouveau accessToken émis pour userId={}", userId);

        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .expiresIn(jwtProperties.getAccessTokenExpiration())
                .build();
    }
}
