package br.edu.utfpr.tsi.xenon.domain.recognize.service;

import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;

import br.edu.utfpr.tsi.xenon.application.dto.InputRecognizerDto;
import br.edu.utfpr.tsi.xenon.application.dto.PlatesDto;
import br.edu.utfpr.tsi.xenon.structure.exception.ErrorRecognizeWorkstationNotFoundException;
import br.edu.utfpr.tsi.xenon.structure.repository.ErrorRecognizerRepository;
import com.github.javafaker.Faker;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - ErrorRecognizeService")
class ErrorRecognizeServiceTest {

    @Mock
    private ErrorRecognizerRepository errorRecognizerRepository;

    @InjectMocks
    private ErrorRecognizeService errorRecognizeService;

    @Test
    @DisplayName("Deve salvar Exception Generica")
    void shouldHaveSaveGenericException() {
        var faker = Faker.instance();
        var ip = Faker.instance().internet().ipV4Address();
        var cause = new NullPointerException();
        var exception = new RuntimeException("message", cause);
        var plate01 = new PlatesDto()
            .plate(faker.bothify("???-####"))
            .confidence(69.0F);

        var plate02 = new PlatesDto()
            .plate(faker.bothify("???-####"))
            .confidence(74.99F);

        var input = new InputRecognizerDto()
            .addRecognizersItem(plate01)
            .addRecognizersItem(plate02);

        errorRecognizeService.insertError(exception, ip, input);

        verify(errorRecognizerRepository).save(argThat(error -> {
            var msg = error.getErrorMessage().equals("message");
            var trace = ExceptionUtils.getStackTrace(exception).equals(error.getTrace());
            var workstation = "Não informado".equals(error.getWorkstationName());
            var inputError = error.getInput().equals(input.toString());
            var ipError = ip.equals(error.getOriginIp());

            return msg && trace && workstation && inputError && ipError;
        }));
    }

    @Test
    @DisplayName("Deve salvar Exception do tipo RecognizerError")
    void shouldHaveSaveRecognizerErrorException() {
        var faker = Faker.instance();
        var ip = Faker.instance().internet().ipV4Address();
        var plate01 = new PlatesDto()
            .plate(faker.bothify("???-####"))
            .confidence(69.0F);

        var plate02 = new PlatesDto()
            .plate(faker.bothify("???-####"))
            .confidence(74.99F);

        var input = new InputRecognizerDto()
            .addRecognizersItem(plate01)
            .addRecognizersItem(plate02);
        var exception = new ErrorRecognizeWorkstationNotFoundException("key-search", input);

        errorRecognizeService.insertError(exception, ip, input);

        verify(errorRecognizerRepository).save(argThat(error -> {
            var msg = error.getErrorMessage()
                .equals("Estação de trabalho para key key-search não encontrada");
            var trace = ExceptionUtils.getStackTrace(exception).equals(error.getTrace());
            var workstation = "Não encontrada".equals(error.getWorkstationName());
            var inputError = error.getInput().equals(input.toString());
            var ipError = ip.equals(error.getOriginIp());

            return msg && trace && workstation && inputError && ipError;
        }));
    }
}
