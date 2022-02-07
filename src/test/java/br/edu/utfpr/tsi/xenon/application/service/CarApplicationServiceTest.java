package br.edu.utfpr.tsi.xenon.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarStateSummary;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarStatus;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.factory.TypeUser;
import br.edu.utfpr.tsi.xenon.structure.exception.BusinessException;
import br.edu.utfpr.tsi.xenon.structure.exception.ResourceNotFoundException;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import com.cloudinary.Cloudinary;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Mock
    private Cloudinary cloudinary;

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

        var carEntity = new CarEntity();
        carEntity.setCarStatus(CarStatus.WAITING);
        when(user.lastCar()).thenReturn(carEntity);
        when(securityContextUserService.getUserByContextSecurity("token"))
            .thenReturn(Optional.of(user));
        when(carRepository.saveAndFlush(any(CarEntity.class))).thenReturn(carEntity);
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

        assertThrows(BusinessException.class,
            () -> carApplicationService.includeDocument(car.getId(), multipartFile, token));

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

        assertThrows(BusinessException.class,
            () -> carApplicationService.authorisedCar(car.getId()));

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

        carApplicationService.unauthorisedCar(car.getId(), "");

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

        assertThrows(BusinessException.class,
            () -> carApplicationService.unauthorisedCar(car.getId(), ""));

        verify(carRepository).findById(car.getId());
        verify(changeStateCar).executeProcess(car);
        verify(carRepository, never()).saveAndFlush(car);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando carro não foi encontrado")
    void shouldThrowsResourceNotFoundExceptionWhenFindById() {
        when(carRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> carApplicationService.unauthorisedCar(1L, ""));
        assertThrows(ResourceNotFoundException.class,
            () -> carApplicationService.authorisedCar(1L));
    }

    @Test
    @DisplayName("Deve retornar uma lista de carros cadastrados")
    void shouldReturnListCar() {
        var carOne = new CarEntity();
        carOne.setId(1L);
        carOne.setCarStatus(CarStatus.APPROVED);
        carOne.setPlate("plate");
        carOne.setAuthorisedAccess(Boolean.TRUE);
        carOne.setState("state");
        carOne.setDocument("document");
        carOne.setReasonBlock("reason block");
        carOne.setModel("model car");
        carOne.setLastAccess(LocalDateTime.now());
        carOne.setNumberAccess(10);

        when(carRepository.findByUserId(1L)).thenReturn(List.of(carOne));

        var carsList = carApplicationService.getAllCarsByUser(1L);
        var result = carsList.get(0);

        assertEquals(1, carsList.size());

        assertEquals(result.getId(), carOne.getId());
        assertEquals(result.getStatus(), carOne.getCarStatus().name());
        assertEquals(result.getAuthorisedAccess(), carOne.getAuthorisedAccess());
        assertEquals(result.getDocument(), carOne.getDocument());
        assertEquals(result.getModelCar(), carOne.getModel());
        assertEquals(result.getLastAcess(), carOne.getLastAccess());
        assertEquals(result.getPlateCar(), carOne.getPlate());
        assertEquals(result.getNumberAccess(), carOne.getNumberAccess());
        assertEquals(result.getReasonLock(), carOne.getReasonBlock());

    }

    @Test
    @DisplayName("Deve retonar com sucesso a uri para fazer o download do documento do carro")
    void shouldReturnDocumentUri() throws Exception {
        var uri = "uri";
        var car = new CarEntity();
        car.setDocument("document");

        when(cloudinary.privateDownload(
            eq(car.getDocument()),
            eq("pdf"),
            any()))
            .thenReturn(uri);
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        var documentUriDto = carApplicationService.downloadDocument(1L);

        assertEquals(uri, documentUriDto.getUri());
        assertEquals(5, documentUriDto.getTtl());

        verify(cloudinary).privateDownload(
            eq(car.getDocument()),
            eq("pdf"),
            any());
    }

    @Test
    @DisplayName("Deve Lançar ResourceNotFoundException quando ocorrer um erro no cloadnay")
    void shouldThrowsResourceNotFoundExceptionDocumentUri() throws Exception {
        var car = new CarEntity();
        car.setDocument("document");

        doThrow(Exception.class)
            .when(cloudinary)
            .privateDownload(eq(car.getDocument()), eq("pdf"), any());
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        var exception = assertThrows(ResourceNotFoundException.class,
            () -> carApplicationService.downloadDocument(1L));

        assertEquals("documento", exception.getResourceName());
        assertEquals("id do carro", exception.getArgumentSearch());
        verify(cloudinary).privateDownload(
            eq(car.getDocument()),
            eq("pdf"),
            any());
    }

    @Test
    @DisplayName("Deve Lançar ResourceNotFoundException quando carro não foi encontrado na base")
    void shouldThrowsResourceNotFoundExceptionDocumentUriWhenCarNotFound() throws Exception {
        when(carRepository.findById(1L)).thenReturn(Optional.empty());

        var exception = assertThrows(ResourceNotFoundException.class,
            () -> carApplicationService.downloadDocument(1L));

        assertEquals("carro", exception.getResourceName());
        assertEquals("id", exception.getArgumentSearch());
        verify(cloudinary, never()).privateDownload(
            anyString(),
            anyString(),
            any());
    }

    @Test
    @DisplayName("Deve retonar uma sumário de carros cadastrado no sistema")
    void shouldReturnSummaryCars() {
        when(carRepository.getCarsSummary()).thenReturn(new CarStateSummary() {
            @Override
            public Long getWaiting() {
                return 10L;
            }

            @Override
            public Long getApproved() {
                return 1L;
            }

            @Override
            public Long getReproved() {
                return 8L;
            }

            @Override
            public Long getBlock() {
                return 2L;
            }
        });

        var result = carApplicationService.getUserCarsSummary();

        assertEquals(10L, result.getWaiting());
        assertEquals(1L, result.getApproved());
        assertEquals(8L, result.getReproved());
        assertEquals(2L, result.getBlock());
    }

    @Test
    @DisplayName("Deve retonar uma página de carros aguardando decisão de aprovação/reprovação")
    void shouldReturnPageCarDecision(@Mock Page<CarEntity> carEntityPage) {
        var car = new CarEntity();
        car.setId(1L);
        car.setPlate("plate");
        car.setCarStatus(CarStatus.WAITING);

        var listCar = List.of(car);

        when(carRepository.findAllByState(eq("WAITING_DECISION"), any(Pageable.class)))
            .thenReturn(carEntityPage);
        when(carEntityPage.getTotalPages()).thenReturn(1);
        when(carEntityPage.getNumber()).thenReturn(1);
        when(carEntityPage.getSize()).thenReturn(1);
        when(carEntityPage.getTotalElements()).thenReturn(1L);
        when(carEntityPage.getContent()).thenReturn(listCar);

        var pageResult = carApplicationService.pageCarWaitingDecision(1, 0);

        verify(carRepository).findAllByState(eq("WAITING_DECISION"), any(Pageable.class));
        verify(carEntityPage).getTotalPages();
        verify(carEntityPage).getNumber();
        verify(carEntityPage).getSize();
        verify(carEntityPage).getTotalElements();

        assertEquals(1, pageResult.getPage());
        assertEquals(1, pageResult.getSize());
        assertEquals(1, pageResult.getTotalPage());
        assertEquals(1, pageResult.getTotalElements());
        assertEquals("DESC", pageResult.getDirection());
    }
}
