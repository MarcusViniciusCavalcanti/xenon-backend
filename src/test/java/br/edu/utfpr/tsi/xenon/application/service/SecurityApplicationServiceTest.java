package br.edu.utfpr.tsi.xenon.application.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.utfpr.tsi.xenon.application.dto.InputChangePasswordDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputLoginDto;
import br.edu.utfpr.tsi.xenon.application.dto.TokenDto;
import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.domain.security.service.AccessTokenService;
import br.edu.utfpr.tsi.xenon.domain.security.service.SecurityContextUserService;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.structure.exception.ResourceNotFoundException;
import br.edu.utfpr.tsi.xenon.structure.repository.UserRepository;
import com.github.javafaker.Faker;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - SecurityApplicationService")
class SecurityApplicationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AccessTokenService accessTokenService;

    @Mock
    private SecurityContextUserService securityContextUserService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SecurityApplicationService service;

    @Test
    @DisplayName("Deve executar o processo de login com sucesso")
    void shouldHaveAuthenticated() {
        var input = new InputLoginDto()
            .email(Faker.instance().internet().emailAddress())
            .password("123456789");
        var accessCardEntity = new AccessCardEntity();

        var authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(accessCardEntity);
        when(accessTokenService.create(accessCardEntity)).thenReturn(new TokenDto());

        service.processSignIn(input);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(authentication).getPrincipal();
        verify(accessTokenService).create(accessCardEntity);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando usuário não existe")
    void shouldResourceNotFoundExceptionWhenUserNotFound() {
        when(securityContextUserService.getUserByContextSecurity(any()))
            .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> service.changePass(new InputChangePasswordDto(), "token"));
    }
}
