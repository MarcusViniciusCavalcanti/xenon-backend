package br.edu.utfpr.tsi.xenon.domain.recognize.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;


@DisplayName("Teste - Unidade -  RecognizerSpecifications")
class RecognizerSpecificationsTest {

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    @NullSource
    @DisplayName("Deve retornar null quando driver name estiver em branco ou nulo")
    void shouldReturnNullWhenDriverNameIsInvalid(String driverName) {
        assertNull(RecognizerSpecifications.nameDriver(driverName));
    }

    @Test
    @DisplayName("Deve retornar null quando only error estiver null")
    void shouldReturnNullWhenOnluErrorIsnull() {
        assertNull(RecognizerSpecifications.onlyError(null));
    }

    @Test
    @DisplayName("Deve retornar Specification de drive name")
    void shouldReturnSpecificationDriverName() {
        assertNotNull(RecognizerSpecifications.nameDriver("name"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Deve retornar Specification de only error")
    void shouldReturnSpecificationOnlyError(Boolean value) {
        assertNotNull(RecognizerSpecifications.onlyError(value));
    }
}
