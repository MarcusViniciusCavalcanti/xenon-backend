package br.edu.utfpr.tsi.xenon.application.service;

import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.NAME_CHANGED_SUCCESSFULLY;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.utfpr.tsi.xenon.application.dto.InputAccessUserDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputNameUserDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputUpdateUserDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputUpdateUserDto.TypeUserEnum;
import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.domain.security.entity.RoleEntity;
import br.edu.utfpr.tsi.xenon.domain.security.service.SecurityContextUserService;
import br.edu.utfpr.tsi.xenon.domain.user.aggregator.AvatarAggregator;
import br.edu.utfpr.tsi.xenon.domain.user.aggregator.RolesAggregator;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.factory.TypeUser;
import br.edu.utfpr.tsi.xenon.domain.user.service.ValidatorEmail;
import br.edu.utfpr.tsi.xenon.structure.MessagesMapper;
import br.edu.utfpr.tsi.xenon.structure.exception.BusinessException;
import br.edu.utfpr.tsi.xenon.structure.exception.ResourceNotFoundException;
import br.edu.utfpr.tsi.xenon.structure.repository.UserRepository;
import com.github.javafaker.Faker;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
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
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - UserUpdaterServiceApplication")
class UserUpdaterServiceApplicationTest {

    @Mock
    private SecurityContextUserService securityContextUserService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AvatarAggregator avatarAggregator;

    @Mock
    private RolesAggregator rolesAggregator;

    @Mock
    private ValidatorEmail validatorEmail;

    @InjectMocks
    private UserUpdaterServiceApplication userUpdaterServiceApplication;

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
        var result = userUpdaterServiceApplication.changeName(input, "token");

