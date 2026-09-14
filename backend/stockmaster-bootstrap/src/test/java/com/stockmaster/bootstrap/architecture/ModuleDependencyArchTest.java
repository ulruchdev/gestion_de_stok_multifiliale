package com.stockmaster.bootstrap.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import java.util.ArrayList;
import java.util.List;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Règles d'architecture du monolithe modulaire — enforcement automatique.
 *
 * <p>Dépendances inter-modules autorisées (décision assumée) :
 * <ul>
 *   <li>{@code com.stockmaster.shared..} — infrastructure et agrégats tenant ;</li>
 *   <li>la <strong>couche contrat</strong> des modules exposés : {@code domain.entity},
 *       {@code domain.enums}, {@code event}, {@code port} de
 *       catalogue / tiers / notification — c'est la structure voulue par V1
 *       (les FK de commande_fournisseur → article, vente → client…
 *       exigent que les documents référencent les entités du catalogue et des
 *       tiers, cf. DEC-002).</li>
 * </ul>
 *
 * <p>Tout le reste est interdit : un module ne doit JAMAIS toucher aux
 * {@code service}, {@code repository}, {@code controller}, {@code dto},
 * {@code mapper} ou {@code config} d'un autre module — la logique métier et
 * l'accès aux données restent encapsulés derrière le contrat de chaque module.</p>
 */
@AnalyzeClasses(packages = "com.stockmaster", importOptions = ImportOption.DoNotIncludeTests.class)
class ModuleDependencyArchTest {

    /** Modules fonctionnels (tout sauf shared et bootstrap). */
    private static final List<String> FUNCTIONAL_MODULES = List.of(
            "auth", "groupe", "utilisateur", "catalogue", "tiers",
            "achat", "stock", "vente", "notification", "reporting");

    /**
     * Sous-packages contrat qu'un autre module peut référencer.
     * S'y ajoutent implicitement : self, shared, java, jakarta, spring…
     */
    private static final String[] CONTRACT_PACKAGES = {
            "com.stockmaster.catalogue.domain.entity..",
            "com.stockmaster.catalogue.domain.enums..",
            "com.stockmaster.catalogue.event..",
            "com.stockmaster.catalogue.port..",
            "com.stockmaster.tiers.domain.entity..",
            "com.stockmaster.tiers.domain.enums..",
            "com.stockmaster.tiers.event..",
            "com.stockmaster.tiers.port..",
            "com.stockmaster.notification.domain.entity..",
            "com.stockmaster.notification.domain.enums..",
            "com.stockmaster.notification.event..",
            "com.stockmaster.notification.port.."
    };

    /** Tous les modules fonctionnels + bootstrap (tout ce qui n'est ni self ni shared). */
    private static String[] allOtherModulePackages(String except) {
        return FUNCTIONAL_MODULES.stream()
                .filter(m -> !m.equals(except))
                .map(m -> "com.stockmaster." + m + "..")
                .toArray(String[]::new);
    }

    // ================================================================
    // Règle 1 — les modules fonctionnels ne dépendent que de shared
    //           et des contrats (entités/enums/événements/ports).
    // ================================================================
    @ArchTest
    static void functionalModulesDependOnlyOnSharedAndContracts(JavaClasses classes) {
        List<String> violations = new ArrayList<>();

        for (String module : FUNCTIONAL_MODULES) {
            String modulePackage = "com.stockmaster." + module + ".";

            for (JavaClass clazz : classes) {
                if (!clazz.getPackageName().startsWith(modulePackage)) {
                    continue;
                }
                for (var dependency : clazz.getDirectDependenciesFromSelf()) {
                    JavaClass target = dependency.getTargetClass();
                    String targetPackage = target.getPackageName();

                    if (!targetPackage.startsWith("com.stockmaster.")) {
                        continue; // JDK / libs tiers : hors périmètre de la règle
                    }
                    boolean self = targetPackage.startsWith(modulePackage);
                    boolean shared = targetPackage.startsWith("com.stockmaster.shared.");
                    boolean contract = resideInAnyPackage(CONTRACT_PACKAGES).test(target);

                    if (!self && !shared && !contract) {
                        violations.add("  " + clazz.getName() + " -> "
                                + dependency.getDescription());
                    }
                }
            }
        }

        if (!violations.isEmpty()) {
            throw new AssertionError(
                    "Dépendances inter-modules interdites détectées "
                    + "(seuls shared et les contrats domain.entity/domain.enums/event/port "
                    + "de catalogue, tiers, notification sont autorisés) :\n"
                    + String.join("\n", violations));
        }
    }

    // ================================================================
    // Règle 2 — shared ne dépend d'aucun module fonctionnel ni de
    //           bootstrap (le graphe pointe TOUJOURS vers shared).
    // ================================================================
    @ArchTest
    static final ArchRule sharedDependsOnNoOtherModule =
            noClasses().that().resideInAPackage("com.stockmaster.shared..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(allOtherModulePackages("shared"))
                    .because("shared est la base du graphe de dépendances — "
                            + "il ne doit jamais dépendre d'un module fonctionnel ni du bootstrap");

    // ================================================================
    // Règle 3 — auth est une feuille du graphe : il ne consomme
    //           AUCUN autre module fonctionnel (contrats inclus).
    // ================================================================
    @ArchTest
    static final ArchRule authIsALeafModule =
            noClasses().that().resideInAPackage("com.stockmaster.auth..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(allOtherModulePackages("auth"))
                    .because("auth ne dépend que de shared (identité, JWT, agrégats tenant) — "
                            + "l'envoi d'emails passe par des événements, jamais par un import");

    // ================================================================
    // Règle 4 — le port de notification est le point d'entrée prévu :
    //           personne hors du module ne touche à notification.service.
    //           (Cas particulier lisible de la règle 1, qui protège
    //           aussi les services de tous les autres modules.)
    // ================================================================
    @ArchTest
    static final ArchRule notificationServiceIsEncapsulated =
            noClasses().that(areOutsideNotificationModule())
                    .should().dependOnClassesThat()
                    .resideInAPackage("com.stockmaster.notification.service..")
                    .because("les émetteurs utilisent CanalNotification (port, DEC-014), "
                            + "jamais l'implémentation notification.service");

    private static DescribedPredicate<JavaClass> areOutsideNotificationModule() {
        return DescribedPredicate.not(
                resideInAPackage("com.stockmaster.notification.."))
                .as("classes hors du module notification");
    }
}
