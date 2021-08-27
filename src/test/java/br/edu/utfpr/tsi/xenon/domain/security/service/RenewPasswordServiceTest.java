package br.edu.utfpr.tsi.xenon.domain.security.service;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.awaitility.Durations.TWO_HUNDRED_MILLISECONDS;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.utfpr.tsi.xenon.application.config.property.ApplicationDomainProperty;
import br.edu.utfpr.tsi.xenon.application.dto.InputChangePasswordDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputRenewPasswordDto;
import br.edu.utfpr.tsi.xenon.domain.notification.model.EmailTemplate;
import br.edu.utfpr.tsi.xenon.domain.notification.service.SenderAdapter;
import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.structure.MessagesMapper;
import br.edu.utfpr.tsi.xenon.structure.exception.BusinessException;
import br.edu.utfpr.tsi.xenon.structure.repository.AccessCardRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.TokenRedisRepository;
import com.github.javafaker.Faker;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - RenewPasswordService")
class RenewPasswordServiceTest {

    /**
     * email_new_password@email.com-sault:8297eefa-d0fa-4d26-a506-b0e5c0bb9794
     */

    @Mock
    private SenderAdapter senderAdapter;

    @Mock
    private BCryptPasswordEncoder cryptPasswordEncoder;

    @Mock
    private AccessCardRepository accessCardRepository;

    @Mock
    private TokenRedisRepository tokenRedisRepository;

    @Mock
    private ApplicationDomainProperty applicationDomainProperty;

    @InjectMocks
    private RenewPasswordService renewPasswordService;

    @Test
    @DisplayName("Deve tratar qualquer exception lançada silenciosamente")
    void shouldCatchAnyExceptionInCheckSolicitation() {
        var email = Faker.instance().internet().emailAddress();
        var input = new InputRenewPasswordDto().email(email);
        var entity = new AccessCardEntity();
        entity.setId(1L);

        lenient()
            .doThrow(IllegalStateException.class)
            .when(tokenRedisRepository)
            .saveToken(anyString(), anyString(), eq(5L), eq(TimeUnit.HOURS));

        assertDoesNotThrow(() -> renewPasswordService.checkSolicitation(input));
    }

    @Test
    @DisplayName("Deve enviar e-mail com confirmação de pedido de nova senha")
    void shouldHaveSendConfirmRenewPassword() {
        var email = Faker.instance().internet().emailAddress();
        var input = new InputRenewPasswordDto().email(email);
        var entity = new AccessCardEntity();
        entity.setId(1L);

        when(accessCardRepository.findByUsername(input.getEmail()))
            .thenReturn(Optional.of(entity));
        doNothing()
            .when(tokenRedisRepository)
            .saveToken(anyString(), anyString(), eq(5L), eq(TimeUnit.HOURS));
        doNothing()
            .when(senderAdapter)
            .sendEmail(any(EmailTemplate.class));

        renewPasswordService.checkSolicitation(input);

        verify(accessCardRepository, timeout(TWO_HUNDRED_MILLISECONDS.toMillis()))
            .findByUsername(input.getEmail());
        verify(tokenRedisRepository, timeout(TWO_HUNDRED_MILLISECONDS.toMillis()))
            .saveToken(anyString(), anyString(), eq(5L), eq(TimeUnit.HOURS));
        verify(accessCardRepository, timeout(TWO_HUNDRED_MILLISECONDS.toMillis()))
            .findByUsername(input.getEmail());
        verify(senderAdapter, timeout(TWO_HUNDRED_MILLISECONDS.toMillis()))
            .sendEmail(any(EmailTemplate.class));
    }

    @Test
    @DisplayName("Deve enviar e-mail com nova senha")
    void shouldHaveSendNewPassword() {
        var params =
            "ZW1haWxfbmV3X3Bhc3N3b3JkQGVtYWlsLmNvbS1zYXVsdDo4Mjk3ZWVmYS1kMGZhLTRkMjYtYTUwNi1iMGU1YzBiYjk3OTQ=";

        var email = "email_new_password@email.com";
        var token = "8297eefa-d0fa-4d26-a506-b0e5c0bb9794";
        var entity = new AccessCardEntity();
        entity.setId(1L);

        when(tokenRedisRepository.findTokenByKey(contains(email))).thenReturn(token);
        when(accessCardRepository.findByUsername(email)).thenReturn(Optional.of(entity));
        when(cryptPasswordEncoder.encode(anyString())).thenReturn("password");
        when(accessCardRepository.saveAndFlush(entity)).thenReturn(entity);

        renewPasswordService.renewPassword(params);

        verify(accessCardRepository, timeout(TWO_HUNDRED_MILLISECONDS.toMillis()))
            .findByUsername(email);
        verify(tokenRedisRepository, timeout(TWO_HUNDRED_MILLISECONDS.toMillis()))
            .findTokenByKey(contains(email));
        verify(tokenRedisRepository, timeout(TWO_HUNDRED_MILLISECONDS.toMillis()))
            .delete(anyString());
        verify(accessCardRepository, timeout(TWO_HUNDRED_MILLISECONDS.toMillis()))
            .findByUsername(email);
        verify(cryptPasswordEncoder, timeout(TWO_HUNDRED_MILLISECONDS.toMillis()))
            .encode(anyString());
        verify(accessCardRepository, timeout(TWO_HUNDRED_MILLISECONDS.toMillis()))
            .saveAndFlush(entity);

    }

