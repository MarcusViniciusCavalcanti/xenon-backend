package br.edu.utfpr.tsi.xenon.domain.notification.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test - Unidade - MessageWelcomeTemplate")
class MessageWelcomeTemplateTest {

    @Test
    @DisplayName("Deve fazer o replace senha no template html ")
    void shouldHaveReplaceTemplateHtml() {
        var faker = Faker.instance();
        var email = faker.internet().emailAddress();
        var name = faker.name().fullName();
        var pass = faker.internet().password();
        var template = new MessageWelcomeTemplate(email, name, pass);

        assertFalse(template.getTemplate().contains("[--nome--]"));
        assertFalse(template.getTemplate().contains("[---email---]"));
        assertFalse(template.getTemplate().contains("[---senha---]"));

        assertTrue(template.getTemplate().contains(email));
        assertTrue(template.getTemplate().contains(name));
        assertTrue(template.getTemplate().contains(pass));
    }

    @Test
    @DisplayName("Deve retornar a senha")
    void shouldReturnPassword() {
        var template = new MessageWelcomeTemplate("email", "name", "password");
        assertEquals("password", template.pass());
    }

    @Test
    @DisplayName("Deve retornar a email")
    void shouldReturnEmail() {
        var template = new MessageWelcomeTemplate("email", "name", "password");
        assertEquals("email", template.to());
    }

    @Test
    @DisplayName("Deve retornar o nome")
    void shouldReturnName() {
        var template = new MessageWelcomeTemplate("email", "name", "password");
        assertEquals("name", template.name());
    }
}
