package br.edu.utfpr.tsi.xenon.domain.security.filter;

import static org.springframework.messaging.simp.stomp.StompCommand.CONNECT;

import br.edu.utfpr.tsi.xenon.domain.security.service.SecurityContextUserService;
import java.util.Collection;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.websocket.AuthenticationException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketInterceptor implements ChannelInterceptor {

    private final SecurityContextUserService securityContextUserService;

    @Override
    public Message<?> preSend(
        @SuppressWarnings("NullableProblems") Message<?> message,
        @SuppressWarnings("NullableProblems") MessageChannel channel) {
        var accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (Objects.isNull(accessor)) {
            throw new IllegalStateException("Accessor not be null");
        }

        log.info("recebendo mensagem com comando: {}. . .", accessor.getCommand());
        if (CONNECT.equals(accessor.getCommand()) && message.getHeaders().get("simpUser") != null) {
            return message;
        }

        if (CONNECT.equals(accessor.getCommand())) {
            log.info("recebendo conectando...");
            var authorization = accessor.getNativeHeader("Authorization");

            log.debug("verificando token");
            if (Objects.isNull(authorization)) {
                log.error("Tentativa de conectar no web socket sem header authorization");
                throw new IllegalStateException("Header não pode ser nulo");
            }

            var token = authorization.get(0).replace("Bearer", "").trim();
            securityContextUserService.receiveTokenToSecurityHolder(token);

            var authorities = SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities();
            var hasNotOperator = authorities.stream().noneMatch(
                grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_OPERATOR"));

            if (hasNotOperator) {
                SecurityContextHolder.clearContext();
                throw new AccessDeniedException("Acesso negado para acessar o websocket");
            }
        }

        return message;
    }

    @Override
    public boolean preReceive(@SuppressWarnings("NullableProblems") MessageChannel channel) {
        return true;
    }
}
