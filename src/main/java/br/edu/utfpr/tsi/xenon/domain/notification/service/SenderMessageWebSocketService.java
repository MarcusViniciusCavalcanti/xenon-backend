package br.edu.utfpr.tsi.xenon.domain.notification.service;

import br.edu.utfpr.tsi.xenon.domain.notification.model.MessageSendRequest;
import br.edu.utfpr.tsi.xenon.domain.notification.model.MessageWebSocket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

@Slf4j
@Service
@RequiredArgsConstructor
public class SenderMessageWebSocketService implements SendingMessageService {

    private final SimpMessagingTemplate simpleMessage;

    private final ApplicationContext context;

    public void sendBeforeTransactionCommit(MessageWebSocket<?> messageRequest, String topic) {
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(),
            "sendBeforeTransactionCommit() foi chamado sem uma transação ativa.");
        log.info("Preparando envio de mensagem para o tópico: {}", topic);
        log.debug("Com payload de mensagem {}", messageRequest.message());
        context.publishEvent(MessageSendRequest.builder()
            .message(messageRequest)
            .topic(topic)
            .build());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleSendEvent(MessageSendRequest<MessageWebSocket<?>> sendRequest) {
        log.info("Enviando mensagem para o tópico {}", sendRequest.getTopic());
        log.debug("Com payload {}", sendRequest.getValue().message());
        var url = String.format("/topic%s", sendRequest.getTopic());
        simpleMessage.convertAndSend(url, sendRequest.getValue().message());
    }
}
