package br.edu.utfpr.tsi.xenon.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.utfpr.tsi.xenon.application.dto.InputNewCarDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputRemoveCarDto;
import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.domain.security.entity.RoleEntity;
import br.edu.utfpr.tsi.xenon.domain.security.service.SecurityContextUserService;
import br.edu.utfpr.tsi.xenon.domain.user.aggregator.CarsAggregator;
import br.edu.utfpr.tsi.xenon.domain.user.aggregator.ChangeStateCar;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.factory.TypeUser;
import br.edu.utfpr.tsi.xenon.structure.exception.BusinessException;
import br.edu.utfpr.tsi.xenon.structure.exception.ResourceNotFoundException;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import java.io.File;
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
@DisplayName("Teste - Unidade - CarApplicationService")
class CarApplicationServiceTest {

    @Mock
    private SecurityContextUserService securityContextUserService;

    @Mock
    private CarsAggregator carsAggregator;

    @Mock
    private CarRepository carRepository;

    @Mock
    private ChangeStateCar changeStateCar;

    @InjectMocks
    private CarApplicationService carApplicationService;

    @Test
    @DisplayName("Deve incluir novo carro com sucesso")
    void shouldHaveIncludeNewCarSuccessfully() {
        var role = new RoleEntity();
        var accessCard = new AccessCardEntity();
        accessCard.setRoleEntities(List.of(role));
        var user = mock(UserEntity.class);

        var input = new InputNewCarDto()
            .model("model")
            .plate("plate");

        when(user.lastCar()).thenReturn(new CarEntity());
        when(securityContextUserService.getUserByContextSecurity("token"))
            .thenReturn(Optional.of(user));
        when(carRepository.saveAndFlush(any(CarEntity.class))).thenReturn(new CarEntity());
        doNothing()
            .when(carsAggregator)
            .includeNewCar(user, input.getModel(), input.getPlate());

        carApplicationService.includeNewCar(input, "token");

        verify(user).lastCar();
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
            .deleteByUserAndPlate(eq(user), any());

        var input = new InputRemoveCarDto().plate("plate");
        carApplicationService.removeCar(input, "token");

        verify(securityContextUserService).getUserByContextSecurity("token");
        verify(carRepository).deleteByUserAndPlate(eq(user), any());
    }

    @Test
    @DisplayName("Deve incluir com sucesso o documento do carro")
    void shouldHaveIncludeDocument() throws IOException {
        var role = new RoleEntity();

        var accessCard = new AccessCardEntity();
        accessCard.setRoleEntities(List.of(role));

        var car = new CarEntity();
        car.setId(1L);
        car.setPlate("plate");

        var user = new UserEntity();
        user.getCar().add(car);
        user.setAccessCard(accessCard);
        user.setTypeUser(TypeUser.STUDENTS.name());

        var multipartFile = mock(MultipartFile.class);

        doNothing()
            .when(multipartFile)
            .transferTo(any(Path.class));

        var token = "token";
        when(securityContextUserService.getUserByContextSecurity(token))
            .thenReturn(Optional.of(user));

        carApplicationService.includeDocument(car.getId(), multipartFile, token);

        verify(securityContextUserService).getUserByContextSecurity(token);
        verify(carsAggregator).includeDocumentToCar(eq(car), any(File.class));
        verify(carRepository).saveAndFlush(car);
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando erro ao mapear o arquivo da requisição")
    void shouldThrowsBusinessException() throws IOException {
        var role = new RoleEntity();

        var accessCard = new AccessCardEntity();
        accessCard.setRoleEntities(List.of(role));

        var car = new CarEntity();
        car.setId(1L);
        car.setPlate("plate");

        var user = new UserEntity();
        user.getCar().add(car);
        user.setAccessCard(accessCard);
        user.setTypeUser(TypeUser.STUDENTS.name());

        var multipartFile = mock(MultipartFile.class);

        doThrow(IOException.class)
            .when(multipartFile)
            .transferTo(any(Path.class));

        var token = "token";
        when(securityContextUserService.getUserByContextSecurity(token))
            .thenReturn(Optional.of(user));

        assertThrows(BusinessException.class, () -> carApplicationService.includeDocument(car.getId(), multipartFile, token));

        verify(securityContextUserService).getUserByContextSecurity(token);
        verify(carsAggregator, never()).includeDocumentToCar(eq(car), any(File.class));
        verify(changeStateCar, never()).executeProcess(car);
        verify(carRepository, never()).saveAndFlush(car);
    }

    @Test
    @DisplayName("Deve autorizar o carro com sucesso")
    void shouldHaveAuthorisedCar() {
        var car = new CarEntity();
        car.setId(1L);
        car.setPlate("plate");

        when(carRepository.findById(car.getId())).thenReturn(Optional.of(car));
        when(changeStateCar.executeProcess(car)).thenReturn(car);
        when(carRepository.saveAndFlush(car)).thenReturn(car);

        carApplicationService.authorisedCar(car.getId());

        verify(carRepository).findById(car.getId());
        verify(changeStateCar).executeProcess(car);
        verify(carRepository).saveAndFlush(car);
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando autorizar carro")
    void shouldThrowsBusinessExceptionWhenAuthorisedCar() {
        var car = new CarEntity();
        car.setId(1L);
        car.setPlate("plate");

        when(carRepository.findById(car.getId())).thenReturn(Optional.of(car));
        when(changeStateCar.executeProcess(car)).thenThrow(new IllegalStateException());

        assertThrows(BusinessException.class, () -> carApplicationService.authorisedCar(car.getId()));

        verify(carRepository).findById(car.getId());
        verify(changeStateCar).executeProcess(car);
        verify(carRepository, never()).saveAndFlush(car);
    }

    @Test
    @DisplayName("Deve autorizar o carro com sucesso")
    void shouldHaveUnauthorisedCar() {
        var car = new CarEntity();
        car.setId(1L);
        car.setPlate("plate");

        when(carRepository.findById(car.getId())).thenReturn(Optional.of(car));
        when(changeStateCar.executeProcess(car)).thenReturn(car);
        when(carRepository.saveAndFlush(car)).thenReturn(car);

        carApplicationService.unauthorisedCar(car.getId());

        verify(carRepository).findById(car.getId());
        verify(changeStateCar).executeProcess(car);
        verify(carRepository).saveAndFlush(car);
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando remover a autorização do carro")
    void shouldThrowsBusinessExceptionWhenUnauthorisedCar() {
        var car = new CarEntity();
        car.setId(1L);
        car.setPlate("plate");

        when(carRepository.findById(car.getId())).thenReturn(Optional.of(car));
        when(changeStateCar.executeProcess(car)).thenThrow(new IllegalStateException());

        assertThrows(BusinessException.class, () -> carApplicationService.unauthorisedCar(car.getId()));

        verify(carRepository).findById(car.getId());
        verify(changeStateCar).executeProcess(car);
        verify(carRepository, never()).saveAndFlush(car);
    }

    @Test
    void shouldThrowsResourceNotFoundExceptionWhenFindById() {
        when(carRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> carApplicationService.unauthorisedCar(1L));
        assertThrows(ResourceNotFoundException.class, () -> carApplicationService.authorisedCar(1L));
    }
}
