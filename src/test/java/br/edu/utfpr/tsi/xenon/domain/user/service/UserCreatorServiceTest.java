package br.edu.utfpr.tsi.xenon.domain.user.service;

import static br.edu.utfpr.tsi.xenon.domain.user.factory.TypeUser.STUDENTS;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import br.edu.utfpr.tsi.xenon.application.dto.InputRegistryStudentDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputUserDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputUserDto.TypeUserEnum;
import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.domain.security.entity.RoleEntity;
import br.edu.utfpr.tsi.xenon.domain.user.aggregator.AccessCardAggregator;
import br.edu.utfpr.tsi.xenon.domain.user.aggregator.AvatarAggregator;
import br.edu.utfpr.tsi.xenon.domain.user.aggregator.CarsAggregator;
import br.edu.utfpr.tsi.xenon.domain.user.aggregator.RolesAggregator;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.factory.TypeUser;
import com.github.javafaker.Faker;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - UserService")
class UserCreatorServiceTest {

    @Mock
    private AccessCardAggregator accessCardAggregator;

    @Mock
    private RolesAggregator rolesAggregator;

    @Mock
    private CarsAggregator carsAggregator;

    @Mock
    private AvatarAggregator avatarAggregator;

    @InjectMocks
    private UserCreatorService userCreatorService;

    @Test
    @DisplayName("Deve salvar usu??rio do tipo estudante")
    void shouldHaveSaveStudents() {
        var faker = Faker.instance();
        var input = new InputRegistryStudentDto()
            .email(faker.internet().emailAddress())
            .password("abc12345")
            .confirmPassword("abc12345");

        var entity = getUserEntity();
        includeAccessCard(entity);
        includeCars(entity);

        doNothing()
            .when(accessCardAggregator)
            .includeAccessCard(
                any(UserEntity.class),
                eq(input.getEmail()),
                eq(input.getPassword()),
                eq(input.getConfirmPassword()));
        doNothing()
            .when(rolesAggregator)
            .includeRoles(any(), eq(STUDENTS), eq(List.of(1L)));
        doNothing()
            .when(avatarAggregator)
            .includeDefaultAvatarUrl(any(UserEntity.class));

        var userCreated = userCreatorService.createNewStudents(input);

        assertNotNull(userCreated);

        verify(accessCardAggregator)
            .includeAccessCard(any(),
                eq(input.getEmail()),
                eq(input.getPassword()),
                eq(input.getConfirmPassword()));
        verify(avatarAggregator)
            .includeDefaultAvatarUrl(any(UserEntity.class));
        verify(rolesAggregator).includeRoles(any(), eq(STUDENTS), eq(List.of(1L)));
    }

    @Test
    @DisplayName("Deve criar um usu??rio com sucesso")
    void shouldHaveCreateNewUser() {
        var faker = Faker.instance();
        var input = new InputUserDto()
            .email(faker.internet().emailAddress())
            .name(faker.name().fullName())
            .typeUser(TypeUserEnum.STUDENTS)
            .addRolesItem(1L)
            .enabled(TRUE)
            .authorisedAccess(TRUE);

        var entity = getUserEntity();
        includeAccessCard(entity);
        includeCars(entity);

        doNothing()
            .when(accessCardAggregator)
            .includeAccessCard(
                any(UserEntity.class),
                eq(input.getEmail()),
                eq("pass"),
                eq("pass"));
        doNothing()
            .when(rolesAggregator)
            .includeRoles(any(), eq(STUDENTS), eq(List.of(1L)));
        doNothing()
            .when(avatarAggregator)
            .includeDefaultAvatarUrl(any(UserEntity.class));

        userCreatorService.createNewUser(input, "pass");

        verify(accessCardAggregator).includeAccessCard(
            any(UserEntity.class),
            eq(input.getEmail()),
            eq("pass"),
            eq("pass"));

        verify(rolesAggregator).includeRoles(any(), eq(STUDENTS), eq(List.of(1L)));
    }

    private UserEntity getUserEntity() {
        var entity = new UserEntity();
        entity.setId(1L);
        entity.setTypeUser(TypeUser.SPEAKER.name());
        return entity;
    }

    private void includeAccessCard(UserEntity entity) {
        var accessCard = new AccessCardEntity();
        var role = new RoleEntity();
        role.setId(1L);
        role.setName("ROLE_TEST");
        accessCard.setRoleEntities(List.of(role));
        entity.setAccessCard(accessCard);
    }

    private void includeCars(UserEntity entity) {
        var car = new CarEntity();
        car.setId(1L);
        car.setNumberAccess(0);
        var cars = new LinkedList<CarEntity>();
        cars.add(car);
        entity.setCar(cars);
    }
}
