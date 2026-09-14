package com.stockmaster.groupe.dto.response;

import com.stockmaster.shared.domain.enums.PlanAbonnement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * Réponse US-015 — informations du groupe + plan d'abonnement actif.
 *
 * <p>Expose exactement ce que demande le critère d'acceptation : nom, plan,
 * limite de filiales ({@code DEC-015}), nombre de filiales actives et
 * date d'expiration du plan.</p>
 */
@Getter
@Builder
@AllArgsConstructor
public class GroupeResponse {

    private Long id;

    private String nomGroupe;

    private PlanAbonnement planAbonnement;

    /** DEC-015 : limite portée par le plan, jamais codée en dur ailleurs. */
    private Integer limiteFiliales;

    /** Nombre de filiales actives (non supprimées, {@code actif = TRUE}). */
    private Long nombreFiliales;

    private LocalDate dateExpirationPlan;

    /** US-014 : URL MinIO du logo du groupe (peut être {@code null} si jamais renseigné). */
    private String logo;

    /** US-014 : Numéro d'Identifiant Fiscal du groupe. */
    private String nif;

    /** US-014 : dénomination légale du groupe. */
    private String raisonSociale;
}
