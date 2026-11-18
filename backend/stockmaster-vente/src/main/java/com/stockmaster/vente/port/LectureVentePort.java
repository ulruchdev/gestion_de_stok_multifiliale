package com.stockmaster.vente.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Port de lecture des ventes — ticket GS-1251 (socle reporting R0).
 *
 * <p>Seul point d'entrée du module {@code reporting} vers {@code vente} : la
 * règle ArchUnit n°1 ({@code ModuleDependencyArchTest}) n'autorise, pour ce
 * module, que {@code vente.port..} et {@code vente.domain.enums..} — jamais
 * {@code vente.repository}, {@code vente.service} ni {@code vente.domain.entity}.
 * Symétrique de {@code stock.port.EcritureStockPort} (#1240).</p>
 *
 * <p>Le port ne renvoie que des <b>lignes brutes de vente</b> (pas d'agrégat de
 * rapport, pas de DTO d'écran) : l'agrégation par article / client / filiale et
 * les calculs de CA restent dans {@code reporting} ({@code CalculCaService},
 * DEC-020). Les jointures libellé article / nom client sont résolues ici pour
 * que {@code reporting} n'ait jamais besoin des entités catalogue ou tiers.</p>
 *
 * <p><b>Périmètre</b> : les bornes {@code debut} (inclus) et {@code fin}
 * (exclu) sont fournies par l'appelant ; l'{@code entrepriseId} vient toujours
 * du JWT, jamais d'un paramètre HTTP. Seules les ventes {@code PAYEE} non
 * supprimées sont lues ({@code StatutVente} — une vente à la caisse est
 * encaissée à la création, DEC-010/DEC-018).</p>
 */
public interface LectureVentePort {

    /**
     * Lignes de vente d'une filiale sur une période.
     *
     * @param entrepriseId filiale du JWT (tenant de lecture)
     * @param debut        borne basse inclusive
     * @param fin          borne haute <b>exclusive</b>
     * @return lignes brutes, liste vide si aucune vente sur la période
     */
    List<LigneVenteAgregat> lignesVendues(Long entrepriseId, Instant debut, Instant fin);

    /**
     * Lignes de vente de plusieurs filiales d'un même groupe — utilisé par la
     * comparaison inter-filiales (ADMIN_GROUPE, #1255). La liste des filiales
     * est résolue depuis le JWT, jamais depuis un paramètre d'entrée.
     *
     * @param entrepriseIds filiales du groupe du principal
     * @param debut         borne basse inclusive
     * @param fin           borne haute <b>exclusive</b>
     * @return lignes brutes, liste vide si aucune vente sur la période
     */
    List<LigneVenteAgregat> lignesVenduesGroupe(List<Long> entrepriseIds, Instant debut, Instant fin);

    /**
     * Ligne de vente projetée — tout ce dont un rapport a besoin, rien de plus.
     *
     * @param entrepriseId    filiale de la vente
     * @param articleId       article vendu
     * @param articleLibelle  désignation au moment de la lecture (catalogue)
     * @param clientId        {@code null} pour un client de passage
     * @param clientNom       {@code null} pour un client de passage
     * @param quantite        quantité en unités de gestion (DECIMAL(12,3))
     * @param prixUnitaireHt  prix unitaire HT snapshoté à la vente (XAF entier)
     * @param tauxTva         taux de TVA snapshoté à la vente
     * @param dateVente       horodatage de la vente
     */
    record LigneVenteAgregat(
            Long entrepriseId,
            Long articleId,
            String articleLibelle,
            Long clientId,
            String clientNom,
            BigDecimal quantite,
            int prixUnitaireHt,
            BigDecimal tauxTva,
            Instant dateVente
    ) {
    }
}
