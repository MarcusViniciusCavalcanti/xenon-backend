package br.edu.utfpr.tsi.xenon.domain.user.service;

import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.utfpr.tsi.xenon.structure.repository.AccessCardRepository;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - ValidatorEmail")
class ValidatorEmailTest {

    @Mock
    private AccessCardRepository accessCardRepository;

    @InjectMocks
    private ValidatorEmail validatorEmail;

    private static Stream<Arguments> provideArgusToEmailInstitutional() {
        var emailInstitutional = Stream.of(
            Arguments.of("not-institutionao@gmail.com", Boolean.FALSE),
            Arguments.of("not-institutionao@edu.br", Boolean.FALSE),
            Arguments.of("not-institutionao@aluno.edu.br", Boolean.FALSE),
            Arguments.of("not-institutional@alunos.edu.br", Boolean.FALSE),
            Arguments.of("not-institutional@utfpr.edu.br", Boolean.FALSE),
            Arguments.of("institutional-studantes@alunos.utfpr.edu.br", TRUE),
            Arguments.of("institutional_studantes@alunos.utfpr.edu.br", TRUE),
            Arguments.of("institutional.studantes@alunos.utfpr.edu.br", TRUE)
        );

        var emails = emailsInvalid();
        return Stream.concat(emailInstitutional, emails);
    }

    private static Stream<Arguments> provideArgsToTestEmailValid() {
        var invalid = emailsInvalid();
        var valid = emailsValid();
        return Stream.concat(invalid, valid);
    }

    private static Stream<Arguments> emailsInvalid() {
        return Stream.of(
            Arguments.of("email", Boolean.FALSE),
            Arguments.of("", Boolean.FALSE),
            Arguments.of(" ", Boolean.FALSE),
            Arguments.of("email@", Boolean.FALSE),
            Arguments.of("email@email", Boolean.FALSE),
            Arguments.of("@email", Boolean.FALSE),
            Arguments.of("@email.com", Boolean.FALSE)
        );
    }

    private static Stream<Arguments> emailsValid() {
        return Stream.of(
            Arguments.of("testes@email.com", TRUE),
            Arguments.of("testes@email.com.br", TRUE),
            Arguments.of("testes.testes@email.com.br", TRUE),
            Arguments.of("testes_testes@email.com.br", TRUE),
            Arguments.of("testes-testes@email.com.br", TRUE),
            Arguments.of("testes-testes@email.domain.com.br", TRUE),
            Arguments.of("testes-testes@email-domain.com.br", TRUE)
        );
    }

    @ParameterizedTest(name = "deve retornar {1} quando e-mail for: [{0}]")
    @MethodSource("provideArgsToTestEmailValid")
    @DisplayName("Deve processar validação de e-mail com sucesso é retornar True ou False para o resultado")
    void shouldReturnTrueOrFalseWhenValidateEmail(String email, Boolean expectedResult) {
        var result = validatorEmail.isEmail(email);
        assertEquals(expectedResult, result);
    }

    @ParameterizedTest(name = "deve retornar {1} quando e-mail for: [{0}]")
    @MethodSource("provideArgusToEmailInstitutional")
    @DisplayName("Deve processar validação de e-mail instutional com sucesso é retornar True ou False para o resultado")
    void shouldReturnTrueOrFalseWhenValidateIsInstitutional(String email, Boolean expectedResult) {
        var result = validatorEmail.validateEmailStudents(email);
        assertEquals(expectedResult, result);
    }

    @Test
    @DisplayName("Deve retornar true ou false para quando email existe")
    void shouldReturnBooleanWhenEmailExist() {
        var email = "email";
        when(accessCardRepository.existsByUsername(email)).thenReturn(TRUE);

        var result = validatorEmail.isExistEmail(email);

        assertTrue(result);
        verify(accessCardRepository).existsByUsername(email);
    }
}

