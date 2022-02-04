package br.edu.utfpr.tsi.xenon.domain.recognize.service;

import static java.util.Collections.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.tsi.xenon.application.dto.PlatesDto;
import br.edu.utfpr.tsi.xenon.application.service.WorkstationApplicationService;
import br.edu.utfpr.tsi.xenon.domain.notification.model.ResultProcessRecognizer;
import br.edu.utfpr.tsi.xenon.domain.notification.service.SendingMessageService;
import br.edu.utfpr.tsi.xenon.domain.recognize.entity.RecognizeEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.workstations.entity.WorkstationEntity;
import br.edu.utfpr.tsi.xenon.domain.workstations.service.WorkstationService;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.RecognizerRepository;
import com.github.javafaker.Faker;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade -  ExecutorResult")
class ExecutorResultTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private SendingMessageService sendingMessageService;

    @Mock
    private RecognizerRepository recognizerRepository;

    @Mock
    private WorkstationApplicationService workstationService;

    private ExecutorResult executorResult;

    @BeforeEach
    void setup() {
        executorResult = new ExecutorResult(
            carRepository,
            sendingMessageService,
            recognizerRepository,
            workstationService
        );
    }

    @Test
    @DisplayName("Deve enviar mensagem notificando que não encontrou carro com base nas placas")
    void shouldNofFoundCarsByPlates() {
        var faker = Faker.instance();

        var plate01 = new PlatesDto()
            .plate(faker.bothify("???-####"))
            .confidence(80.0F);

        var plate02 = new PlatesDto()
            .plate(faker.bothify("???-####"))
            .confidence(75.99F);

        var plateString01 = plate01.getPlate();
        var plateString02 = plate02.getPlate();
        var plates = Map.of(
            plateString01, List.of(plate01),
            plateString02, List.of(plate02)
        );

        when(carRepository.findAllByPlateIn(anyList())).thenReturn(emptyList());

        executorResult.processResult(plates, buildWorkstation("AUTOMATIC"));

        verify(sendingMessageService).sendBeforeTransactionCommit(any(), any());
        verify(workstationService, never()).sendRequestOpen(any());
    }

    @Test
    @DisplayName("Deve processar reconhecimento quando encontro mais de uma placa na base de dados")
    void shouldHaveMessageCarsByPlates() {
        var faker = Faker.instance();

        var plate01 = new PlatesDto()
            .plate(faker.bothify("???-####"))
            .confidence(80.0F);

        var plate02 = new PlatesDto()
            .plate(faker.bothify("???-####"))
            .confidence(75.99F);

        var plateString01 = plate01.getPlate();
        var plateString02 = plate02.getPlate();
        var plates = Map.of(
            plateString01, List.of(plate01),
            plateString02, List.of(plate02)
        );

        var user = new UserEntity();
        var car01 = new CarEntity();
        car01.setPlate(plateString01);
        car01.setUser(user);

        var car02 = new CarEntity();
        car02.setPlate(plateString02);
        car02.setUser(user);
        car02.setLastAccess(LocalDateTime.now().minusMinutes(9));

        when(carRepository.findAllByPlateIn(anyList()))
            .thenReturn(List.of(car01, car02));

        var workstation = buildWorkstation("AUTOMATIC");

        when(recognizerRepository.saveAndFlush(any(RecognizeEntity.class))).thenReturn(new RecognizeEntity());
        executorResult.processResult(plates, workstation);

        verify(workstationService).sendRequestOpen(workstation.getId());
        verify(sendingMessageService).sendBeforeTransactionCommit(
            argThat(msg -> {
                ResultProcessRecognizer message = (ResultProcessRecognizer) msg.message();
                var confidence = message.getConfidence().equals(plate01.getConfidence());
                var plate = message.getPlate().equals(plateString01);

                return confidence && plate;
            }),
            any());
    }

    @Test
    @DisplayName("Deve processar reconhecimento quando encontro mais de uma placa na base de dados")
    void shouldHaveMessageCarsByOnePlate() {
        var faker = Faker.instance();

        var plate01 = new PlatesDto()
            .plate(faker.bothify("???-####"))
            .confidence(80.0F);

        var plate02 = new PlatesDto()
            .plate(faker.bothify("???-####"))
            .confidence(75.99F);

        var plateString01 = plate01.getPlate();
        var plateString02 = plate02.getPlate();
        var plates = Map.of(
            plateString01, List.of(plate01),
            plateString02, List.of(plate02)
        );

        var user = new UserEntity();
        var car01 = new CarEntity();
        car01.setPlate(plateString01);
        car01.setUser(user);

        when(carRepository.findAllByPlateIn(anyList()))
            .thenReturn(List.of(car01));

        var workstation = buildWorkstation("AUTOMATIC");

        when(recognizerRepository.saveAndFlush(any(RecognizeEntity.class))).thenReturn(new RecognizeEntity());
        executorResult.processResult(plates, workstation);

        verify(workstationService).sendRequestOpen(workstation.getId());
        verify(sendingMessageService).sendBeforeTransactionCommit(
            argThat(msg -> {
                ResultProcessRecognizer message = (ResultProcessRecognizer) msg.message();
                var confidence = message.getConfidence().equals(plate01.getConfidence());
                var plate = message.getPlate().equals(plateString01);

                return confidence && plate;
            }),
            any());
    }

    @Test
    @DisplayName("Deve enviar mensagem notificando o reconhecimento com maior confiabilidade")
    void shouldHaveMessageCarsMajorConfidence() {
        var faker = Faker.instance();

        var plate01 = new PlatesDto()
            .plate(faker.bothify("???-####"))
            .confidence(80.0F);

        var plate02 = new PlatesDto()
            .plate(faker.bothify("???-####"))
            .confidence(75.99F);

        var plateString01 = plate01.getPlate();
        var plateString02 = plate02.getPlate();
        var plates = Map.of(
            plateString01, List.of(plate01),
            plateString02, List.of(plate02)
        );

        var user = new UserEntity();
        var car01 = new CarEntity();
        car01.setPlate(plateString01);
        car01.setUser(user);
        car01.setLastAccess(LocalDateTime.now().minusMinutes(5));

        var car02 = new CarEntity();
        car02.setPlate(plateString02);
        car02.setUser(user);
        car02.setLastAccess(LocalDateTime.now().minusMinutes(3));

        when(carRepository.findAllByPlateIn(anyList()))
            .thenReturn(List.of(car01, car02));

        var workstation = buildWorkstation("AUTOMATIC");

        when(recognizerRepository.saveAndFlush(any(RecognizeEntity.class))).thenReturn(new RecognizeEntity());
        executorResult.processResult(plates, workstation);

        verify(sendingMessageService).sendBeforeTransactionCommit(
            argThat(msg -> {
                ResultProcessRecognizer message = (ResultProcessRecognizer) msg.message();
                var confidence = message.getConfidence().equals(plate01.getConfidence());
                var plate = message.getPlate().equals(plateString01);

                return confidence && plate;
            })
            , any());
        verify(workstationService).sendRequestOpen(workstation.getId());
    }

    @Test
    @DisplayName("Deve enviar mensagem notificando o reconhecimento com maior confiabilidade")
    void shouldNotHaveSendRequestWhenNotAutomatic() {
        var faker = Faker.instance();

        var plate01 = new PlatesDto()
            .plate(faker.bothify("???-####"))
            .confidence(80.0F);

        var plate02 = new PlatesDto()
            .plate(faker.bothify("???-####"))
            .confidence(75.99F);

        var plateString01 = plate01.getPlate();
        var plateString02 = plate02.getPlate();
        var plates = Map.of(
            plateString01, List.of(plate01),
            plateString02, List.of(plate02)
        );

        var user = new UserEntity();
        var car01 = new CarEntity();
        car01.setPlate(plateString01);
        car01.setUser(user);
        car01.setLastAccess(LocalDateTime.now().minusMinutes(5));

        var car02 = new CarEntity();
        car02.setPlate(plateString02);
        car02.setUser(user);
        car02.setLastAccess(LocalDateTime.now().minusMinutes(3));

        when(carRepository.findAllByPlateIn(anyList()))
            .thenReturn(List.of(car01, car02));

        var workstation = buildWorkstation("MANUAL");

        when(recognizerRepository.saveAndFlush(any(RecognizeEntity.class))).thenReturn(new RecognizeEntity());
        executorResult.processResult(plates, workstation);

        verify(workstationService, never()).sendRequestOpen(workstation.getId());

        verify(sendingMessageService).sendBeforeTransactionCommit(
            argThat(msg -> {
                ResultProcessRecognizer message = (ResultProcessRecognizer) msg.message();
                var confidence = message.getConfidence().equals(plate01.getConfidence());
                var plate = message.getPlate().equals(plateString01);

                return confidence && plate;
            })
            , any());
    }

    private WorkstationEntity buildWorkstation(String mode) {
        var workstation = new WorkstationEntity();
        workstation.setMode(mode);

        return workstation;
    }

}
