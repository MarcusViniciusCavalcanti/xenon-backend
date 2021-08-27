package br.edu.utfpr.tsi.xenon.application.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.utfpr.tsi.xenon.application.dto.InputNewCarDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputRemoveCarDto;
import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.domain.security.entity.RoleEntity;
import br.edu.utfpr.tsi.xenon.domain.security.service.SecurityContextUserService;
import br.edu.utfpr.tsi.xenon.domain.user.aggregator.CarsAggregator;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.factory.TypeUser;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - CarApplicationService")
class CarApplicationServiceTest {

    @Mock
    private SecurityContextUserService securityContextUserService;

    @Mock
    private CarsAggregator carsAggregator;

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private CarApplicationService carApplicationService;

    @Test
    @DisplayName("Deve incluir novo carro com sucesso")
    void shouldHaveIncludeNewCarSuccessfully() {
        var role = new RoleEntity();
        var accessCard = new AccessCardEntity();
        accessCard.setRoleEntities(List.of(role));
        var user = new UserEntity();
        user.setAccessCard(accessCard);
        user.setTypeUser(TypeUser.STUDENTS.name());

        var input = new InputNewCarDto()
            .model("model")
            .plate("plate");

        when(securityContextUserService.getUserByContextSecurity("token"))
            .thenReturn(Optional.of(user));
        when(carRepository.saveAndFlush(any(CarEntity.class))).thenReturn(new CarEntity());
        doNothing()
            .when(carsAggregator)
            .includeNewCar(user, input.getModel(), input.getPlate());

        carApplicationService.includeNewCar(input, "token");

        verify(carRepository).saveAndFlush(any(CarEntity.class));
        verify(securityContextUserService).getUserByContextSecurity("token");
        verify(carsAggregator).includeNewCar(user, input.getModel(), input.getPlate());
    }

    @Test
    @DisplayName("Deve retornar um carro vazio quando usuário não encontrado")
    void shouldReturnCardEmpty() {
        var input = new InputNewCarDto()
            .model("model")
            .plate("plate");

        when(securityContextUserService.getUserByContextSecurity("token"))
            .thenReturn(Optional.empty());

        carApplicationService.includeNewCar(input, "token");

        verify(carRepository, never()).saveAndFlush(any(CarEntity.class));
        verify(securityContextUserService).getUserByContextSecurity("token");
        verify(carsAggregator, never()).includeNewCar(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve remover com sucesso o carro do usuário")
    void shouldRemoveCar() {
        var role = new RoleEntity();
        var accessCard = new AccessCardEntity();
        accessCard.setRoleEntities(List.of(role));
        var user = new UserEntity();
        var car = new CarEntity();
        car.setPlate("plate");

        user.getCar().add(car);
        user.setAccessCard(accessCard);
        user.setTypeUser(TypeUser.STUDENTS.name());

        when(securityContextUserService.getUserByContextSecurity("token"))
            .thenReturn(Optional.of(user));
        doNothing()
            .when(carRepository)
            .delete(any(CarEntity.class));

        var input = new InputRemoveCarDto().plate("plate");
        carApplicationService.removeCar(input, "token");

        verify(securityContextUserService).getUserByContextSecurity("token");
        verify(carRepository).delete(any(CarEntity.class));
    }
}
