package br.edu.utfpr.tsi.xenon.domain.recognize.service;

import br.edu.utfpr.tsi.xenon.application.dto.InputRecognizerDto;
import br.edu.utfpr.tsi.xenon.domain.recognize.entity.ErrorRecognizerEntity;
import br.edu.utfpr.tsi.xenon.domain.recognize.entity.RecognizeEntity;
import br.edu.utfpr.tsi.xenon.structure.exception.RecognizerError;
import br.edu.utfpr.tsi.xenon.structure.repository.ErrorRecognizerRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ErrorRecognizeService {

    private final ErrorRecognizerRepository errorRecognizerRepository;

    public void insertError(
        Exception exception,
        String ip,
        InputRecognizerDto input) {
        if (exception instanceof RecognizerError ex) {
            var stackTrace = ExceptionUtils.getStackTrace(ex);

            var error = createError(
                stackTrace,
                exception.getMessage(),
                ex.getWorkstationName(),
                ((RecognizerError) exception).getInput().toString(),
                ip);

            createRecognize(ip, input, error);
            errorRecognizerRepository.save(error);

        } else {
            var stackTrace = ExceptionUtils.getStackTrace(exception);

            var error = createError(
                stackTrace,
                exception.getMessage(),
                "Não informado",
                input.toString(),
                ip);

            createRecognize(ip, input, error);
            errorRecognizerRepository.save(error);
        }
    }

    private ErrorRecognizerEntity createError(
        String trace,
        String msg,
        String workstationName,
        String input,
        String ip) {
        var error = new ErrorRecognizerEntity();
        error.setErrorMessage(msg);
        error.setDate(LocalDateTime.now());
        error.setTrace(trace);
        error.setOriginIp(ip);
        error.setWorkstationName(workstationName);
        error.setInput(input);

        return error;
    }

    private void createRecognize(
        String ip,
        InputRecognizerDto input,
        ErrorRecognizerEntity errorRecognizer) {
        var firstDto = input.getRecognizers().get(0);
        var recognize = new RecognizeEntity();

        errorRecognizer.setRecognize(recognize);
        recognize.setErrorRecognizer(errorRecognizer);
        recognize.setConfidence(firstDto.getConfidence());
        recognize.setHasError(Boolean.TRUE);
        recognize.setOriginIp(ip);
        recognize.setAccessGranted(Boolean.FALSE);
        recognize.setEpochTime(LocalDateTime.now());
        recognize.setPlate(firstDto.getPlate());
        recognize.setDriverName("Desconhecido");
    }
}