    @Test
    @DisplayName("Deve não processar renovação de senha quando access card não encontrado")
    void shouldNotProcessRenewPass() {
        var params =
            "ZW1haWxfbmV3X3Bhc3N3b3JkQGVtYWlsLmNvbS1zYXVsdDo4Mjk3ZWVmYS1kMGZhLTRkMjYtYTUwNi1iMGU1YzBiYjk3OTQ=";

        var email = "email_new_password@email.com";
        var token = "8297eefa-d0fa-4d26-a506-b0e5c0bb9794";
        when(tokenRedisRepository.findTokenByKey(contains(email))).thenReturn(token);
        when(accessCardRepository.findByUsername(email)).thenReturn(Optional.empty());

        renewPasswordService.renewPassword(params);

        verify(accessCardRepository, timeout(TWO_HUNDRED_MILLISECONDS.toMillis()))
            .findByUsername(email);
        verify(tokenRedisRepository, timeout(TWO_HUNDRED_MILLISECONDS.toMillis()))
            .findTokenByKey(contains(email));

        verify(tokenRedisRepository, never()).delete(anyString());
        verify(cryptPasswordEncoder, never()).encode(anyString());
        verify(accessCardRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Deve não processar renovação de senha quando access card token está inválido")
    void shouldNotProcessTokenIsInvalid() {
        var params =
            "ZW1haWxfbmV3X3Bhc3N3b3JkQGVtYWlsLmNvbS1zYXVsdDo4Mjk3ZWVmYS1kMGZhLTRkMjYtYTUwNi1iMGU1YzBiYjk3OTQ=";
        var email = "email_new_password@email.com";

        when(tokenRedisRepository.findTokenByKey(contains(email))).thenReturn(null);

        renewPasswordService.renewPassword(params);

        verify(tokenRedisRepository, timeout(TWO_HUNDRED_MILLISECONDS.toMillis()))
            .findTokenByKey(contains(email));

        verify(accessCardRepository, never()).findByUsername(email);
        verify(tokenRedisRepository, never()).delete(anyString());
        verify(cryptPasswordEncoder, never()).encode(anyString());
        verify(accessCardRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldCatchAnyExceptionInRenewPassword() {
        var params =
            "ZW1haWxfbmV3X3Bhc3N3b3JkQGVtYWlsLmNvbS1zYXVsdDo4Mjk3ZWVmYS1kMGZhLTRkMjYtYTUwNi1iMGU1YzBiYjk3OTQ=";

        lenient()
            .doThrow(IllegalStateException.class)
            .when(tokenRedisRepository)
            .findTokenByKey(anyString());

        assertDoesNotThrow(() -> renewPasswordService.renewPassword(params));
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando senha atual não combina com senha salva")
    void shouldThrowsBusinessExceptionWhenActualNotMatch() {
        var pass = Faker.instance().internet().password();
        var accessCard = new AccessCardEntity();
        accessCard.setPassword(pass);

        var input = new InputChangePasswordDto()
            .actualPassword("actual")
            .password("new_pass")
            .confirmPassword("new_pass");

        when(cryptPasswordEncoder.matches("actual", accessCard.getPassword())).thenReturn(FALSE);

        var exception = assertThrows(BusinessException.class, () -> renewPasswordService.changePassword(accessCard, input));

        assertEquals(MessagesMapper.PASS_ACTUAL_NOT_MATCH.getCode(), exception.getCode());
        assertEquals(422, exception.getStatus());

        verify(cryptPasswordEncoder).matches("actual", accessCard.getPassword());
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando senha atual não combina com senha salva")
    void shouldThrowsBusinessExceptionWhenPassAndConfirmNotMatch() {
        var faker = Faker.instance();
        var pass = faker.internet().password();
        var newPass = faker.internet().password();
        var confirmPass = faker.internet().password();

        var accessCard = new AccessCardEntity();
        accessCard.setPassword(pass);

        var input = new InputChangePasswordDto()
            .actualPassword(pass)
            .password(newPass)
            .confirmPassword(confirmPass);

        when(cryptPasswordEncoder.matches(pass, accessCard.getPassword())).thenReturn(TRUE);

        var exception = assertThrows(BusinessException.class, () -> renewPasswordService.changePassword(accessCard, input));

        assertEquals(MessagesMapper.PASS_AND_CONFIRM_NOT_MATCH.getCode(), exception.getCode());
        assertEquals(422, exception.getStatus());

        verify(cryptPasswordEncoder).matches(pass, accessCard.getPassword());
    }
}
