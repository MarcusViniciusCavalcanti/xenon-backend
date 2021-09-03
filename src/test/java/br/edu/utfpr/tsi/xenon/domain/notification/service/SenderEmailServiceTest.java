package br.edu.utfpr.tsi.xenon.domain.notification.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.utfpr.tsi.xenon.domain.notification.model.EmailTemplate;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test - Unidade - SenderAdapter")
class SenderEmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private SenderEmailService senderEmailService;

    @Test
    @DisplayName("Deve enviar email")
    void shouldHaveSendEmail() {
        var mineMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mineMessage);
        doNothing()
            .when(javaMailSender)
            .send(mineMessage);

        var template = new TemplateEmail();
        senderEmailService.sendEmail(template);

        verify(javaMailSender).send(mineMessage);
    }

    @Test
    @DisplayName("Deve enviar email")
    void shouldDontHaveSendEmail() throws MessagingException {
        var mineMessage = mock(MimeMessage.class);
        lenient()
            .doThrow(MessagingException.class)
            .when(mineMessage)
            .setSubject(anyString(), anyString());

        when(javaMailSender.createMimeMessage()).thenReturn(mineMessage);

        var template = new TemplateEmail();
        assertDoesNotThrow(() -> senderEmailService.sendEmail(template));
    }

    public static class TemplateEmail implements EmailTemplate {

        @Override
        public String getTemplate() {
            return "<h1>template</h1>";
        }

        @Override
        public String subject() {
            return "subject";
        }

        @Override
        public String to() {
            return "to";
        }
    }
}
