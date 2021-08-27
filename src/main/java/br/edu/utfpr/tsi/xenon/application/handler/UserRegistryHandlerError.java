package br.edu.utfpr.tsi.xenon.application.handler;

import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.EMAIL_EXIST;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.PLATE_ALREADY;

import br.edu.utfpr.tsi.xenon.application.controller.EndpointsTranslator;
import br.edu.utfpr.tsi.xenon.application.dto.ErrorBaseDto;
import br.edu.utfpr.tsi.xenon.application.dto.ErrorDto;
import br.edu.utfpr.tsi.xenon.structure.exception.EmailErrorException;
import br.edu.utfpr.tsi.xenon.structure.exception.PlateException;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class UserRegistryHandlerError implements EndpointsTranslator {

    private final MessageSource messageSource;

    @ExceptionHandler(PlateException.class)
    public ResponseEntity<ErrorBaseDto> plateException(
        PlateException exception,
        HttpServletRequest request) {
        log.error("placa já existe: {}", exception.getMessage());
        return buildResponse(request, exception.getCode(), exception.getPlate());
    }

    @ExceptionHandler(EmailErrorException.class)
    public ResponseEntity<ErrorBaseDto> emailExistException(
        EmailErrorException exception,
        HttpServletRequest request) {
        log.error("e-mail já existe: {}", exception.getMessage());
        return buildResponse(request, exception.getCode(), exception.getEmail());
    }

    private ResponseEntity<ErrorBaseDto> buildResponse(
        HttpServletRequest request,
        String code,
        String email) {
        var path = request.getServletPath();
        var locale = getLocale(request.getHeader("accept-language"));
        var message = getMessage(code, locale, email);

        if (code.equals(PLATE_ALREADY.getCode()) || code.equals(EMAIL_EXIST.getCode())) {
            var error = new ErrorDto()
                .message(message)
                .path(path)
                .statusCode(HttpStatus.CONFLICT.value());

            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        var error = new ErrorDto()
            .message(message)
            .path(path)
            .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());

        return ResponseEntity.unprocessableEntity().body(error);
    }

    @Override
    public MessageSource getMessageSource() {
        return messageSource;
    }
}
