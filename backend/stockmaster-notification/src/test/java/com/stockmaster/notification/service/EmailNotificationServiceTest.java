package com.stockmaster.notification.service;

import com.stockmaster.notification.port.CanalNotification;
import com.stockmaster.shared.domain.entity.Utilisateur;
import com.stockmaster.shared.repository.UtilisateurRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * US-021 (infra) — {@link EmailNotificationService} : première implémentation du
 * port {@link CanalNotification} (DEC-014). Contrats verrouillés :
 * <ul>
 *   <li>l'email part vers l'adresse du destinataireId résolu (jamais une adresse
 *       passée en clair dans le port) ;</li>
 *   <li>un échec SMTP ne propage JAMAIS d'exception (l'email ne doit pas casser la
 *       transaction métier — même philosophie que US-006) ;</li>
 *   <li>mail non configuré (hébergement sans SMTP) → no-op journalisé, pas d'erreur.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmailNotificationService — canal EMAIL du port CanalNotification (DEC-014)")
class EmailNotificationServiceTest {

    @Mock
    private ObjectProvider<JavaMailSender> mailSenderProvider;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private JavaMailSender mailSender;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> messageCaptor;

    @InjectMocks
    private EmailNotificationService service;

    private Utilisateur utilisateur(String email) {
        return Utilisateur.builder()
                .id(7L)
                .email(email)
                .prenom("Marie")
                .nom("Ngono")
                .build();
    }

    @Test
    @DisplayName("✅ envoie l'email à l'adresse du destinataire résolu, sujet et corps intacts")
    void shouldSendToResolvedUserEmail() {
        when(utilisateurRepository.findById(7L)).thenReturn(Optional.of(utilisateur("marie.ngono@distribo.cm")));
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);

        service.envoyer(10L, 7L, "INVITATION", "Invitation StockMaster", "Votre lien : https://…");

        // envoi asynchrone → vérification avec délai (déterministe)
        verify(mailSender, timeout(2000)).send(messageCaptor.capture());
        SimpleMailMessage message = messageCaptor.getValue();
        assertThat(message.getTo()).containsExactly("marie.ngono@distribo.cm");
        assertThat(message.getSubject()).isEqualTo("Invitation StockMaster");
        assertThat(message.getText()).isEqualTo("Votre lien : https://…");
    }

    @Test
    @DisplayName("✅ le canal s'identifie comme EMAIL")
    void shouldExposeEmailChannelName() {
        assertThat(service.nom()).isEqualTo("EMAIL");
        assertThat(service).isInstanceOf(CanalNotification.class);
    }

    @Test
    @DisplayName("❌ échec SMTP → avalé (jamais de propagation vers l'appelant)")
    void shouldSwallowSmtpFailure() {
        when(utilisateurRepository.findById(7L)).thenReturn(Optional.of(utilisateur("marie.ngono@distribo.cm")));
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        doThrow(new RuntimeException("SMTP down")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatCode(() -> service.envoyer(10L, 7L, "INVITATION", "Sujet", "Corps"))
                .doesNotThrowAnyException();
        // l'envoi a bien été tenté (asynchrone) et son échec n'a rien remonté
        verify(mailSender, timeout(2000)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("❌ mail non configuré (SMTP absent) → no-op journalisé, pas d'erreur")
    void shouldSkipWhenMailSenderNotConfigured() {
        when(utilisateurRepository.findById(7L)).thenReturn(Optional.of(utilisateur("marie.ngono@distribo.cm")));
        when(mailSenderProvider.getIfAvailable()).thenReturn(null);

        assertThatCode(() -> service.envoyer(10L, 7L, "INVITATION", "Sujet", "Corps"))
                .doesNotThrowAnyException();
        // le no-op est décidé dans la tâche asynchrone
        verify(mailSenderProvider, timeout(2000).atLeastOnce()).getIfAvailable();
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("❌ destinataire inconnu ou supprimé → no-op (pas d'email orphelin)")
    void shouldSkipWhenDestinataireUnknown() {
        when(utilisateurRepository.findById(999L)).thenReturn(Optional.empty());

        service.envoyer(10L, 999L, "INVITATION", "Sujet", "Corps");

        verify(mailSenderProvider, never()).getIfAvailable();
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }
}
