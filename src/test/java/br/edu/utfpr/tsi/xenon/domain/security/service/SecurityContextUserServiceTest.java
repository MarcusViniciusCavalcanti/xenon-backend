package br.edu.utfpr.tsi.xenon.domain.security.service;

import static java.lang.Boolean.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import com.github.javafaker.Faker;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - SecurityContextUserService")
class SecurityContextUserServiceTest {

    @Mock
    private AccessTokenService tokenCreator;

    @Mock
    private UserDetailsService accessCardRepository;

    @InjectMocks
    private SecurityContextUserService service;

    @Test
    @DisplayName("Deve lançar AuthorizationServiceException quando token for invalido")
    void shouldThrowsExceptionWhenTokenIsInvalid() {
        var token = RandomStringUtils.random(10);
        when(tokenCreator.isValid(token)).thenReturn(FALSE);

        assertThrows(AuthorizationServiceException.class, () -> service.receiveTokenToSecurityHolder(token));

        verify(accessCardRepository, never()).loadUserByUsername(anyString());
    }

    @Test
    @DisplayName("Deve Lançar BadCredentialsException quando token não contem subject")
    void shouldThrowBadCredentialsExceptionWhenTokenIsSubject() {
        var token = RandomStringUtils.random(10);
        when(tokenCreator.isValid(token)).thenReturn(TRUE);
        when(tokenCreator.getEmail(token)).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> service.receiveTokenToSecurityHolder(token));

        verify(tokenCreator).isValid(token);
        verify(tokenCreator).getEmail(token);
        verify(accessCardRepository, never()).loadUserByUsername(anyString());
    }

    @Test
    @DisplayName("Deve Lançar BadCredentialsException quando token não contem subject")
    void shouldThrowBadCredentialsExceptionWhenUserDetailsNotFound() {
        var token = RandomStringUtils.random(10);
        var email = Faker.instance().internet().emailAddress();
        when(tokenCreator.isValid(token)).thenReturn(TRUE);
        when(tokenCreator.getEmail(token)).thenReturn(Optional.of(email));
        when(accessCardRepository.loadUserByUsername(email)).thenReturn(null);

        assertThrows(BadCredentialsException.class, () -> service.receiveTokenToSecurityHolder(token));

        verify(tokenCreator).isValid(token);
        verify(tokenCreator).getEmail(token);
        verify(accessCardRepository).loadUserByUsername(anyString());
    }

    @Test
    @DisplayName("Deve Autenticar com sucesso")
    void shouldHaveAuthenticatedSuccessfully() {
        var token = RandomStringUtils.random(10);
        var email = Faker.instance().internet().emailAddress();
        var accessCard = new AccessCardEntity();

        var authentication = mock(Authentication.class);
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(tokenCreator.isValid(token)).thenReturn(TRUE);
        when(tokenCreator.getEmail(token)).thenReturn(Optional.of(email));
        when(accessCardRepository.loadUserByUsername(email)).thenReturn(accessCard);

        service.receiveTokenToSecurityHolder(token);

        verify(tokenCreator).isValid(token);
        verify(tokenCreator).getEmail(token);
        verify(accessCardRepository).loadUserByUsername(anyString());

        assertEquals(authentication, SecurityContextHolder.getContext().getAuthentication());
    }
}
