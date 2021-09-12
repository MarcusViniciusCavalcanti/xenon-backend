package br.edu.utfpr.tsi.xenon.application.handler;

import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.ACCESS_DENIED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - AccessDeniedFailureHandler")
class AccessDeniedFailureHandlerTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Test
    @DisplayName("Deve retornar um response com error Forbidden")
    void shouldReturnForbidden() throws IOException {
        var handler = new AccessDeniedFailureHandler(messageSource);
        var writer = mock(PrintWriter.class);

        when(messageSource
            .getMessage(eq(ACCESS_DENIED.getCode()), any(String[].class), any(Locale.class)))
            .thenReturn("message");
        when(response.getWriter()).thenReturn(writer);
        when(request.getLocale()).thenReturn(Locale.ROOT);

        handler.handle(request, response, new AccessDeniedException(""));

        verify(response).setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE);
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }
}
