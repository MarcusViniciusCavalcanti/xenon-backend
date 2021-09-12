package br.edu.utfpr.tsi.xenon.application.handler;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import br.edu.utfpr.tsi.xenon.application.dto.ErrorBaseDto;
import br.edu.utfpr.tsi.xenon.structure.MessagesMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

@Slf4j
public record AuthenticationFailureHandlerImpl(
    MessageSource messageSource) implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception) throws IOException {
        log.error(
            "Erro na solicitação do processo: {} causado por: autenticação com falha",
            request.getRequestURL());

        var responseError = new ErrorBaseDto()
            .message(getMessage(MessagesMapper.UNAUTHORIZED.getCode(), request.getLocale()))
            .statusCode(HttpStatus.UNAUTHORIZED.value())
            .path(request.getServletPath());

        var objectMapper = new ObjectMapper();

        response.getWriter().println(objectMapper.writeValueAsString(responseError));
        response.addHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE);
        response.setStatus(SC_UNAUTHORIZED);
    }

    private String getMessage(String messagesMapper, Locale locale, String... args) {
        return messageSource.getMessage(messagesMapper, args, locale);
    }
}
