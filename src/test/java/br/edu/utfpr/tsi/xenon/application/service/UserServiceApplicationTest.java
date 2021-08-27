package br.edu.utfpr.tsi.xenon.application.service;

import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.NAME_CHANGED_SUCCESSFULLY;
import static java.lang.Boolean.FALSE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.utfpr.tsi.xenon.application.dto.InputNameUserDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputNewCarDto;
import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.domain.security.entity.RoleEntity;
import br.edu.utfpr.tsi.xenon.domain.security.service.SecurityContextUserService;
import br.edu.utfpr.tsi.xenon.domain.user.aggregator.CarsAggregator;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.factory.TypeUser;
import br.edu.utfpr.tsi.xenon.domain.user.service.ValidatorEmail;
import br.edu.utfpr.tsi.xenon.structure.MessagesMapper;
import br.edu.utfpr.tsi.xenon.structure.exception.BusinessException;
import br.edu.utfpr.tsi.xenon.structure.exception.ResourceNotFoundException;
import br.edu.utfpr.tsi.xenon.structure.repository.UserRepository;
import com.github.javafaker.Faker;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - UserServiceApplication")
class UserServiceApplicationTest {

    @Mock
    private ValidatorEmail validatorEmail;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContextUserService securityContextUserService;

    @InjectMocks
    private UserServiceApplication userServiceApplication;

    @Test
    @DisplayName("Deve retornar o usuário vazio quando usuário uma exception for lançada")
    void shouldReturnUserEmptyWhenThrowsException() {
        assertDoesNotThrow(() -> userServiceApplication.getUserByToken("token"));
        verify(userRepository, never()).findByAccessCard(any());
    }

    @Test
    @DisplayName("Deve alterar o nome do usuário com sucesso")
    void shouldHaveChangeNameSuccessfully() {
        var name = Faker.instance().name().fullName();
        var nameChanged = Faker.instance().name().fullName();

        var role = new RoleEntity();
        var accessCard = new AccessCardEntity();
        accessCard.setRoleEntities(List.of(role));
        var user = new UserEntity();
        user.setName(name);
        user.setAccessCard(accessCard);
        user.setTypeUser(TypeUser.STUDENTS.name());

        when(securityContextUserService.getUserByContextSecurity("token")).thenReturn(Optional.of(user));
        when(userRepository.existsByName(nameChanged)).thenReturn(FALSE);

        var input = new InputNameUserDto().name(nameChanged);
        var result = userServiceApplication.changeName(input, "token");

        assertEquals(NAME_CHANGED_SUCCESSFULLY.getCode(), result.getResult());
        verify(userRepository).existsByName(nameChanged);
        verify(userRepository).saveAndFlush(user);
    }

    @Test
    @DisplayName("deve retornar sucesso mesmo que usuário usuário não for encontrado")
    void shouldReturnSuccessSameUserNotFound() {
        when(securityContextUserService.getUserByContextSecurity(any())).thenReturn(Optional.empty());

        userServiceApplication.changeName(new InputNameUserDto().name("name"), "token");

        verify(userRepository, never()).existsByName(any());
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Deve tratar silenciosamente qualquer exception")
    void shouldCatchAnyException() {
        lenient()
            .doThrow(NullPointerException.class)
            .when(securityContextUserService)
            .getUserByContextSecurity(any());

        assertDoesNotThrow(() -> userServiceApplication.getUserByToken("token"));
    }
}

