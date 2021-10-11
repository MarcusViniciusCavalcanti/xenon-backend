package br.edu.utfpr.tsi.xenon.application.handler;

import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.*;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.ARGUMENT_INVALID;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.EMAIL_EXIST;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.KNOWN;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.MEDIA_TYPE_NOT_SUPPORTED;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.REQUEST_INVALID;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.REQUEST_METHOD_INVALID;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.RESOURCE_NOT_FOUND;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.URL_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import br.edu.utfpr.tsi.xenon.application.dto.ErrorDto;
import br.edu.utfpr.tsi.xenon.structure.MessagesMapper;
import br.edu.utfpr.tsi.xenon.structure.exception.BusinessException;
import br.edu.utfpr.tsi.xenon.structure.exception.RegistryUserException;
import br.edu.utfpr.tsi.xenon.structure.exception.ResourceNotFoundException;
import br.edu.utfpr.tsi.xenon.structure.exception.WorkStationException;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.NoHandlerFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - DefaultHandlerError")
class HandlerErrorTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private HandlerError handlerError;

    @Test
    @DisplayName("Deve retornar erro com status bad request quando MethodArgumentNotValidException")
    void shouldReturnBadRequestWhenMethodArgumentNotValidException() {
        var exception = mock(MethodArgumentNotValidException.class);
        var fieldError = new FieldError("objectName", "field", "defaultMessage");

        when(exception.getFieldErrors()).thenReturn(List.of(fieldError));
        when(request.getServletPath()).thenReturn("path");
        when(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE)).thenReturn("pt-BR");
        when(messageSource
            .getMessage(eq(ARGUMENT_INVALID.getCode()), any(String[].class), any(Locale.class)))
            .thenReturn("message");

        var result = handlerError.methodArgumentNotValidException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        var error = (ErrorDto) result.getBody();
        assert error != null;
        assertEquals("message", error.getMessage());
        assertEquals("path", error.getPath());
        assertEquals(400, error.getStatusCode());
        assertEquals(fieldError.getField(), error.getDetails().get(0).getField());
        assertEquals(fieldError.getDefaultMessage(),
            error.getDetails().get(0).getDescriptionError());
    }

    @Test
    @DisplayName("Deve retornar erro com mensagem em branco quando MethodArgumentNotValidException")
    void shouldReturnBadRequestWhenMethodArgumentNotValidExceptionWithMsgEmpty() {
        var exception = mock(MethodArgumentNotValidException.class);

        when(exception.getFieldErrors()).thenReturn(List.of());
        when(request.getServletPath()).thenReturn("path");
        when(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE)).thenReturn("pt-BR");
        when(messageSource
            .getMessage(eq(ARGUMENT_INVALID.getCode()), any(String[].class), any(Locale.class)))
            .thenReturn("message");

        var result = handlerError.methodArgumentNotValidException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        var error = (ErrorDto) result.getBody();
        assert error != null;
        assertEquals("message", error.getMessage());
        assertEquals("path", error.getPath());
        assertEquals(400, error.getStatusCode());
        assertTrue(error.getDetails().isEmpty());
    }

    @Test
    @DisplayName("Deve retornar erro com status not found quando ResourceNotFoundException")
    void shouldReturnNotFoundWhenResourceNotFoundException() {
        var exception = new ResourceNotFoundException("resourceName", "argument");

        when(request.getServletPath()).thenReturn("path");
        when(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE)).thenReturn("pt-BR");

        when(messageSource.getMessage(
            eq(RESOURCE_NOT_FOUND.getCode()),
            any(String[].class),
            any(Locale.class))).thenReturn("message");

        var result = handlerError.entityNotFound(exception, request);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        var error = result.getBody();
        assert error != null;
        assertEquals("message", error.getMessage());
        assertEquals("path", error.getPath());
        assertEquals(404, error.getStatusCode());
    }

    @Test
    @DisplayName("Deve retornar erro quando BusinessException")
    void shouldReturnErroWhenBusinessException() {
        var exception = new BusinessException(400, KNOWN.getCode());

        when(request.getServletPath()).thenReturn("path");
        when(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE)).thenReturn("pt-BR");
        when(messageSource.getMessage(
            eq(KNOWN.getCode()),
            any(String[].class),
            any(Locale.class))).thenReturn("message");

        var result = handlerError.businessException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        var error = result.getBody();
        assert error != null;
        assertEquals("message", error.getMessage());
        assertEquals("path", error.getPath());
        assertEquals(400, error.getStatusCode());
    }

    @Test
    @DisplayName("Deve retornar erro quando HttpMessageNotReadableException")
    void shouldReturnHttpMessageNotReadableException() {
        var exception = mock(HttpMessageNotReadableException.class);

        when(request.getServletPath()).thenReturn("path");
        when(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE)).thenReturn("pt-BR");
        when(messageSource
            .getMessage(eq(REQUEST_INVALID.getCode()), any(String[].class), any(Locale.class)))
            .thenReturn("message");

        var result = handlerError.httpMessageNotReadableException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        var error = (ErrorDto) result.getBody();
        assert error != null;
        assertEquals("message", error.getMessage());
        assertEquals("path", error.getPath());
        assertEquals(400, error.getStatusCode());
    }

    @Test
    @DisplayName("Deve retornar erro quando HttpRequestMethodNotSupportedException")
    void shouldReturnHttpRequestMethodNotSupportedException() {
        var exception = mock(HttpRequestMethodNotSupportedException.class);

        when(request.getServletPath()).thenReturn("path");
        when(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE)).thenReturn("pt-BR");
        when(messageSource
            .getMessage(eq(REQUEST_METHOD_INVALID.getCode()), any(String[].class), any(Locale.class)))
            .thenReturn("message");

        var result = handlerError.httpRequestMethodNotSupportedException(exception, request);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, result.getStatusCode());
        var error = (ErrorDto) result.getBody();
        assert error != null;
        assertEquals("message", error.getMessage());
        assertEquals("path", error.getPath());
        assertEquals(405, error.getStatusCode());
    }

    @Test
    @DisplayName("Deve retornar erro quando NoHandlerFoundException")
    void shouldReturnNoHandlerFoundException() {
        var exception = mock(NoHandlerFoundException.class);

        when(request.getServletPath()).thenReturn("path");
        when(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE)).thenReturn("pt-BR");
        when(messageSource
            .getMessage(eq(URL_NOT_FOUND.getCode()), any(String[].class), any(Locale.class)))
            .thenReturn("message");

        var result = handlerError.noHandlerFoundException(exception, request);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        var error = (ErrorDto) result.getBody();
        assert error != null;
        assertEquals("message", error.getMessage());
        assertEquals("path", error.getPath());
        assertEquals(404, error.getStatusCode());
    }

    @Test
    @DisplayName("Deve retornar erro quando HttpMediaTypeNotSupportedException")
    void shouldReturnHttpMediaTypeNotSupportedException() {
        var exception = mock(HttpMediaTypeNotSupportedException.class);

        when(request.getServletPath()).thenReturn("path");
        when(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE)).thenReturn("pt-BR");
        when(messageSource
            .getMessage(eq(MEDIA_TYPE_NOT_SUPPORTED.getCode()), any(String[].class), any(Locale.class)))
            .thenReturn("message");

        var result = handlerError.httpMediaTypeNotSupportedException(exception, request);

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, result.getStatusCode());
        var error = (ErrorDto) result.getBody();
        assert error != null;
        assertEquals("message", error.getMessage());
        assertEquals("path", error.getPath());
        assertEquals(415, error.getStatusCode());
    }

    @Test
    @DisplayName("Deve retornar erro quando HttpMediaTypeNotSupportedException")
    void shouldReturnRegistryUserException() {
        var exception =new RegistryUserException(EMAIL_EXIST.getCode());

        when(request.getServletPath()).thenReturn("path");
        when(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE)).thenReturn("pt-BR");
        when(messageSource
            .getMessage(eq(EMAIL_EXIST.getCode()), any(String[].class), any(Locale.class)))
            .thenReturn("message");

        var result = handlerError.registryUser(exception, request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.getStatusCode());
        var error = (ErrorDto) result.getBody();
        assert error != null;
        assertEquals("message", error.getMessage());
        assertEquals("path", error.getPath());
        assertEquals(422, error.getStatusCode());
    }

    @Test
    @DisplayName("Deve retornar erro quando HttpMediaTypeNotSupportedException")
    void shouldReturnException() {
        var exception =new Exception(EMAIL_EXIST.getCode());

        when(request.getServletPath()).thenReturn("path");
        when(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE)).thenReturn("pt-BR");
        when(messageSource
            .getMessage(eq(KNOWN.getCode()), any(String[].class), any(Locale.class)))
            .thenReturn("message");

        var result = handlerError.defaultHandler(exception, request);

        assertEquals(INTERNAL_SERVER_ERROR, result.getStatusCode());
        var error = (ErrorDto) result.getBody();
        assert error != null;
        assertEquals("message", error.getMessage());
        assertEquals("path", error.getPath());
        assertEquals(500, error.getStatusCode());
    }

    @Test
    @DisplayName("Deve retornar erro quando RuntimeException")
    void shouldReturnRuntimeException() {
        var exception =new RuntimeException(KNOWN.getCode());

        when(request.getServletPath()).thenReturn("path");
        when(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE)).thenReturn("pt-BR");
        when(messageSource
            .getMessage(eq(KNOWN.getCode()), any(String[].class), any(Locale.class)))
            .thenReturn("message");

        var result = handlerError.defaultHandler(exception, request);

        assertEquals(INTERNAL_SERVER_ERROR, result.getStatusCode());
        var error = (ErrorDto) result.getBody();
        assert error != null;
        assertEquals("message", error.getMessage());
        assertEquals("path", error.getPath());
        assertEquals(500, error.getStatusCode());
    }

    @Test
    @DisplayName("Deve retornar erro quando WorkStationException")
    void shouldReturnWorkStationException() {
        var exception = new WorkStationException(IP_WORKSTATION_EXIST.getCode(), "value");

        when(request.getServletPath()).thenReturn("path");
        when(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE)).thenReturn("pt-BR");
        when(messageSource
            .getMessage(eq(IP_WORKSTATION_EXIST.getCode()), any(String[].class), any(Locale.class)))
            .thenReturn("message");

        var result = handlerError.workstationException(exception, request);

        assertEquals(UNPROCESSABLE_ENTITY, result.getStatusCode());
        var error = (ErrorDto) result.getBody();
        assert error != null;
        assertEquals("message", error.getMessage());
        assertEquals("path", error.getPath());
        assertEquals(422, error.getStatusCode());
    }


}
