package br.edu.utfpr.tsi.xenon.domain.notification.model;

import static org.junit.jupiter.api.Assertions.*;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test - Unidade - MessageRegistryTemplate")
class MessageRegistryTemplateTest {

    @Test
    @DisplayName("Deve fazer o replace senha no template html ")
    void shouldHaveReplaceTemplateHtml() {
        var faker = Faker.instance();
        var domain = faker.internet().domainWord();
        var email = faker.internet().emailAddress();
        var name = faker.name().fullName();
        var template = new MessageRegistryTemplate(email, domain, name);

        assertFalse(template.getTemplate().contains("[----nome----]"));
        assertFalse(template.getTemplate().contains("[---URL_ATIVAR_CONTA---]"));
        assertTrue(template.getTemplate().contains(domain));
    }

    @Test
    @DisplayName("Deve retornar o subject [Xenon bem vindo!!!]")
    void shouldReturnSubject() {
        var template = new MessageRegistryTemplate("email", "https://localhost", "name");
        assertEquals("Xenon bem vindo!!!", template.subject());
    }

    @Test
    @DisplayName("Deve retornar o nome")
    void shouldReturnName() {
        var template = new MessageRegistryTemplate("email", "https://localhost", "name");
        assertEquals("name", template.name());
    }

    @Test
    @DisplayName("Deve retornar o e-mail")
    void shouldReturnEmail() {
        var template = new MessageRegistryTemplate("email", "https://localhost", "name");
        assertEquals("email", template.to());
    }

    @Test
    @DisplayName("Deve retornar o e-mail")
    void shouldReturnUrl() {
        var template = new MessageRegistryTemplate("email", "https://localhost", "name");
        assertEquals("https://localhost", template.url());
    }
}
