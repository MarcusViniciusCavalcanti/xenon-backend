package br.edu.utfpr.tsi.xenon.domain.notification.model;

public record NewRegistryCarWaitingDecisionMessage() implements MessageWebSocket<String> {

    @Override
    public String message() {
        return "Receive new Car";
    }
}
