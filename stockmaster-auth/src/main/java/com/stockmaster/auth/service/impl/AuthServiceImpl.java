package com.stockmaster.auth.service.impl;

import com.stockmaster.auth.config.JwtTokenProvider;
import com.stockmaster.auth.domain.entity.Entreprise;
import com.stockmaster.auth.domain.entity.TenantGroup;
import com.stockmaster.auth.domain.entity.Utilisateur;
import com.stockmaster.auth.domain.enums.PlanAbonnement;
import com.stockmaster.auth.domain.enums.RoleUtilisateur;
import com.stockmaster.auth.domain.enums.ScopeUtilisateur;
import com.stockmaster.auth.domain.enums.TypeEntreprise;
import com.stockmaster.auth.dto.request.InscriptionEntrepriseUniqueRequest;
import com.stockmaster.auth.dto.request.LoginRequest;
import com.stockmaster.auth.dto.response.InscriptionResponse;
import com.stockmaster.auth.dto.response.LoginResponse;
import com.stockmaster.auth.event.InscriptionSuccessEvent;
import com.stockmaster.auth.mapper.AuthMapper;
import com.stockmaster.auth.repository.EntrepriseRepository;
import com.stockmaster.auth.repository.TenantGroupRepository;
import com.stockmaster.auth.repository.UtilisateurRepository;
import com.stockmaster.auth.service.AuthService;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final TenantGroupRepository tenantGroupRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthMapper authMapper;
    private final ApplicationEventPublisher eventPublisher;

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

        log.info("Connexion réussie — userId={}, role={}", userId, role);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(900)
                .role(role)
                .scope(scope)
                .build();
    }
}
