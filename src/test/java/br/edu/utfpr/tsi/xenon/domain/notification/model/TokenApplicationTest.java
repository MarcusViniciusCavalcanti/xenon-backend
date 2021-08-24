package br.edu.utfpr.tsi.xenon.domain.notification.model;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.ONE_HUNDRED_MILLISECONDS;
import static org.junit.jupiter.api.Assertions.*;

import com.github.javafaker.service.FakeValuesService;
import com.github.javafaker.service.RandomService;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Test - Unidade - TokenApplication")
class TokenApplicationTest {
    private static final Pattern PATTERN_TOKEN =
        Pattern.compile("([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})");

    private static final FakeValuesService
        FAKER = new FakeValuesService(Locale.forLanguageTag("pt-BR"), new RandomService());

    @Test
    @DisplayName("Deve lançar exception quando key não existe")
    void shouldThrowsIllegalStateException() {
        var token = TokenApplication.newInstance(null);
        assertThrows(IllegalStateException.class, token::generateNewToken);
    }

    @Test
    @DisplayName("Deve gerar um novo token a cada chamada")
    void shouldHaveGenerateToken() {
        var email = FAKER.bothify("???**@alunos.utfpr.edu.br");
        var token = TokenApplication.newInstance(email);

        token.generateNewToken();
        var tokenValue = token.getToken();

        assertNotNull(tokenValue);
        assertTrue(PATTERN_TOKEN.asPredicate().test(tokenValue));
    }

    @Test
    @DisplayName("Deve gerar um novo token a cada chamada")
    void shouldHaveCreateNewToken() {
        var email = FAKER.bothify("???????@alunos.utfpr.edu.br");
        var token = TokenApplication.newInstance(email);

        token.generateNewToken();
        var token01 = token.getToken();

        await().atMost(ONE_HUNDRED_MILLISECONDS);

        token.generateNewToken();
        var token02 = token.getToken();

        assertNotEquals(token01, token02);
        assertEquals(email, token.getKey());
    }

    @ParameterizedTest(name = "deve ser [{2}]")
    @MethodSource("provideArgsToValidatedToken")
    @DisplayName("Deve validar email")
    void shouldValidateToken(String actual, String expected, Boolean resultExpected) {
        var tokenApplication = TokenApplication.newInstance();
        assertEquals(resultExpected, tokenApplication.validateToken(actual, expected));
    }

    @Test
    @DisplayName("Deve retornar false quando token é maior que 36 caracteres")
    void shouldHaveFalseValidateToken() {
        var tokenApplication = TokenApplication.newInstance();
        assertFalse(tokenApplication.validateToken(RandomStringUtils.random(37), RandomStringUtils.random(36)));
        assertFalse(tokenApplication.validateToken(RandomStringUtils.random(36), RandomStringUtils.random(37)));
    }

    public static Stream<Arguments> provideArgsToValidatedToken() {
        var uuid1 = UUID.randomUUID().toString();
        var uuid2 = UUID.randomUUID().toString();
        return Stream.of(
            Arguments.of(uuid1, uuid1, TRUE),
            Arguments.of(uuid1, uuid2, FALSE),
            Arguments.of("   ", uuid2, FALSE),
            Arguments.of("", uuid2, FALSE),
            Arguments.of(uuid1, "", FALSE),
            Arguments.of(uuid1, "    ", FALSE),
            Arguments.of(uuid1, null, FALSE)
        );
    }
}
