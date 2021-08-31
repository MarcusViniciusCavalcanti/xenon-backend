package br.edu.utfpr.tsi.xenon.domain.notification.model;

import static org.junit.jupiter.api.Assertions.*;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test - Unidade - MessageRenewPassTemplate")
class MessageRenewPassTemplateTest {

    @Test
    @DisplayName("Deve fazer o replace senha no template html ")
    void shouldHaveReplaceTemplateHtml() {
        var faker = Faker.instance();
        var password = faker.bothify("???????-####");
        var email = faker.internet().emailAddress();
        var template = new MessageRenewPassTemplate(password, email);

        assertFalse(template.getTemplate().contains("[---senha---]"));
        assertTrue(template.getTemplate().contains(password));
    }

    @Test
    @DisplayName("Deve retornar o subject [Xenon nova senha]")
    void shouldReturnSubject() {
        var template = new MessageRenewPassTemplate("password", "email");
        assertEquals("Xenon nova senha", template.subject());
    }

    @Test
    @DisplayName("Deve retornar a senha")
    void shouldReturnPassword() {
        var template = new MessageRenewPassTemplate("password", "email");
        assertEquals("password", template.password());
    }

    @Test
    @DisplayName("Deve retornar a email")
    void shouldReturnEmail() {
        var template = new MessageRenewPassTemplate("password", "email");
        assertEquals("email", template.to());
    }

}
