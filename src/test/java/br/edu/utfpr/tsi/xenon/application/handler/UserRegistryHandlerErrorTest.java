package br.edu.utfpr.tsi.xenon.application.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import br.edu.utfpr.tsi.xenon.structure.MessagesMapper;
import br.edu.utfpr.tsi.xenon.structure.exception.EmailErrorException;
import br.edu.utfpr.tsi.xenon.structure.exception.PlateException;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - UserRegistryHandlerError")
class UserRegistryHandlerErrorTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private UserRegistryHandlerError userRegistryError;

    @Test
    @DisplayName("deve retornar status code conflict quando placa já existe")
    void shouldReturnConflictPlate() {
        var exception = new PlateException("plate", MessagesMapper.PLATE_ALREADY.getCode());

        when(httpServletRequest.getServletPath()).thenReturn("localhost");
        when(httpServletRequest.getHeader("accept-language")).thenReturn(Locale.ROOT.getLanguage());

        var response = userRegistryError.plateException(exception, httpServletRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @DisplayName("deve retornar status code unprocessable quando placa está inválida")
    void shouldReturnUnprocessedPlate() {
        var exception = new PlateException("plate", MessagesMapper.PLATE_INVALID.getCode());

        when(httpServletRequest.getServletPath()).thenReturn("localhost");
        when(httpServletRequest.getHeader("accept-language")).thenReturn(Locale.ROOT.getLanguage());

        var response = userRegistryError.plateException(exception, httpServletRequest);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    }

    @Test
    @DisplayName("deve retornar status code conflict quando e-mail já existe")
    void shouldReturnConflictEmail() {
        var exception =
            new EmailErrorException("emaill@email.com", MessagesMapper.EMAIL_EXIST.getCode());

        when(httpServletRequest.getServletPath()).thenReturn("localhost");
        when(httpServletRequest.getHeader("accept-language")).thenReturn(Locale.ROOT.getLanguage());

        var response = userRegistryError.emailExistException(exception, httpServletRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @DisplayName("deve retornar status code unprocessable quando e-mail não é institucional")
    void shouldReturnUnprocessedEmail() {
        var exception =
            new PlateException("email@email.com", MessagesMapper.EMAIL_NOT_INSTITUTIONAL.getCode());

        when(httpServletRequest.getServletPath()).thenReturn("localhost");
        when(httpServletRequest.getHeader("accept-language")).thenReturn(Locale.ROOT.getLanguage());

        var response = userRegistryError.plateException(exception, httpServletRequest);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    }
}
