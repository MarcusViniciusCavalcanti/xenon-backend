package br.edu.utfpr.tsi.xenon.application.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.utfpr.tsi.xenon.application.dto.InputLoginDto;
import br.edu.utfpr.tsi.xenon.application.dto.TokenDto;
import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.domain.security.service.AccessTokenService;
import com.github.javafaker.Faker;
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
}
