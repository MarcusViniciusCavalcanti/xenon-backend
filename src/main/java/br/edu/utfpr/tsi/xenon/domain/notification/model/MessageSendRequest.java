package br.edu.utfpr.tsi.xenon.domain.notification.model;

import lombok.Builder;
import lombok.Getter;

@Builder
public class MessageSendRequest<T extends MessageWebSocket<?>> {

    private final T message;

    @Getter
    private final String topic;

    public T getValue() {
        return message;
    }
}
