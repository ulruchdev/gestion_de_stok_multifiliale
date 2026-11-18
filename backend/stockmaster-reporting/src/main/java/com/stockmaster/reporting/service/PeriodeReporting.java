package com.stockmaster.reporting.service;

import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

/**
 * Période d'un rapport — GS-1251 (socle R0).
 *
 * <p>Règle unique de lecture des bornes {@code debut}/{@code fin} des endpoints
 * reporting, réutilisée par R1, R3, R4 et R6 : défaut = mois courant,
 * {@code fin < debut} ou période de plus de 366 jours → {@code 400}
 * ({@code SYS_BAD_REQUEST}).</p>
 *
 * <p>Bornes converties en {@link Instant} <b>semi-ouvertes</b> :
 * {@code debut} = 00:00 UTC du premier jour, {@code fin} = 00:00 UTC du
 * lendemain du dernier jour inclus. Le {@code fin} transmis au port de lecture
 * est donc exclusif — pas de perte de la dernière seconde de la journée.</p>
 *
 * @param debut borne basse inclusive
 * @param fin   borne haute exclusive
 */
public record PeriodeReporting(Instant debut, Instant fin) {

    /** Période maximale acceptée, en jours calendaires inclus. */
    public static final int JOURS_MAX = 366;

    /**
     * Résout les bornes d'une requête de rapport, mois courant par défaut.
     *
     * @param debut premier jour demandé ({@code null} = premier jour du mois courant)
     * @param fin   dernier jour demandé ({@code null} = dernier jour du mois courant)
     * @throws BusinessException {@code SYS_BAD_REQUEST} si les bornes sont incohérentes
     *                           ou si la période dépasse {@value #JOURS_MAX} jours
     */
    public static PeriodeReporting resoudre(LocalDate debut, LocalDate fin) {
        return resoudre(debut, fin, LocalDate.now(ZoneOffset.UTC));
    }

    /** Variante testable : {@code aujourdHui} fixe le mois courant par défaut. */
    static PeriodeReporting resoudre(LocalDate debut, LocalDate fin, LocalDate aujourdHui) {
        LocalDate premierJour = debut != null
                ? debut
                : aujourdHui.withDayOfMonth(1);
        LocalDate dernierJour = fin != null
                ? fin
                : aujourdHui.with(TemporalAdjusters.lastDayOfMonth());

        if (dernierJour.isBefore(premierJour)) {
            throw new BusinessException(ErrorCode.SYS_BAD_REQUEST,
                    "La date de fin doit être postérieure ou égale à la date de début");
        }
        long joursInclus = ChronoUnit.DAYS.between(premierJour, dernierJour) + 1;
        if (joursInclus > JOURS_MAX) {
            throw new BusinessException(ErrorCode.SYS_BAD_REQUEST,
                    "La période ne peut pas dépasser " + JOURS_MAX + " jours");
        }

        return new PeriodeReporting(
                premierJour.atStartOfDay(ZoneOffset.UTC).toInstant(),
                dernierJour.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant());
    }
}
