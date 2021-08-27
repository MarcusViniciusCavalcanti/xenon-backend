package br.edu.utfpr.tsi.xenon.application.handler;

import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.ARGUMENT_INVALID;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.RESOURCE_NOT_FOUND;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import br.edu.utfpr.tsi.xenon.application.controller.EndpointsTranslator;
import br.edu.utfpr.tsi.xenon.application.dto.ErrorBaseDto;
import br.edu.utfpr.tsi.xenon.application.dto.ErrorDetailsDto;
import br.edu.utfpr.tsi.xenon.application.dto.ErrorDto;
import br.edu.utfpr.tsi.xenon.structure.exception.BusinessException;
import br.edu.utfpr.tsi.xenon.structure.exception.ResourceNotFoundException;
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
        var message = getMessage(ARGUMENT_INVALID.getCode(), locale);
        var error = new ErrorDto()
            .details(errorsDetails)
            .message(message)
            .path(path)
            .statusCode(HttpStatus.BAD_REQUEST.value());

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorBaseDto> entityNotFound(
        ResourceNotFoundException exception,
        HttpServletRequest request) {
        log.error("e-mail já existe: {}", exception.getMessage());
        var path = request.getServletPath();
        var locale = getLocale(request.getHeader("accept-language"));
        var message = getMessage(
            RESOURCE_NOT_FOUND.getCode(),
            locale, exception.getResourceName(),
            exception.getArgumentSearch()
        );
        var error = new ErrorDto()
            .message(message)
            .path(path)
            .statusCode(NOT_FOUND.value());

        return ResponseEntity.status(NOT_FOUND).body(error);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorBaseDto> businessException(
        BusinessException exception,
        HttpServletRequest request) {

        log.error("erro de negócio: {}", exception.getMessage());
        var path = request.getServletPath();
        var locale = getLocale(request.getHeader("accept-language"));
        var message = getMessage(
            exception.getCode(),
            locale,
            exception.getArgs()
        );
        var error = new ErrorDto()
            .message(message)
            .path(path)
            .statusCode(exception.getStatus());

        return ResponseEntity.status(HttpStatus.valueOf(exception.getStatus())).body(error);
    }

    @Override
    public MessageSource getMessageSource() {
        return messageSource;
    }
}
