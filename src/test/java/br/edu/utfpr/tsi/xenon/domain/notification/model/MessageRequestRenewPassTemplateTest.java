package br.edu.utfpr.tsi.xenon.domain.notification.model;

import static org.junit.jupiter.api.Assertions.*;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test - Unidade - MessageRequestRenewPassTemplate")
class MessageRequestRenewPassTemplateTest {

    @Test
    @DisplayName("Deve fazer o replace senha no template html ")
    void shouldHaveReplaceTemplateHtml() {
        var email = Faker.instance().internet().emailAddress();
        var url = Faker.instance().internet().domainName();
        var template = new MessageRequestRenewPassTemplate(email, url);

        assertFalse(template.getTemplate().contains("[---URL_SENHA---]"));
        assertTrue(template.getTemplate().contains(url));
    }

    @Test
    @DisplayName("Deve retornar o subject [Xenon nova senha]")
    void shouldReturnSubject() {
        var template = new MessageRequestRenewPassTemplate("password", "email");
        assertEquals("Xenon: Solicitação de renovação de senha.", template.subject());
    }

    @Test
    @DisplayName("Deve retornar a email")
    void shouldReturnPassword() {
        var template = new MessageRequestRenewPassTemplate("email", "url");
        assertEquals("email", template.to());
    }

    @Test
    @DisplayName("Deve retornar a url")
    void shouldReturnEmail() {
        var template = new MessageRequestRenewPassTemplate("email", "url");
        assertEquals("url", template.url());
    }
}
