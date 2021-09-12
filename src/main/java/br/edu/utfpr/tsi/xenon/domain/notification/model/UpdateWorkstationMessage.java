package br.edu.utfpr.tsi.xenon.domain.notification.model;

public record UpdateWorkstationMessage(
    ActionChangeWorkstation actionChangeWorkstation)
    implements MessageWebSocket<ActionChangeWorkstation> {

    @Override
    public ActionChangeWorkstation message() {
        return actionChangeWorkstation;
    }
}
