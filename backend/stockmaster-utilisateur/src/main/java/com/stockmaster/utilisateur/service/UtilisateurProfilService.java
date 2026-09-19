package com.stockmaster.utilisateur.service;

import com.stockmaster.shared.domain.entity.Utilisateur;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.repository.UtilisateurRepository;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.shared.storage.MinioService;
import com.stockmaster.utilisateur.dto.request.ProfilUtilisateurRequest;
import com.stockmaster.utilisateur.dto.response.UtilisateurProfilResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * US-026 — consulter et modifier <b>son propre</b> profil.
 *
 * <p><b>Self-service par construction</b> : le service travaille toujours sur
 * {@code principal.getUserId()} — aucun identifiant externe n'est accepté,
 * donc chaque utilisateur ne peut consulter et modifier que son propre profil
 * (critère d'acceptation US-026).</p>
 *
 * <p><b>Email et rôle non modifiables</b> (critère d'acceptation US-026) :
 * {@link ProfilUtilisateurRequest} ne porte ni email ni rôle et ce service
 * ne les touche jamais — la modification passe par l'admin (US-024).</p>
 *
 * <p><b>Photo vers MinIO</b> (critère d'acceptation US-026) : délégation à
 * {@link MinioService#uploadImage(String, MultipartFile)} avec préfixe
 * {@code utilisateur/<id>} (objet public-read, URL persistée en colonne
 * {@code photo}). L'upload précède le {@code save} : en cas d'échec MinIO
 * rien n'est persisté et l'ancienne URL reste intacte.</p>
 *
 * <p><b>Sémantique PATCH</b> (convention US-024) : champs {@code null}
 * = inchangés ; part {@code photo} absente = photo actuelle conservée.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UtilisateurProfilService {

    private final UtilisateurRepository utilisateurRepository;
    private final MinioService minioService;

    /**
     * Consulte le profil de l'utilisateur connecté.
     *
     * @throws BusinessException {@code RES_ENTITY_NOT_FOUND} si le compte est
     *         inexistant ou soft-supprimé (404 uniforme — ne se produit en
     *         pratique qu'avec un token émis avant suppression logique).
     */
    @Transactional(readOnly = true)
    public UtilisateurProfilResponse consulter(StockMasterPrincipal principal) {
        Utilisateur moi = chargerMonCompte(principal);
        return UtilisateurProfilResponse.de(moi);
    }

    /**
     * Modifie le profil de l'utilisateur connecté (champs JSON et/ou photo).
     *
     * @throws BusinessException {@code RES_ENTITY_NOT_FOUND} si le compte est
     *         inexistant ou soft-supprimé ; les erreurs de fichier (vide,
     *         trop volumineux, type refusé) et MinIO remontent de
     *         {@link MinioService} sans persistance.
     */
    @Transactional
    public UtilisateurProfilResponse mettreAJour(StockMasterPrincipal principal,
            ProfilUtilisateurRequest request, MultipartFile photo) {
        Utilisateur moi = chargerMonCompte(principal);

        // Upload MinIO AVANT tout save : échec → transaction annulée, rien n'est persisté
        if (photo != null && !photo.isEmpty()) {
            String url = minioService.uploadImage("utilisateur/" + moi.getId(), photo);
            moi.setPhoto(url);
            log.info("US-026 : photo de l'utilisateur {} remplacée ({})", moi.getId(), url);
        }

        if (request != null) {
            if (request.getPrenom() != null) {
                moi.setPrenom(request.getPrenom());
            }
            if (request.getNom() != null) {
                moi.setNom(request.getNom());
            }
            if (request.getAdresseVille() != null) {
                moi.setAdresseVille(request.getAdresseVille());
            }
        }

        Utilisateur modifie = utilisateurRepository.save(moi);
        log.info("US-026 : profil de l'utilisateur {} mis à jour par lui-même", modifie.getId());
        return UtilisateurProfilResponse.de(modifie);
    }

    /** Self-service : la cible est TOUJOURS l'utilisateur du token — jamais un id externe. */
    private Utilisateur chargerMonCompte(StockMasterPrincipal principal) {
        Long monId = principal != null ? principal.getUserId() : null;
        if (monId == null) {
            throw new BusinessException(ErrorCode.SEC_ACCESS_DENIED);
        }
        return utilisateurRepository.findById(monId)
                .filter(u -> !Boolean.TRUE.equals(u.getSupprime()))
                .orElseThrow(() -> new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND));
    }
}
