package br.edu.utfpr.tsi.xenon.domain.user.state;

import static java.lang.Boolean.FALSE;

import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import java.util.List;
import java.util.Map;

public abstract class CarState {

    private final Map<String, List<CarStateName>> allowsStates = Map.of(
        CarStateName.CREATE.name(), List.of(CarStateName.WAITING_DOCUMENT),
        CarStateName.WAITING_DOCUMENT.name(), List.of(CarStateName.WAITING_DECISION),
        CarStateName.WAITING_DECISION.name(), List.of(CarStateName.APPROVED, CarStateName.REPROVED),
        CarStateName.APPROVED.name(), List.of(CarStateName.BLOCK),
        CarStateName.REPROVED.name(), List.of(CarStateName.REPROVED),
        CarStateName.BLOCK.name(), List.of(CarStateName.APPROVED)
    );

    public abstract void executeProcess(CarEntity carEntity);

    abstract String stateName();

    abstract CarStateName previsionAllowsState();

    void nextState() {
        throw new IllegalStateException(
            "Não é possível avançar o estado quando o estado é %s".formatted(stateName()));
    }

    void alternativeState() {
        throw new IllegalStateException(
            "Não é possível retornar o estado quando o estado é %s".formatted(stateName()));
    }

    void processNextStep(CarEntity carEntity) {
        if (Boolean.TRUE.equals(carEntity.getAuthorisedAccess())) {
            nextState();
        } else {
            alternativeState();
        }
    }

    void validateCycleState(String actual) {
        var from = previsionAllowsState();
        var to = CarStateName.valueOf(actual);
        var anyMatch = allowsStates.get(from.name()).contains(to);

        if (FALSE.equals(anyMatch)) {
            throw getIllegalStateException(from.name(), actual);
        }
    }

    IllegalStateException getIllegalStateException(String from, String actual) {
        return new IllegalStateException(
            "Ciclo de estado impossível, o estado %s não pode passar para %s".formatted(
                from,
                actual
            )
        );
    }

    enum CarStateName {
        CREATE,
        APPROVED,
        BLOCK,
        REPROVED,
        WAITING_DOCUMENT,
        WAITING_DECISION,
    }
}
