package com.stockmaster.catalogue.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Taux de TVA supportés à la création de catégorie — critère US-027 :
 * « 19.25 %, 0 %, ou taux réduit configuré ». La liste est configurable
 * ({@code stockmaster.catalogue.taux-tva-autorises}, CSV) pour qu'un taux
 * réduit puisse être ouvert sans redéploiement de règle codée en dur.
 *
 * <p>Comparaison par <b>valeur</b> ({@code compareTo}) : {@code 6.60} et
 * {@code 6.6} sont équivalents, échelle ignorée.</p>
 */
@Component
public class TauxTvaAutorises {

    /** Défaut : taux standard Cameroun + exonération. */
    static final String DEFAUT_CSV = "19.25,0";

    private final Set<BigDecimal> valeurs;

    @Autowired
    public TauxTvaAutorises(
            @Value("${stockmaster.catalogue.taux-tva-autorises:" + DEFAUT_CSV + "}") String tauxCsv) {
        this(parse(tauxCsv));
    }

    TauxTvaAutorises(Set<BigDecimal> valeurs) {
        this.valeurs = Set.copyOf(valeurs);
    }

    static Set<BigDecimal> parse(String tauxCsv) {
        return Arrays.stream(tauxCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> new BigDecimal(s.toLowerCase(Locale.ROOT)))
                .collect(Collectors.toUnmodifiableSet());
    }

    public boolean estAutorise(BigDecimal taux) {
        if (taux == null) {
            return false;
        }
        return valeurs.stream().anyMatch(v -> v.compareTo(taux) == 0);
    }
}
