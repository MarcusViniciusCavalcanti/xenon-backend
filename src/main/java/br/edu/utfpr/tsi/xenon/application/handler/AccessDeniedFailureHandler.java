package br.edu.utfpr.tsi.xenon.application.handler;

import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.ACCESS_DENIED;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import br.edu.utfpr.tsi.xenon.application.controller.EndpointsTranslator;
import br.edu.utfpr.tsi.xenon.application.dto.ErrorBaseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

@Slf4j
public record AccessDeniedFailureHandler(MessageSource messageSource)
    implements AccessDeniedHandler, EndpointsTranslator {

    @Override
    public void handle(HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException exception) throws IOException {
        log.error(
            "Error em processar requisição: {} causa: {}",
            request.getRequestURL(),
            exception.getClass().getSimpleName());

        var responseError = new ErrorBaseDto()
            .message(getMessage(ACCESS_DENIED.getCode(), request.getLocale()))
            .statusCode(FORBIDDEN.value())
            .path(request.getServletPath());

        var objectMapper = new ObjectMapper();

        response.getWriter().println(objectMapper.writeValueAsString(responseError));
        response.setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE);
        response.setStatus(SC_FORBIDDEN);
    }

    @Override
    public MessageSource getMessage() {
        return messageSource;
    }
}
