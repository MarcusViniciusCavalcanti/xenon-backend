package br.edu.utfpr.tsi.xenon.application.handler;

import br.edu.utfpr.tsi.xenon.application.controller.EndpointsTranslator;
import br.edu.utfpr.tsi.xenon.application.dto.ErrorBaseDto;
import br.edu.utfpr.tsi.xenon.application.dto.ErrorDetailsDto;
import br.edu.utfpr.tsi.xenon.application.dto.ErrorDto;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class DefaultHandlerError implements EndpointsTranslator {

    private final MessageSource messageSource;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorBaseDto> methodArgumentNotValidException(
        MethodArgumentNotValidException exception,
        HttpServletRequest request) {
        log.error("campos inválidos {}", exception.getMessage());
        var errorsDetails = exception.getFieldErrors().stream()
            .map(error -> {
                var field = error.getField();
                var msg = error.getDefaultMessage();

                return new ErrorDetailsDto()
                    .descriptionError(msg)
                    .field(field);
            }).collect(Collectors.toList());

        var path = request.getServletPath();
        var locale = getLocale(request.getHeader("accept-language"));
        var message = getMessage("ERROR-001", locale);
        var error = new ErrorDto()
            .details(errorsDetails)
            .message(message)
            .path(path)
            .statusCode(HttpStatus.BAD_REQUEST.value());

        return ResponseEntity.badRequest().body(error);
    }

    @Override
    public MessageSource getMessage() {
        return messageSource;
    }
}
