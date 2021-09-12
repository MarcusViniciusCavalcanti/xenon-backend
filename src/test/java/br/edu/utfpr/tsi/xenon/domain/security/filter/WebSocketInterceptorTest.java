package br.edu.utfpr.tsi.xenon.domain.security.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.messaging.simp.stomp.StompCommand.CONNECT;
import static org.springframework.messaging.simp.stomp.StompCommand.SEND;

import br.edu.utfpr.tsi.xenon.domain.security.service.SecurityContextUserService;
import java.security.Principal;
import java.util.Collection;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - WebSocketInterceptor")
class WebSocketInterceptorTest {

    @Mock
    private SecurityContextUserService securityContextUserService;

    @Mock
    private MessageChannel messageChannel;

    @InjectMocks
    private WebSocketInterceptor webSocketInterceptor;

    @Test
    @DisplayName("Deve ")
    void shouldAuthenticatedReceiveMessageConnectUserIsAuthenticated() {
        var simpMessageHeaderAccessor = StompHeaderAccessor.create(CONNECT);

        simpMessageHeaderAccessor.setSessionId("systemTest");
        simpMessageHeaderAccessor.setDestination("/topic/topic");
        simpMessageHeaderAccessor.setLeaveMutable(true);

        simpMessageHeaderAccessor.setUser(new UserTest());

        simpMessageHeaderAccessor.addNativeHeader("Authorization", "Bearer");

        var payload = "message";
        var genericMessage =
            new GenericMessage<>(payload, simpMessageHeaderAccessor.getMessageHeaders());

        var message = webSocketInterceptor.preSend(genericMessage, messageChannel);

        assert message != null;
        assertEquals(payload, message.getPayload());

    }

    @Test
    void shouldSendMessageAuthenticatedReceiveMessageConnectUserIsAuthenticated() {
        var simpMessageHeaderAccessor = StompHeaderAccessor.create(SEND);

        simpMessageHeaderAccessor.setSessionId("systemTest");
        simpMessageHeaderAccessor.setDestination("/topic/topic");
        simpMessageHeaderAccessor.setLeaveMutable(true);

        simpMessageHeaderAccessor.setUser(new UserTest());

        simpMessageHeaderAccessor.addNativeHeader("Authorization", "Bearer");

        String payload = "message";
        var genericMessage =
            new GenericMessage<>(payload, simpMessageHeaderAccessor.getMessageHeaders());

        var message = webSocketInterceptor.preSend(genericMessage, messageChannel);

        assert message != null;
        assertEquals(payload, message.getPayload());

    }

    @Test
    void shouldAuthenticateMessageWhenSendMessageWhitBearer() {
        var simpMessageHeaderAccessor = StompHeaderAccessor.create(CONNECT);

        simpMessageHeaderAccessor.setSessionId("systemTest");
        simpMessageHeaderAccessor.setDestination("/topic/topic");
        simpMessageHeaderAccessor.setLeaveMutable(true);

        simpMessageHeaderAccessor.addNativeHeader("Authorization", "Bearer");

        var payload = "message";
        var genericMessage =
            new GenericMessage<>(payload, simpMessageHeaderAccessor.getMessageHeaders());

        var authentication =
            new TestAuthentication(Set.of(new SimpleGrantedAuthority("ROLE_OPERATOR")));
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        doNothing().when(securityContextUserService).receiveTokenToSecurityHolder(any());
        when(securityContext.getAuthentication()).thenReturn(authentication);

        var message = webSocketInterceptor.preSend(genericMessage, messageChannel);

        assert message != null;
        assertEquals(payload, message.getPayload());

    }

    @Test
    void shouldReturnAccessDeniedWhenRoleIsDiffOperator() {
        var simpMessageHeaderAccessor = StompHeaderAccessor.create(CONNECT);

        simpMessageHeaderAccessor.setSessionId("systemTest");
        simpMessageHeaderAccessor.setDestination("/topic/topic");
        simpMessageHeaderAccessor.setLeaveMutable(true);

        simpMessageHeaderAccessor.addNativeHeader("Authorization", "Bearer");

        var payload = "message";
        var genericMessage =
            new GenericMessage<>(payload, simpMessageHeaderAccessor.getMessageHeaders());

        var authentication =
            new TestAuthentication(Set.of(new SimpleGrantedAuthority("ROLE_DRIVER")));
        var securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        doNothing().when(securityContextUserService).receiveTokenToSecurityHolder(any());
        when(securityContext.getAuthentication()).thenReturn(authentication);

        assertThrows(AccessDeniedException.class,
            () -> webSocketInterceptor.preSend(genericMessage, messageChannel));
    }

    @Test
    void shouldReturnNullPointerWhenHeaderNotInstanceStompAccessor() {
        var simpMessageHeaderAccessor = StompHeaderAccessor.create(SimpMessageType.CONNECT);

        var payload = "message";
        var genericMessage =
            new GenericMessage<>(payload, simpMessageHeaderAccessor.getMessageHeaders());

        assertThrows(IllegalStateException.class,
            () -> webSocketInterceptor.preSend(genericMessage, messageChannel));
    }

    @Test
    void shouldSendMessageAuthenticateMessageWhenSendMessageWhitBearer() {
        var simpMessageHeaderAccessor = StompHeaderAccessor.create(SEND);

        simpMessageHeaderAccessor.setSessionId("systemTest");
        simpMessageHeaderAccessor.setDestination("/topic/topic");
        simpMessageHeaderAccessor.setLeaveMutable(true);

        simpMessageHeaderAccessor.addNativeHeader("Authorization", "Bearer");

        var payload = "message";
        var genericMessage =
            new GenericMessage<>(payload, simpMessageHeaderAccessor.getMessageHeaders());

        var message = webSocketInterceptor.preSend(genericMessage, messageChannel);

        assert message != null;
        assertEquals(payload, message.getPayload());

    }

    @Test
    void shouldMessageWhenWithoutHeaderAuthorization() {
        var simpMessageHeaderAccessor = StompHeaderAccessor.create(CONNECT);

        simpMessageHeaderAccessor.setSessionId("systemTest");
        simpMessageHeaderAccessor.setDestination("/topic/topic");
        simpMessageHeaderAccessor.setLeaveMutable(true);

        var payload = "message";
        var genericMessage =
            new GenericMessage<>(payload, simpMessageHeaderAccessor.getMessageHeaders());

        assertThrows(IllegalStateException.class,
            () -> webSocketInterceptor.preSend(genericMessage, messageChannel));
    }

    @Test
    void shouldReturnTrue() {
        //noinspection ConstantConditions
        assertTrue(webSocketInterceptor.preReceive(null));
    }

    private static class UserTest implements Principal {

        @Override
        public String getName() {
            return "UserTest";
        }
    }

    public record TestAuthentication(
        Collection<? extends GrantedAuthority> roles)
        implements Authentication {

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return roles;
        }

        @Override
        public Object getCredentials() {
            return null;
        }

        @Override
        public Object getDetails() {
            return null;
        }

        @Override
        public Object getPrincipal() {
            return null;
        }

        @Override
        public boolean isAuthenticated() {
            return false;
        }

        @Override
        public void setAuthenticated(boolean b) throws IllegalArgumentException {

        }

        @Override
        public String getName() {
            return null;
        }
    }
}
