package br.edu.utfpr.tsi.xenon.domain.recognize.service;

import br.edu.utfpr.tsi.xenon.application.dto.InputRecognizerDto;
import br.edu.utfpr.tsi.xenon.domain.recognize.entity.ErrorRecognizerEntity;
import br.edu.utfpr.tsi.xenon.structure.exception.RecognizerError;
import br.edu.utfpr.tsi.xenon.structure.repository.ErrorRecognizerRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
            var trace = Arrays.stream(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.joining());

            var error = createError(
                trace,
                exception.getMessage(),
                ex.getWorkstationName(),
                ((RecognizerError) exception).getInput().toString(),
                ip);

            errorRecognizerRepository.save(error);

        } else {
            var trace = Arrays.stream(exception.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.joining());

            var error = createError(
                trace,
                exception.getMessage(),
                "Não informado",
                input.toString(),
                ip);

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
}
