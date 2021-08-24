package br.edu.utfpr.tsi.xenon.domain.user.aggregator;

import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.PASSWORD_INVALID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.structure.exception.RegistryUserException;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - AccessCardAggregator")
class AccessCardAggregatorTest {

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private AccessCardAggregator accessCardAggregator;

    @Test
    @DisplayName("Deve lançar RegistryUserException quando senha e confirmação de senha não confere")
    void shouldRegistryUserException() {
        var user = new UserEntity();

        var exception = assertThrows(RegistryUserException.class,
            () -> accessCardAggregator.includeAccessCard(user, "email@email.com", "abc", "cba"));

        assertEquals(PASSWORD_INVALID.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("Deve incluir access card")
    void shouldIncludeAccessCard() {
        var user = new UserEntity();
        var email = Faker.instance().internet().emailAddress();
        var pass = "12344567";

        when(bCryptPasswordEncoder.encode(pass)).thenReturn(pass);

        accessCardAggregator.includeAccessCard(user, email, pass, pass);

        assertEquals(user.getAccessCard().getPassword(), pass);
        assertEquals(user.getAccessCard().getUsername(), email);
        assertTrue(user.getAccessCard().isEnabled());

        verify(bCryptPasswordEncoder).encode(pass);
    }
}
