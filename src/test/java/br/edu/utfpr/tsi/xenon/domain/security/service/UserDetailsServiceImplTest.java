package br.edu.utfpr.tsi.xenon.domain.security.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.structure.repository.AccessCardRepository;
import com.github.javafaker.Faker;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - UserDetailsServiceImpl")
class UserDetailsServiceImplTest {

    @Mock
    private AccessCardRepository accessCardRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException quando usuário não encontrado")
    void shouldThrowsUsernameNotFoundException() {
        var email = Faker.instance().internet().emailAddress();
        when(accessCardRepository.findByUsername(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(email));

        verify(accessCardRepository).findByUsername(email);
    }

    @Test
    @DisplayName("Deve retornar access card do usuário")
    void shouldReturnAccessCard() {
        var accessCard = new AccessCardEntity();
        var email = Faker.instance().internet().emailAddress();
        when(accessCardRepository.findByUsername(email)).thenReturn(Optional.of(accessCard));

        var userDetails = userDetailsService.loadUserByUsername(email);

        assertEquals(accessCard, userDetails);

        verify(accessCardRepository).findByUsername(email);
    }
}
