package br.edu.utfpr.tsi.xenon.structure;

import br.edu.utfpr.tsi.xenon.structure.ParamsQuerySearchRecognizeDto.SortedRecognizePropertyEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Test - Unidade - SortedRecognizePropertyEnum")
class SortedRecognizePropertyEnumTest {

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "other"})
    @NullSource
    void shouldReturnDefaultValue(String value) {
        var result = ParamsQuerySearchRecognizeDto.SortedRecognizePropertyEnum.fromValue(value);
        Assertions.assertEquals(SortedRecognizePropertyEnum.CREATED, result);
    }
}
