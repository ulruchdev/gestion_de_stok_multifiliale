package com.stockmaster.shared.domain.entity;

import com.stockmaster.shared.domain.enums.PlanAbonnement;
import com.stockmaster.shared.domain.enums.TypeEntreprise;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Défauts @PrePersist des agrégats tenant (DEC-015, DEC-016, A-10)")
class TenantDefaultsTest {

    @Test
    @DisplayName("TenantGroup.onCreate → actif, non supprimé, plan GRATUIT, limites 1 filiale / 10 utilisateurs")
    void tenantGroupDefaults() {
        TenantGroup g = TenantGroup.builder().nomGroupe("G1").build();
        g.onCreate();
        assertThat(g.getActif()).isTrue();
        assertThat(g.getSupprime()).isFalse();
        assertThat(g.getPlanAbonnement()).isEqualTo(PlanAbonnement.GRATUIT);
        assertThat(g.getLimiteFiliales()).isEqualTo(1);
        assertThat(g.getLimiteUtilisateurs()).isEqualTo(10);
        assertThat(g.getDateCreation()).isNotNull();
        assertThat(g.getDateModification()).isNotNull();
    }

    @Test
    @DisplayName("TenantGroup.onCreate préserve les valeurs métier explicites (PRO / 50) et horodate")
    void tenantGroupPreservesExplicitValues() {
        TenantGroup g = TenantGroup.builder()
                .planAbonnement(PlanAbonnement.PRO)
                .limiteFiliales(15)
                .limiteUtilisateurs(50)
                .build();
        g.onCreate();
        assertThat(g.getPlanAbonnement()).isEqualTo(PlanAbonnement.PRO);
        assertThat(g.getLimiteFiliales()).isEqualTo(15);
        assertThat(g.getLimiteUtilisateurs()).isEqualTo(50);
        // dateCreation est estampillé à la persistance (sans condition dans onCreate)
        assertThat(g.getDateCreation()).isNotNull();
    }

    @Test
    @DisplayName("Entreprise.onCreate → actif, site opérationnel, pays Cameroun (DEC-015)")
    void entrepriseDefaults() {
        Entreprise e = Entreprise.builder()
                .typeEntreprise(TypeEntreprise.FILIALE)
                .nom("F1")
                .codeFiliale("FIL1")
                .build();
        e.onCreate();
        assertThat(e.getActif()).isTrue();
        assertThat(e.getSupprime()).isFalse();
        assertThat(e.getSiteOperationnel()).isTrue();
        assertThat(e.getAdressePays()).isEqualTo("Cameroun");
        assertThat(e.getDateCreation()).isNotNull();
    }

    @Test
    @DisplayName("Entreprise.onCreate préserve siteOperationnel=false (pur siège, DEC-015)")
    void entreprisePreservesNonOperationalSite() {
        Entreprise e = Entreprise.builder()
                .typeEntreprise(TypeEntreprise.MERE)
                .nom("Siège")
                .codeFiliale("SIEGE")
                .siteOperationnel(false)
                .build();
        e.onCreate();
        assertThat(e.getSiteOperationnel()).isFalse();
    }

    @Test
    @DisplayName("Utilisateur.onCreate → actif, email NON vérifié par défaut (DEC-016), non supprimé")
    void utilisateurDefaults() {
        Utilisateur u = new Utilisateur();
        u.onCreate();
        assertThat(u.getActif()).isTrue();
        assertThat(u.getEmailVerifie()).isFalse();
        assertThat(u.getSupprime()).isFalse();
        assertThat(u.getDateCreation()).isNotNull();
        assertThat(u.getDateModification()).isNotNull();
    }

    @Test
    @DisplayName("Utilisateur.onCreate préserve emailVerifie=true")
    void utilisateurPreservesVerifiedEmail() {
        Utilisateur u = new Utilisateur();
        u.setEmailVerifie(true);
        u.onCreate();
        assertThat(u.getEmailVerifie()).isTrue();
    }
}
