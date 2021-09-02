package br.edu.utfpr.tsi.xenon.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import br.edu.utfpr.tsi.xenon.structure.ParamsQuerySearchUserDto.DirectionEnum;
import br.edu.utfpr.tsi.xenon.structure.ParamsQuerySearchUserDto.SortedEnum;
import br.edu.utfpr.tsi.xenon.structure.ParamsQuerySearchUserDto.Type;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Test - Unidade - ParamsQuerySearchUserDto")
class ParamsQuerySearchUserDtoTest {

    @ParameterizedTest
    @ValueSource(strings = {"aaa", "aaa", "bbbb"})
    @NullSource
    @DisplayName("Deve retornar o padrão CREATED")
    void shouldReturnCREATEDByDefault(String value) {
        var sorted = SortedEnum.fromValue(value);
        assertEquals(SortedEnum.CREATED, sorted);
    }

    @ParameterizedTest
    @ValueSource(strings = {"aaa", "aaa", "eeee"})
    @NullSource
    @DisplayName("Deve retornar o padrão DESC")
    void shouldReturnDESCByDefault(String value) {
        var direction = DirectionEnum.fromValue(value);
        assertEquals(DirectionEnum.DESC, direction);
    }

    @ParameterizedTest
    @ValueSource(strings = {"aaa", "aaa", "nnnnn"})
    @DisplayName("Deve retornar SERVICE para valores o padrão")
    void shouldReturnTypeByDefault(String value) {
        var type = Type.fromValue(value);
        assertEquals(Type.SERVICE.name(), type);
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", ""})
    @NullSource
    @DisplayName("Deve retornar null se type não foi encontrado")
    void shouldReturnNullType(String value) {
        var type = Type.fromValue(value);
        assertNull(type);
    }
}
