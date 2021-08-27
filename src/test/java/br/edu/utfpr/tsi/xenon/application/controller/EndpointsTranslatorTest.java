package br.edu.utfpr.tsi.xenon.application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.MessageSource;

@DisplayName("Teste - Unidade - EndpointsTranslator")
class EndpointsTranslatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"*", "", " "})
    @NullSource
    @DisplayName("Deve retorna idioma padrão [Locale.US]")
    void shouldReturnLanguageDefault(String value) {
        var controller = new TestController();

        var locale = controller.getLocale(value);

        assertEquals(Locale.US, locale);
    }

    @Test
    @DisplayName("Deve retorna idioma informado")
    void shouldReturnLanguageInformative() {
        var controller = new TestController();

        var locale = controller.getLocale(Locale.CANADA.toLanguageTag());

        assertEquals(Locale.CANADA, locale);
    }

    @Test
    @DisplayName("Deve retorna o primeiro idioma informado")
    void shouldReturnLanguageInformativeFirst() {
        var controller = new TestController();
        var localte = "pt-Br,en-CA";
        var locale = controller.getLocale(localte);

        assertEquals(Locale.forLanguageTag("pt-Br"), locale);
    }

    static class TestController implements EndpointsTranslator {

        @Override
        public MessageSource getMessageSource() {
            return null;
        }
    }
}
