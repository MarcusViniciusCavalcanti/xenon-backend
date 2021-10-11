package br.edu.utfpr.tsi.xenon.domain.recognize.service;

import static java.util.Collections.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.tsi.xenon.application.dto.CarDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputRecognizerDto;
import br.edu.utfpr.tsi.xenon.application.dto.PlatesDto;
import br.edu.utfpr.tsi.xenon.domain.notification.model.ResultProcessRecognizer;
import br.edu.utfpr.tsi.xenon.domain.notification.service.SendingMessageService;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import com.github.javafaker.Faker;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade -  ExecutorResult")
class ExecutorResultTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private SendingMessageService sendingMessageService;

    private ExecutorResult executorResult;

    @BeforeEach
    void setup() {
        executorResult = new ExecutorResult(carRepository, sendingMessageService);
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

        executorResult.processResult(plates, 1L);

        verify(sendingMessageService).sendBeforeTransactionCommit(any(), any());
    }

    @Test
    @DisplayName("Deve enviar mensagem notificando que encontrou carro")
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

        executorResult.processResult(plates, 1L);

        verify(sendingMessageService).sendBeforeTransactionCommit(any(), any());
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

        executorResult.processResult(plates, 1L);

        verify(sendingMessageService).sendBeforeTransactionCommit(
            argThat(msg -> {
                ResultProcessRecognizer message = (ResultProcessRecognizer) msg.message();
                var confidence = message.getConfidence().equals(plate01.getConfidence());
                var plate = message.getPlate().equals(plateString01);

                return confidence && plate;
            })
            , any());
    }

}
