package br.edu.utfpr.tsi.xenon.domain.recognize.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.utfpr.tsi.xenon.application.dto.InputRecognizerDto;
import br.edu.utfpr.tsi.xenon.application.dto.PlatesDto;
import br.edu.utfpr.tsi.xenon.domain.notification.service.SendingMessageService;
import br.edu.utfpr.tsi.xenon.domain.workstations.entity.WorkstationEntity;
import br.edu.utfpr.tsi.xenon.domain.workstations.service.WorkstationService;
import br.edu.utfpr.tsi.xenon.structure.exception.ErrorRecognizeConfidenceIsLow;
import br.edu.utfpr.tsi.xenon.structure.exception.ErrorRecognizeOriginIpNotAllowed;
import br.edu.utfpr.tsi.xenon.structure.exception.ErrorRecognizeWorkstationNotFoundException;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - ExecutorRecognizerService")
class ExecutorRecognizerServiceTest {

    @Mock
    private WorkstationRepository workstationRepository;

    @Mock
    private WorkstationService workstationService;

    @Mock
    private CarRepository carRepository;

    @Mock
    private SendingMessageService sendingMessageService;

    @Mock
    private ErrorRecognizeService errorRecognizeService;

    @InjectMocks
    private ExecutorRecognizerService executorRecognizerService;

    @Test
    @DisplayName("Deve persistir o erro quando exception for lançada")
    void shouldHavePersistError() {
        var key = "workstation_key";
        var ip = "ip";
        var input = new InputRecognizerDto();

        when(workstationRepository.findIdByKey(key)).thenThrow(new RuntimeException());

        assertDoesNotThrow(() -> executorRecognizerService.accept(input, key, ip));

        verify(errorRecognizeService).insertError(any(RuntimeException.class), eq(ip), eq(input));
        verify(workstationService, never()).formatterIp(ip);
        verify(sendingMessageService, never()).sendBeforeTransactionCommit(any(), any());
    }

    @Test
    @DisplayName("Deve lançar ErrorRecognizeWorkstationNotFoundException quando estação não encontrada")
    void shouldThrowsErrorRecognizeWorkstationNotFoundException() {
        var key = "workstation_key";
        var ip = "ip";
        var input = new InputRecognizerDto();

        when(workstationRepository.findIdByKey(key)).thenReturn(null);
        assertDoesNotThrow(() -> executorRecognizerService.accept(input, key, ip));

        verify(errorRecognizeService).insertError(
            any(ErrorRecognizeWorkstationNotFoundException.class), eq(ip), eq(input));
        verify(workstationService, never()).formatterIp(ip);
        verify(sendingMessageService, never()).sendBeforeTransactionCommit(any(), any());
    }

    @Test
    @DisplayName("Deve lançar ErrorRecognizeOriginIpNotAllowed quando reconhecimentos não forem maior que 75.0 de confiabilidade")
    void shouldThrowsErrorRecognizeOriginIpNotAllowed() {
        var faker = Faker.instance();
        var key = "workstation_key";
        var ip = faker.internet().ipV6Address();
        var input = new InputRecognizerDto();

        var workstation = new WorkstationEntity();
        workstation.setIp(faker.internet().ipV4Address());

        when(workstationService.formatterIp(ip)).thenReturn(ip);
        when(workstationRepository.findIdByKey(key)).thenReturn(workstation);
        assertDoesNotThrow(() -> executorRecognizerService.accept(input, key, ip));

        verify(errorRecognizeService).insertError(
            any(ErrorRecognizeOriginIpNotAllowed.class), eq(ip), eq(input));
        verify(workstationService).formatterIp(ip);
        verify(workstationRepository).findIdByKey(key);
        verify(sendingMessageService, never()).sendBeforeTransactionCommit(any(), any());
    }

    @Test
    @DisplayName("Deve lançar ErrorRecognizeConfidenceIsLow quando reconhecimentos não forem maior que 75.0 de confiabilidade")
    void shouldThrowsErrorRecognizeConfidenceIsLow() {
        var faker = Faker.instance();
        var key = "workstation_key";
        var ip = faker.internet().ipV6Address();

        var plate01 = new PlatesDto()
            .plate(faker.bothify("???-####"))
            .confidence(69.0F);

        var plate02 = new PlatesDto()
            .plate(faker.bothify("???-####"))
            .confidence(74.99F);

        var input = new InputRecognizerDto()
            .addRecognizersItem(plate01)
            .addRecognizersItem(plate02);

        var workstation = new WorkstationEntity();
        workstation.setIp(ip);

        when(workstationService.formatterIp(ip)).thenReturn(ip);
        when(workstationRepository.findIdByKey(key)).thenReturn(workstation);
        assertDoesNotThrow(() -> executorRecognizerService.accept(input, key, ip));

        verify(errorRecognizeService).insertError(
            any(ErrorRecognizeConfidenceIsLow.class), eq(ip), eq(input));
        verify(workstationService).formatterIp(ip);
        verify(workstationRepository).findIdByKey(key);
        verify(sendingMessageService, never()).sendBeforeTransactionCommit(any(), any());
    }

    @Test
    @DisplayName("Deve enviar messagem recebimento de novo reconhecimento com sucesso")
    void shouldTHaveSendingMessageNewRecognizer() {
        var faker = Faker.instance();
        var key = "workstation_key";
        var ip = faker.internet().ipV6Address();

        var plate01 = new PlatesDto()
            .plate(faker.bothify("???-####"))
            .confidence(80.0F);

        var plate02 = new PlatesDto()
            .plate(faker.bothify("???-####"))
            .confidence(75.99F);

        var input = new InputRecognizerDto()
            .addRecognizersItem(plate01)
            .addRecognizersItem(plate02);

        var workstation = new WorkstationEntity();
        workstation.setIp(ip);

        when(workstationService.formatterIp(ip)).thenReturn(ip);
        when(workstationRepository.findIdByKey(key)).thenReturn(workstation);
        assertDoesNotThrow(() -> executorRecognizerService.accept(input, key, ip));

        verify(errorRecognizeService, never()).insertError(any(Exception.class), eq(ip), eq(input));
        verify(workstationService).formatterIp(ip);
        verify(workstationRepository).findIdByKey(key);
        verify(sendingMessageService).sendBeforeTransactionCommit(any(), any());
        verify(carRepository).findAllByPlateIn(any());
    }
}
