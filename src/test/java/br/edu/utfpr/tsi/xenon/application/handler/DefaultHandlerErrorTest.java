package br.edu.utfpr.tsi.xenon.application.handler;

import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.ARGUMENT_INVALID;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.KNOWN;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.RESOURCE_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.edu.utfpr.tsi.xenon.application.dto.ErrorDto;
import br.edu.utfpr.tsi.xenon.structure.exception.BusinessException;
import br.edu.utfpr.tsi.xenon.structure.exception.ResourceNotFoundException;
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
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - DefaultHandlerError")
class DefaultHandlerErrorTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private DefaultHandlerError defaultHandlerError;

    @Test
    @DisplayName("Deve retornar erro com status bad request quando MethodArgumentNotValidException")
    void shouldReturnBadRequestWhenMethodArgumentNotValidException() {
        var exception = mock(MethodArgumentNotValidException.class);
        var fieldError = new FieldError("objectName", "field", "defaultMessage");

        when(exception.getFieldErrors()).thenReturn(List.of(fieldError));
        when(request.getServletPath()).thenReturn("path");
        when(request.getHeader("accept-language")).thenReturn("pt-BR");
        when(messageSource
            .getMessage(eq(ARGUMENT_INVALID.getCode()), any(String[].class), any(Locale.class)))
            .thenReturn("message");

        var result = defaultHandlerError.methodArgumentNotValidException(exception, request);

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
    @DisplayName("Deve retornar erro com status not found quando ResourceNotFoundException")
    void shouldReturnNotFoundWhenResourceNotFoundException() {
        var exception = new ResourceNotFoundException("resourceName", "argument");

        when(request.getServletPath()).thenReturn("path");
        when(request.getHeader("accept-language")).thenReturn("pt-BR");

        when(messageSource.getMessage(
            eq(RESOURCE_NOT_FOUND.getCode()),
            any(String[].class),
            any(Locale.class))).thenReturn("message");

        var result = defaultHandlerError.entityNotFound(exception, request);

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
        when(request.getHeader("accept-language")).thenReturn("pt-BR");
        when(messageSource.getMessage(
            eq(KNOWN.getCode()),
            any(String[].class),
            any(Locale.class))).thenReturn("message");

        var result = defaultHandlerError.businessException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        var error = result.getBody();
        assert error != null;
        assertEquals("message", error.getMessage());
        assertEquals("path", error.getPath());
        assertEquals(400, error.getStatusCode());
    }
}
