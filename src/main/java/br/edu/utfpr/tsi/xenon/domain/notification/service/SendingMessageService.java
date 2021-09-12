package br.edu.utfpr.tsi.xenon.domain.notification.service;

import br.edu.utfpr.tsi.xenon.domain.notification.model.MessageWebSocket;
import org.springframework.stereotype.Service;

@Service
public interface SendingMessageService {

    void sendBeforeTransactionCommit(MessageWebSocket<?> messageRequest, String topic);
}