        assertEquals(NAME_CHANGED_SUCCESSFULLY.getCode(), result.getResult());
        verify(userRepository).existsByName(nameChanged);
        verify(userRepository).saveAndFlush(user);
    }

    @Test
    @DisplayName("deve retornar sucesso mesmo que usuário usuário não for encontrado")
    void shouldReturnSuccessSameUserNotFound() {
        when(securityContextUserService.getUserByContextSecurity(any())).thenReturn(Optional.empty());

        userUpdaterServiceApplication.changeName(new InputNameUserDto().name("name"), "token");

        verify(userRepository, never()).existsByName(any());
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Deve incluir avatar com sucesso")
    void shouldHaveIncludeAvatarSuccessfully() throws IOException {
        var multipart = mock(MultipartFile.class);
        var role = new RoleEntity();
        var accessCard = new AccessCardEntity();
        accessCard.setRoleEntities(List.of(role));
        var user = new UserEntity();
        user.setName("name");
        user.setAccessCard(accessCard);
        user.setTypeUser(TypeUser.STUDENTS.name());

        when(securityContextUserService.getUserByContextSecurity("token")).thenReturn(Optional.of(user));
        doNothing()
            .when(avatarAggregator)
            .includeAvatar(any(), eq(user));

        userUpdaterServiceApplication.changeAvatar(multipart, "token");

        verify(userRepository).saveAndFlush(any());
        verify(avatarAggregator).includeAvatar(any(), eq(user));
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando algo falhar")
    void shouldThrowsBusinessExceptionWhenFileNotImage() throws IOException {
        var multipart = mock(MultipartFile.class);
        var role = new RoleEntity();
        var accessCard = new AccessCardEntity();
        accessCard.setRoleEntities(List.of(role));
        var user = new UserEntity();
        user.setName("name");
        user.setAccessCard(accessCard);
        user.setTypeUser(TypeUser.STUDENTS.name());

        doThrow(IOException.class)
            .when(multipart)
            .transferTo(any(Path.class));
        when(securityContextUserService.getUserByContextSecurity("token")).thenReturn(Optional.of(user));

        assertThrows(BusinessException.class,
            () -> userUpdaterServiceApplication.changeAvatar(multipart, "token"));

        verify(userRepository, never()).saveAndFlush(any());
        verify(avatarAggregator, never()).includeAvatar(any(), eq(user));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando usuário não exite")
    void shouldThrowsResourceNotFoundExceptionWhenUserNotFoundInDatabase() {
        var id = 1L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        var exception = assertThrows(ResourceNotFoundException.class,
            () -> userUpdaterServiceApplication.updateUser(new InputUpdateUserDto(), id));

        assertEquals("usuário", exception.getResourceName());
        assertEquals("id", exception.getArgumentSearch());
    }

    @Test
    @DisplayName("Deve atualizar usuário com sucesso")
    void shouldHaveUpdateUserSuccessfully() {
        var faker = Faker.instance();
        var name = faker.name().fullName();
        var email = faker.internet().emailAddress();
        var id = 1L;

        var input = new InputUpdateUserDto()
            .name(name)
            .email(email)
            .addRolesItem(2L)
            .addRolesItem(3L)
            .typeUser(TypeUserEnum.SERVICE)
            .enabled(FALSE)
            .authorisedAccess(FALSE)
            .disableReason("reason");

        var user = new UserEntity();
        user.setTypeUser(TypeUser.SERVICE.name());
        user.setName("name");

        var accessCard = new AccessCardEntity();
        accessCard.setUsername("email");
        user.setAccessCard(accessCard);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(validatorEmail.isEmail(email)).thenReturn(TRUE);
        when(validatorEmail.isExistEmail(email)).thenReturn(FALSE);
        when(userRepository.saveAndFlush(user)).thenReturn(user);
        doNothing()
            .when(rolesAggregator)
            .includeRoles(accessCard,  TypeUser.valueOf(input.getTypeUser().name()), input.getRoles());

        var result = userUpdaterServiceApplication.updateUser(input, id);

        assertEquals(name, result.getName());
        assertEquals(email, result.getEmail());
        assertEquals(input.getTypeUser().name(), result.getType().name());
        assertFalse(result.getEnabled());
        assertFalse(result.getAuthorisedAccess());
        assertEquals("reason", result.getDisableReason());

        verify(userRepository).existsByName(input.getName());
        verify(userRepository).findById(id);
        verify(validatorEmail).isEmail(email);
        verify(validatorEmail, never()).validateEmailStudents(email);
        verify(validatorEmail).isExistEmail(email);
        verify(userRepository).saveAndFlush(user);
        verify(rolesAggregator).includeRoles(accessCard,  TypeUser.valueOf(input.getTypeUser().name()), input.getRoles());
    }

    @ParameterizedTest
    @MethodSource("providerInput")
    @DisplayName("Deve lançar BusinessException quando reason não foi preenchido")
    void shouldThrowsBusinessExceptionWhenReasonIsEmpty(InputUpdateUserDto input) {
        var id = 1L;
        when(userRepository.findById(id)).thenReturn(Optional.of(new UserEntity()));

        var exception = assertThrows(BusinessException.class,
            () -> userUpdaterServiceApplication.updateUser(input, id));

        assertEquals(400, exception.getStatus());
        assertEquals(MessagesMapper.REASON_IS_EMPTY.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("Não deve alterar nem validar nome quando é o mesmo")
    void shouldNotHaveChangeName() {
        var faker = Faker.instance();
        var name = faker.name().fullName();
        var email = faker.internet().emailAddress();
        var id = 1L;

        var input = new InputUpdateUserDto()
            .name(name)
            .email(email)
            .addRolesItem(id)
            .typeUser(TypeUserEnum.STUDENTS)
            .enabled(TRUE)
            .authorisedAccess(TRUE);

        var user = new UserEntity();
        user.setTypeUser(TypeUser.SERVICE.name());
        user.setName(name);

        var accessCard = new AccessCardEntity();
        accessCard.setUsername("email");
        user.setAccessCard(accessCard);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(validatorEmail.isEmail(email)).thenReturn(TRUE);
        when(validatorEmail.validateEmailStudents(email)).thenReturn(TRUE);
        when(validatorEmail.isExistEmail(email)).thenReturn(FALSE);
        when(userRepository.saveAndFlush(user)).thenReturn(user);
        doNothing()
            .when(rolesAggregator)
            .includeRoles(accessCard,  TypeUser.valueOf(input.getTypeUser().name()), input.getRoles());

        var result = userUpdaterServiceApplication.updateUser(input, id);

        assertEquals(name, result.getName());

        verify(userRepository, never()).existsByName(input.getName());
        verify(userRepository).findById(id);
        verify(validatorEmail).isEmail(email);
        verify(validatorEmail).validateEmailStudents(email);
        verify(validatorEmail).isExistEmail(email);
        verify(userRepository).saveAndFlush(user);
        verify(rolesAggregator).includeRoles(accessCard,  TypeUser.valueOf(input.getTypeUser().name()), input.getRoles());
    }

    @Test
    @DisplayName("Não deve altera email quando é o mesmo")
    void shouldNotHaveChangeEmail() {
        var faker = Faker.instance();
        var name = faker.name().fullName();
        var email = faker.internet().emailAddress();
        var id = 1L;

        var input = new InputUpdateUserDto()
            .name(name)
            .email(email)
            .addRolesItem(id)
            .typeUser(TypeUserEnum.STUDENTS)
            .enabled(TRUE)
            .authorisedAccess(TRUE);

        var user = new UserEntity();
        user.setTypeUser(TypeUser.SERVICE.name());
        user.setName("name");

        var accessCard = new AccessCardEntity();
        accessCard.setUsername(email);
        user.setAccessCard(accessCard);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.existsByName(input.getName())).thenReturn(FALSE);
        when(userRepository.saveAndFlush(user)).thenReturn(user);
        doNothing()
            .when(rolesAggregator)
            .includeRoles(accessCard,  TypeUser.valueOf(input.getTypeUser().name()), input.getRoles());

        var result = userUpdaterServiceApplication.updateUser(input, id);

        assertEquals(email, result.getEmail());

        verify(userRepository).existsByName(input.getName());
        verify(userRepository).findById(id);
        verify(validatorEmail, never()).isEmail(email);
        verify(validatorEmail, never()).validateEmailStudents(email);
        verify(validatorEmail, never()).isExistEmail(email);
        verify(userRepository).saveAndFlush(user);
        verify(rolesAggregator).includeRoles(accessCard,  TypeUser.valueOf(input.getTypeUser().name()), input.getRoles());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException na desativação do usuário quando não encontrado")
    void shouldThrowsResourceNotFoundExceptionInDisabledUserWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        var exception = assertThrows(ResourceNotFoundException.class,
            () -> userUpdaterServiceApplication.updateUser(new InputUpdateUserDto(), 1L));

        assertEquals("usuário", exception.getResourceName());
        assertEquals("id", exception.getArgumentSearch());
    }

    @Test
    @DisplayName("Deve desautorizar acesso do usuário com sucesso")
    void shouldHaveUnauthorizedAccessSuccessfully() {
        var faker = Faker.instance();
        var email = faker.internet().emailAddress();
        var id = 1L;

        var user = new UserEntity();
        user.setTypeUser(TypeUser.SERVICE.name());
        user.setName("name");

        var accessCard = new AccessCardEntity();
        accessCard.setUsername(email);
        user.setAccessCard(accessCard);

        var input = new InputAccessUserDto()
            .reason("usuário desativado")
            .userId(1L);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.saveAndFlush(user)).thenReturn(user);

        userUpdaterServiceApplication.unauthorizedAccess(input);

        verify(userRepository).findById(id);
        verify(userRepository).saveAndFlush(user);
    }

    @Test
    @DisplayName("Deve lançar ResourceException na tentativa de desautorizar o acesso do usuário")
    void shouldThrowsResourceExceptionWhenUnauthorizedAccess() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> userUpdaterServiceApplication.unauthorizedAccess(new InputAccessUserDto()));

        verify(userRepository).findById(any());
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Deve adicionar autorização para usuário")
    void shouldHaveAddAuthorizationAccess() {
        var faker = Faker.instance();
        var email = faker.internet().emailAddress();
        var id = 1L;

        var user = new UserEntity();
        user.setTypeUser(TypeUser.SERVICE.name());
        user.setName("name");

        var accessCard = new AccessCardEntity();
        accessCard.setUsername(email);
        user.setAccessCard(accessCard);

        var input = new InputAccessUserDto().userId(1L);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        var result = userUpdaterServiceApplication.authorizedAccess(input);

        assertEquals(MessagesMapper.ADD_AUTHORIZATION_ACCESS.getCode(), result.getResult());
        verify(userRepository).saveAndFlush(user);
        verify(userRepository).findById(id);
    }

    @Test
    @DisplayName("Deve lançar ResourceException na tentativa de adicionar autorização e o usuário não existe")
    void shouldThrowsResourceExceptionInAuthorizationWhenUserNotFound() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> userUpdaterServiceApplication.authorizedAccess(new InputAccessUserDto()));

        verify(userRepository).findById(any());
        verify(userRepository, never()).saveAndFlush(any());
    }

    private static Stream<Arguments> providerInput() {
        return Stream.of(
            Arguments.of(new InputUpdateUserDto().authorisedAccess(FALSE)),
            Arguments.of(new InputUpdateUserDto().authorisedAccess(TRUE).enabled(FALSE))
        );
    }
}
