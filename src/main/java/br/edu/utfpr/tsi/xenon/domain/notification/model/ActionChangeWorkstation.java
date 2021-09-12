package br.edu.utfpr.tsi.xenon.domain.notification.model;

import br.edu.utfpr.tsi.xenon.application.dto.WorkstationDto;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public final class ActionChangeWorkstation {

    private final ActionType type;
    private final WorkstationDto workstation;

    public ActionChangeWorkstation(ActionType type, WorkstationDto workstation) {
        this.type = type;
        this.workstation = workstation;
    }

    public enum ActionType {
        UPDATE, DELETE
    }

}
