package com.stockmaster.tiers.service;

import com.stockmaster.shared.config.PaginationProperties;
import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.shared.dto.response.PageResponse;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.tiers.domain.entity.Client;
import com.stockmaster.tiers.dto.request.CreerClientRequest;
import com.stockmaster.tiers.dto.request.ModifierClientRequest;
import com.stockmaster.tiers.dto.response.ClientResponse;
import com.stockmaster.tiers.mapper.ClientMapper;
import com.stockmaster.tiers.port.CommandesClientPort;
import com.stockmaster.tiers.repository.ClientRepository;
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
 * Clients — EPIC 6 (Tiers), US-040 → US-043.
 *
 * <p><b>RBAC (arbitrage du 9 nov. 2026)</b> : écriture réservée à
 * {@code COMMERCIAL | ADMIN_FILIALE | ADMIN_GROUPE} ; le {@code CAISSIER} est
 * admis <b>en lecture seule</b> (recherche d'un client au comptoir). Le
 * {@code @PreAuthorize} porte le refus côté contrôleur ; les gardes ci-dessous
 * sont la défense en profondeur (fail-fast, avant toute I/O).</p>
 *
 * <p><b>Isolation tenant (règle n°1)</b> : le client est rattaché à
 * l'{@code entreprise_id} extrait du JWT, <b>jamais</b> lu du corps de la
 * requête (le DTO n'expose aucun champ tenant). Id inconnu, hors tenant ou déjà
 * archivé → {@code 404 RES_001} uniforme, jamais {@code 403}.</p>
 *
 * <p>Les gardes tenant/rôle sont mutualisées avec {@code FournisseurService} via
 * {@link TiersPerimetreGuard} (même comportement, une seule implémentation).</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {

    /** Rôles autorisés en lecture (US-041 — CAISSIER inclus). */
    private static final Set<String> ROLES_LECTURE = Set.of(
            "COMMERCIAL", "ADMIN_FILIALE", "ADMIN_GROUPE", "CAISSIER");

    /** Rôles autorisés en écriture (US-040/042/043). */
    private static final Set<String> ROLES_ECRITURE = Set.of(
            "COMMERCIAL", "ADMIN_FILIALE", "ADMIN_GROUPE");

    private final ClientRepository clientRepository;
    private final TiersPerimetreGuard perimetre;
    private final ClientMapper clientMapper;
    private final PaginationProperties paginationProperties;
    /**
     * US-043 — lecture des commandes client par port exposé (DEC-019) :
     * l'implémentation est dans {@code stockmaster-vente}, {@code tiers} ne
     * connaît que l'interface (ArchUnit règle 1).
     */
    private final CommandesClientPort commandesClientPort;

    // ─── US-040 — créer ──────────────────────────────────────────────────────

    /**
     * Crée un client dans l'entreprise du JWT.
     *
     * @throws BusinessException {@code SEC_001} rôle non autorisé ou JWT sans
     *         entreprise ; {@code GRP_002} JWT sans groupe ; {@code RES_001}
     *         entreprise introuvable, soft-supprimée ou hors groupe
     */
    @Transactional
    public ClientResponse creer(StockMasterPrincipal principal, CreerClientRequest request) {
        perimetre.verifierRole(principal, ROLES_ECRITURE);
        Entreprise entreprise = perimetre.chargerEntrepriseDuPrincipal(principal);

        Client client = clientMapper.toEntity(request);
        client.setEntreprise(entreprise);
        client.setSupprime(false);
        Client cree = clientRepository.save(client);

        log.info("US-040 : client id={} ({}) créé pour l'entreprise {} par l'utilisateur {}",
                cree.getId(), cree.getNom(), entreprise.getId(), principal.getUserId());

        return clientMapper.toResponse(cree);
    }

    // ─── US-041 — lister / rechercher ────────────────────────────────────────

    /**
     * US-041 — liste paginée des clients du tenant, avec recherche optionnelle
     * par nom, prénom ou téléphone.
     *
     * <p><b>Arbitrages validés</b> : tri fixe {@code nom ASC} ; {@code size}
     * borné à {@code stockmaster.pagination.max-page-size} (100) côté service —
     * clamp silencieux, jamais de {@code 400} ; {@code search} vide ou blanc →
     * listing complet ; aucun résultat → page vide, jamais {@code 404}.</p>
     */
    @Transactional(readOnly = true)
    public PageResponse<ClientResponse> lister(StockMasterPrincipal principal,
                                               String search, int page, int size) {
        perimetre.verifierRole(principal, ROLES_LECTURE);
        Entreprise entreprise = perimetre.chargerEntrepriseDuPrincipal(principal);
        Long entrepriseId = entreprise.getId();

        Pageable pageable = PageRequest.of(Math.max(0, page), borneTaille(size),
                Sort.by(Sort.Direction.ASC, "nom"));

        Page<Client> clients = (search == null || search.isBlank())
                ? clientRepository.findAllByEntrepriseIdAndSupprimeFalse(entrepriseId, pageable)
                : clientRepository.rechercherParEntrepriseEtTexte(
                        entrepriseId, search.trim(), pageable);

        PageResponse<ClientResponse> reponse = PageResponse.from(clients.map(clientMapper::toResponse));

        log.info("US-041 : {} client(s) sur entreprise {} (page {}/{}, search={})",
                reponse.getTotalElements(), entrepriseId, reponse.getPage() + 1,
                Math.max(reponse.getTotalPages(), 1),
                (search == null || search.isBlank()) ? "<absent>" : search.trim());

        return reponse;
    }

    // ─── US-042 — modifier ───────────────────────────────────────────────────

    /**
     * US-042 — mise à jour des coordonnées d'un client du tenant du JWT.
     *
     * <p><b>Sémantique PATCH</b> (convention US-014/018/024/038 sur endpoint
     * {@code PUT}) : champ absent ou blanc = valeur conservée, corps vide =
     * aucune modification. {@code entreprise}, {@code id} et {@code supprime}
     * ne sont jamais touchés par le payload.</p>
     *
     * @throws BusinessException {@code RES_001} uniforme si l'entreprise ou le
     *         client est introuvable, supprimé ou hors périmètre
     */
    @Transactional
    public ClientResponse modifier(StockMasterPrincipal principal, Long id,
                                   ModifierClientRequest request) {
        perimetre.verifierRole(principal, ROLES_ECRITURE);
        Entreprise entreprise = perimetre.chargerEntrepriseDuPrincipal(principal);

        Client client = clientRepository.findByIdAndSupprimeFalse(id)
                .filter(c -> c.getEntreprise() != null
                        && entreprise.getId().equals(c.getEntreprise().getId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND));

        appliquerModifications(client, request);
        Client misAJour = clientRepository.save(client);

        log.info("US-042 : client id={} ({}) modifié pour l'entreprise {} par l'utilisateur {}",
                misAJour.getId(), misAJour.getNom(), entreprise.getId(), principal.getUserId());

        return clientMapper.toResponse(misAJour);
    }

    // ─── US-043 — supprimer (archiver) ───────────────────────────────────────

    /**
     * US-043 — archivage logique d'un client du tenant du JWT.
     *
     * <p><b>Soft delete uniquement</b> : {@code supprime = true}, la ligne reste
     * en base — la traçabilité commerciale et les FK {@code commande_client}
     * sont préservées.</p>
     *
     * <p><b>Règle de blocage (arbitrage du 9 nov. 2026)</b> : le CA exige la
     * suppression d'un client « sans historique de commandes » →
     * {@code 409 RES_005} dès qu'<b>une</b> commande existe, <b>quel que soit son
     * état</b> (y compris {@code ANNULEE}). Choix volontairement plus strict que
     * US-039 (où seules les commandes actives bloquent).</p>
     *
     * <p><b>Isolation tenant</b> : chargement par id <b>puis</b> filtre
     * {@code entreprise_id} du JWT ; déjà archivé = id inconnu →
     * {@code 404 RES_001}, sans interroger le port de dépendance.</p>
     *
     * @throws BusinessException {@code RES_005} si des commandes existent
     */
    @Transactional
    public ClientResponse supprimer(StockMasterPrincipal principal, Long id) {
        perimetre.verifierRole(principal, ROLES_ECRITURE);
        Entreprise entreprise = perimetre.chargerEntrepriseDuPrincipal(principal);

        Client client = clientRepository.findByIdAndSupprimeFalse(id)
                .filter(c -> c.getEntreprise() != null
                        && entreprise.getId().equals(c.getEntreprise().getId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND));

        // Toute commande passée — même annulée — est un historique à conserver.
        if (commandesClientPort.aDesCommandes(client.getId())) {
            throw new BusinessException(ErrorCode.RES_ENTITY_HAS_DEPENDENCIES);
        }

        client.setSupprime(true);
        Client archive = clientRepository.save(client);

        log.info("US-043 : client id={} ({}) archivé pour l'entreprise {} par l'utilisateur {}",
                archive.getId(), archive.getNom(), entreprise.getId(), principal.getUserId());

        return clientMapper.toResponse(archive);
    }

    // ─── helpers privés ──────────────────────────────────────────────────────

    /**
     * PATCH sémantique — seule une valeur non nulle et non blanche est appliquée.
     * {@code entreprise}, {@code id} et {@code supprime} ne sont jamais touchés.
     */
    private void appliquerModifications(Client client, ModifierClientRequest request) {
        if (StringUtils.hasText(request.getNom())) {
            client.setNom(request.getNom());
        }
        if (StringUtils.hasText(request.getPrenom())) {
            client.setPrenom(request.getPrenom());
        }
        if (StringUtils.hasText(request.getTelephone())) {
            client.setTelephone(request.getTelephone());
        }
        if (StringUtils.hasText(request.getEmail())) {
            client.setEmail(request.getEmail());
        }
        if (StringUtils.hasText(request.getAdresseVille())) {
            client.setAdresseVille(request.getAdresseVille());
        }
        if (StringUtils.hasText(request.getAdresseQuartier())) {
            client.setAdresseQuartier(request.getAdresseQuartier());
        }
    }

    /** Bornes de pagination — convention repo : clamp silencieux, pas de 400. */
    private int borneTaille(int size) {
        return Math.min(Math.max(1, size), paginationProperties.getMaxPageSize());
    }
}
