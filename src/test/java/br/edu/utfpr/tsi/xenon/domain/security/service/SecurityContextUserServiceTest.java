package br.edu.utfpr.tsi.xenon.domain.security.service;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.domain.security.entity.RoleEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.factory.TypeUser;
import br.edu.utfpr.tsi.xenon.structure.exception.TokenInvalidException;
import br.edu.utfpr.tsi.xenon.structure.repository.UserRepository;
import com.github.javafaker.Faker;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SecurityContextUserService service;

    @Test
    @DisplayName("Deve lançar AuthorizationServiceException quando token for invalido")
    void shouldThrowsExceptionWhenTokenIsInvalid() {
        var token = RandomStringUtils.random(10);
        when(tokenCreator.isValid(token)).thenReturn(FALSE);

        assertThrows(TokenInvalidException.class,
            () -> service.receiveTokenToSecurityHolder(token));

        verify(accessCardRepository, never()).loadUserByUsername(anyString());
    }

    @Test
    @DisplayName("Deve Lançar BadCredentialsException quando token não contem subject")
    void shouldThrowBadCredentialsExceptionWhenTokenIsSubject() {
        var token = RandomStringUtils.random(10);
        when(tokenCreator.isValid(token)).thenReturn(TRUE);
        when(tokenCreator.getEmail(token)).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class,
            () -> service.receiveTokenToSecurityHolder(token));

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

        assertThrows(BadCredentialsException.class,
            () -> service.receiveTokenToSecurityHolder(token));

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

    @Test
    @DisplayName("Deve retornar o usuário dono do token")
    void shouldReturnUserOwnerToken() {
        var authentication = mock(Authentication.class);
        var securityContext = mock(SecurityContext.class);
        var role = new RoleEntity();
        var accessCard = new AccessCardEntity();
        accessCard.setRoleEntities(List.of(role));
        var user = new UserEntity();
        user.setAccessCard(accessCard);
        user.setTypeUser(TypeUser.STUDENTS.name());

        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(accessCard);
        when(userRepository.findByAccessCard(accessCard)).thenReturn(Optional.of(user));

        service.getUserByContextSecurity("token");

        verify(securityContext).getAuthentication();
        verify(authentication).getPrincipal();
        verify(userRepository).findByAccessCard(accessCard);
    }
}
