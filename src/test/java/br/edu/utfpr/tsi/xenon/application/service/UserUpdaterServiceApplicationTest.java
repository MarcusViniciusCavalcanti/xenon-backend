package br.edu.utfpr.tsi.xenon.application.service;

import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.NAME_CHANGED_SUCCESSFULLY;
import static java.lang.Boolean.FALSE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.utfpr.tsi.xenon.application.dto.InputNameUserDto;
import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.domain.security.entity.RoleEntity;
import br.edu.utfpr.tsi.xenon.domain.security.service.SecurityContextUserService;
import br.edu.utfpr.tsi.xenon.domain.user.aggregator.AvatarAggregator;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.factory.TypeUser;
import br.edu.utfpr.tsi.xenon.structure.exception.BusinessException;
import br.edu.utfpr.tsi.xenon.structure.repository.UserRepository;
import com.github.javafaker.Faker;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
}
