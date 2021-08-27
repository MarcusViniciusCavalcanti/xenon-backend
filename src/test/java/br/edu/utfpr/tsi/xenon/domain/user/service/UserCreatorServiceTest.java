package br.edu.utfpr.tsi.xenon.domain.user.service;

import static br.edu.utfpr.tsi.xenon.domain.user.factory.TypeUser.STUDENTS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import br.edu.utfpr.tsi.xenon.application.dto.InputRegistryStudentDto;
import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.domain.security.entity.RoleEntity;
import br.edu.utfpr.tsi.xenon.domain.user.aggregator.AccessCardAggregator;
import br.edu.utfpr.tsi.xenon.domain.user.aggregator.AvatarAggregator;
import br.edu.utfpr.tsi.xenon.domain.user.aggregator.CarsAggregator;
import br.edu.utfpr.tsi.xenon.domain.user.aggregator.RolesAggregator;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.factory.TypeUser;
import br.edu.utfpr.tsi.xenon.structure.repository.UserRepository;
import com.github.javafaker.Faker;
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
    private UserRepository userRepository;

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
    @DisplayName("Deve salvar usuário do tipo estudante")
    void shouldHaveSaveStudents() {
        var faker = Faker.instance();
        var input = new InputRegistryStudentDto()
            .email(faker.internet().emailAddress())
            .password("abc12345")
            .confirmPassword("abc12345")
            .plateCar(faker.bothify("???-####"))
            .modelCar("Model car");

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
            .when(carsAggregator)
            .includeNewCar(any(UserEntity.class), eq(input.getModelCar()), eq(input.getPlateCar()));
        doNothing()
            .when(rolesAggregator)
            .includeRoles(any(), eq(STUDENTS), eq(List.of(1L)));
        doNothing()
            .when(avatarAggregator)
            .includeDefaultAvatarUrl(any(UserEntity.class));

        var userCreated = userCreatorService.registryNewStudent(input);

        assertNotNull(userCreated);

        verify(accessCardAggregator)
            .includeAccessCard(any(),
                eq(input.getEmail()),
                eq(input.getPassword()),
                eq(input.getConfirmPassword()));
        verify(carsAggregator)
            .includeNewCar(any(), eq(input.getModelCar()), eq(input.getPlateCar()));
        verify(carsAggregator)
            .includeNewCar(any(), eq(input.getModelCar()), eq(input.getPlateCar()));
        verify(avatarAggregator)
            .includeDefaultAvatarUrl(any(UserEntity.class));
    }

//    @Test
//    @DisplayName("Deve lançar exception quando recurso não existe")
//    void shouldThrowsExceptionWhenUserNotFound() {
//        var id = 1L;
//        when(userRepository.findById(id)).thenReturn(Optional.empty());
//
//        var exception = assertThrows(ResourceNotFoundException.class, () -> userCreatorService.findById(id));
//
//        assertEquals("usuário", exception.getResourceName());
//        assertEquals("userId: 1", exception.getArgumentSearch());
//    }

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
        entity.setCar(List.of(car));
    }
}
