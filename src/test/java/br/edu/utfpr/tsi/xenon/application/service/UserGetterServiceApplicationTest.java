package br.edu.utfpr.tsi.xenon.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import br.edu.utfpr.tsi.xenon.domain.security.service.SecurityContextUserService;
import br.edu.utfpr.tsi.xenon.structure.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - UserGetterServiceApplication")
class UserGetterServiceApplicationTest {

    @Mock
    private SecurityContextUserService securityContextUserService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserGetterServiceApplication userGetterServiceApplication;

    @Test
    @DisplayName("Deve tratar silenciosamente qualquer exception")
    void shouldCatchAnyException() {
        lenient()
            .doThrow(NullPointerException.class)
            .when(securityContextUserService)
            .getUserByContextSecurity(any());

        assertDoesNotThrow(() -> userGetterServiceApplication.getUserByToken("token"));
    }

    @Test
    @DisplayName("Deve retornar o usuário vazio quando usuário uma exception for lançada")
    void shouldReturnUserEmptyWhenThrowsException() {
        assertDoesNotThrow(() -> userGetterServiceApplication.getUserByToken("token"));
        verify(userRepository, never()).findByAccessCard(any());
    }
}
