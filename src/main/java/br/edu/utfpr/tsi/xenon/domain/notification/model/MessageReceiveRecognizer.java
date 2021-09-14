package br.edu.utfpr.tsi.xenon.domain.notification.model;

public record MessageReceiveRecognizer(
    ResultProcessRecognizer resultProcessRecognizer)
    implements MessageWebSocket<ResultProcessRecognizer> {

    @Override
    public ResultProcessRecognizer message() {
        return resultProcessRecognizer;
    }
}
