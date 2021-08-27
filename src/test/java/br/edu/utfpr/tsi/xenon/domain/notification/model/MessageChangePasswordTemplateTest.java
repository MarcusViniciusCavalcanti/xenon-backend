package br.edu.utfpr.tsi.xenon.domain.notification.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test - Unidade - MessageChangePasswordTemplate")
class MessageChangePasswordTemplateTest {

    @Test
    @DisplayName("Deve retornar o subject [Xenon senha altera com sucesso]")
    void shouldReturnSubject() {
        var template = new MessageChangePasswordTemplate("email");
        assertEquals("Xenon senha altera com sucesso", template.subject());
    }

    @Test
    @DisplayName("Deve retornar o e-mail")
    void shouldReturnEmail() {
        var template = new MessageChangePasswordTemplate("email");
        assertEquals("email", template.to());
    }
}
