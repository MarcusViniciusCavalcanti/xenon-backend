package br.edu.utfpr.tsi.xenon.application.handler;

import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.ACCESS_DENIED;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.ARGUMENT_INVALID;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.EMAIL_INVALID;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.EMAIL_NOT_INSTITUTIONAL;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.KNOWN;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.MEDIA_TYPE_NOT_SUPPORTED;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.PLATE_INVALID;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.REQUEST_INVALID;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.REQUEST_METHOD_INVALID;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.RESOURCE_NOT_FOUND;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.URL_NOT_FOUND;
import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

import br.edu.utfpr.tsi.xenon.application.controller.EndpointsTranslator;
import br.edu.utfpr.tsi.xenon.application.dto.ErrorBaseDto;
import br.edu.utfpr.tsi.xenon.application.dto.ErrorDetailsDto;
import br.edu.utfpr.tsi.xenon.application.dto.ErrorDto;
import br.edu.utfpr.tsi.xenon.structure.MessagesMapper;
import br.edu.utfpr.tsi.xenon.structure.exception.BusinessException;
import br.edu.utfpr.tsi.xenon.structure.exception.EmailErrorException;
import br.edu.utfpr.tsi.xenon.structure.exception.PlateException;
import br.edu.utfpr.tsi.xenon.structure.exception.RegistryUserException;
import br.edu.utfpr.tsi.xenon.structure.exception.ResourceNotFoundException;
import br.edu.utfpr.tsi.xenon.structure.exception.WorkStationException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.data.util.StreamUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class HandlerError implements EndpointsTranslator {

    private final MessageSource messageSource;

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorBaseDto> httpMessageNotReadableException(
        HttpMessageNotReadableException exception,
        HttpServletRequest request) {
        log.error("Erro ao ler requisi????o {}", exception.getMessage());
        return buildResponseError(request, REQUEST_INVALID.getCode(), BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorBaseDto> httpRequestMethodNotSupportedException(
        HttpRequestMethodNotSupportedException exception,
        HttpServletRequest request) {
        var path = request.getServletPath();
        var method = exception.getMethod();
        log.error("Erro ao tentar executar uma a????o na url: {}, como m??todo: {}",
            path,
            exception.getMessage());
        return buildResponseError(request, REQUEST_METHOD_INVALID.getCode(), METHOD_NOT_ALLOWED,
            path, method);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorBaseDto> noHandlerFoundException(
        NoHandlerFoundException exception,
        HttpServletRequest request) {
        var path = request.getServletPath();
        var method = exception.getHttpMethod();
        log.error("Rota n??o encontrada para {}", path);
        return buildResponseError(request, URL_NOT_FOUND.getCode(), NOT_FOUND, path, method);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorBaseDto> httpMediaTypeNotSupportedException(
        HttpMediaTypeNotSupportedException exception,
        HttpServletRequest request) {
        var type = exception.getContentType();
        log.error("Erro na requisi????o media type n??o suportada {}", type);

        var locale = getLocale(request.getHeader(ACCEPT_LANGUAGE));
        var message = getMessage(MEDIA_TYPE_NOT_SUPPORTED.getCode(), locale);
        var error = new ErrorDto()
            .message(message)
            .path(request.getServletPath())
            .statusCode(UNSUPPORTED_MEDIA_TYPE.value());

        return ResponseEntity.status(UNSUPPORTED_MEDIA_TYPE).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorBaseDto> methodArgumentNotValidException(
        MethodArgumentNotValidException exception,
        HttpServletRequest request) {
        log.error("campos inv??lidos {}", exception.getMessage());
        var errorsDetails = exception.getFieldErrors().stream()
            .map(error -> {
                var field = error.getField();
                var msg = StringUtils.EMPTY;

                if (StringUtils.equalsIgnoreCase(error.getCode(), "pattern")) {
                    var locale = getLocale(request.getHeader(ACCEPT_LANGUAGE));
                    msg = checkCase(error, locale);
                } else {
                    msg = StringUtils.defaultString(error.getDefaultMessage(), StringUtils.EMPTY);
                }

                return new ErrorDetailsDto()
                    .descriptionError(msg)
                    .field(field);
            }).toList();

        return getErrorBaseDtoResponseEntity(request, errorsDetails);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorBaseDto> entityNotFound(
        ResourceNotFoundException exception,
        HttpServletRequest request) {
        log.error("Falha na busca: {}", exception.getMessage());
        var path = request.getServletPath();
        var locale = getLocale(request.getHeader(ACCEPT_LANGUAGE));
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

        log.error("erro de neg??cio: {}", exception.getMessage());
        var path = request.getServletPath();
        var locale = getLocale(request.getHeader(ACCEPT_LANGUAGE));
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

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorBaseDto> constraintViolationException(
        ConstraintViolationException exception,
        HttpServletRequest request) {

        var errors = exception.getConstraintViolations().stream()
            .map(constraintViolation -> {
                var field = fieldNameFromPropertyPath(constraintViolation.getPropertyPath());
                var msg = constraintViolation.getMessage();
                return new ErrorDetailsDto()
                    .descriptionError(msg)
                    .field(field);
            }).toList();

        return getErrorBaseDtoResponseEntity(request, errors);
    }

    @ExceptionHandler(PlateException.class)
    public ResponseEntity<ErrorBaseDto> plateException(
        PlateException exception,
        HttpServletRequest request) {
        log.error("placa j?? existe: {}", exception.getMessage());

        var path = request.getServletPath();
        var locale = getLocale(request.getHeader(ACCEPT_LANGUAGE));
        var message = getMessage(exception.getCode(), locale, exception.getPlate());

        var error = new ErrorDto()
            .message(message)
            .path(path)
            .statusCode(HttpStatus.CONFLICT.value());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(EmailErrorException.class)
    public ResponseEntity<ErrorBaseDto> emailExistException(
        EmailErrorException exception,
        HttpServletRequest request) {
        log.error("Falha no e-mail: {}", exception.getMessage());
        var email = exception.getEmail();
        var path = request.getServletPath();
        var locale = getLocale(request.getHeader(ACCEPT_LANGUAGE));
        var message = getMessage(exception.getCode(), locale, email);
        var error = new ErrorDto()
            .message(message)
            .path(path)
            .statusCode(exception.getHttpStatusCode());

        return ResponseEntity.status(HttpStatus.valueOf(exception.getHttpStatusCode())).body(error);
    }

    @ExceptionHandler(RegistryUserException.class)
    public ResponseEntity<ErrorBaseDto> registryUser(
        RegistryUserException exception,
        HttpServletRequest request) {
        log.error("Error no cadastro: {}", exception.getMessage());
        return buildResponseError(request, exception.getCode(),
            HttpStatus.valueOf(exception.getStatusCode()));
    }

    @ExceptionHandler(WorkStationException.class)
    public ResponseEntity<ErrorBaseDto> workstationException(
        WorkStationException exception,
        HttpServletRequest request) {
        return buildResponseError(
            request, exception.getCode(), UNPROCESSABLE_ENTITY, exception.getValue());
    }

    @ExceptionHandler({Exception.class, RuntimeException.class})
    public ResponseEntity<ErrorBaseDto> defaultHandler(
        Exception exception,
        HttpServletRequest request) {
        log.error("Erro interno requisi????o {}", exception.getMessage(), exception);
        return buildResponseError(request, KNOWN.getCode(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorBaseDto> accessDeniedException(
        AccessDeniedException exception,
        HttpServletRequest request) {
        log.error(
            "Error em processar requisi????o: {} causa: {}",
            request.getRequestURL(),
            exception.getClass().getSimpleName());
        return buildResponseError(request, ACCESS_DENIED.getCode(), FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorBaseDto> accessAuthenticationException(
        AuthenticationException exception,
        HttpServletRequest request) {
        log.error(
            "Error em processar requisi????o: {} causa: {}",
            request.getRequestURL(),
            exception.getClass().getSimpleName());
        return buildResponseError(request, MessagesMapper.UNAUTHORIZED.getCode(), UNAUTHORIZED);
    }

    private ResponseEntity<ErrorBaseDto> getErrorBaseDtoResponseEntity(HttpServletRequest request,
        List<ErrorDetailsDto> errors) {
        var path = request.getServletPath();
        var locale = getLocale(request.getHeader(ACCEPT_LANGUAGE));
        var message = getMessage(ARGUMENT_INVALID.getCode(), locale);
        var error = new ErrorDto()
            .details(errors)
            .message(message)
            .path(path)
            .statusCode(HttpStatus.BAD_REQUEST.value());

        return ResponseEntity.status(BAD_REQUEST).body(error);
    }

    private ResponseEntity<ErrorBaseDto> buildResponseError(
        HttpServletRequest request,
        String code,
        HttpStatus httpStatus,
        String... args) {
        var path = request.getServletPath();
        var locale = getLocale(request.getHeader(ACCEPT_LANGUAGE));
        var message = getMessage(code, locale, args);
        var error = new ErrorDto()
            .message(message)
            .path(path)
            .statusCode(httpStatus.value());

        return ResponseEntity.status(httpStatus).body(error);
    }

    @Override
    public MessageSource getMessageSource() {
        return messageSource;
    }

    private String fieldNameFromPropertyPath(Path path) {
        var list = StreamUtils.createStreamFromIterator(path.iterator()).toList();
        return list.get(list.size() - 1).getName();
    }

    private String checkCase(FieldError error, Locale locale) {
        return switch (error.getField()) {
            case "email" -> getMsgError(error, locale);
            case "plateCar" -> getMessage(
                PLATE_INVALID.getCode(),
                locale,
                String.valueOf(error.getRejectedValue()));
            default -> getMessage(KNOWN.getCode(), locale);
        };
    }

    private String getMsgError(FieldError error, Locale locale) {
        var email = String.valueOf(error.getRejectedValue());
        if (Objects.requireNonNull(error.getDefaultMessage()).endsWith("@alunos.utfpr.edu.br$\"")
            && !email.endsWith("@alunos.utfpr.edu.br")) {
            return getMessage(EMAIL_NOT_INSTITUTIONAL.getCode(), locale, email);
        } else {
            return getMessage(EMAIL_INVALID.getCode(), locale);
        }
    }
}
