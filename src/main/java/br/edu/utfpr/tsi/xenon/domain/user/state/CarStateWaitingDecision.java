package br.edu.utfpr.tsi.xenon.domain.user.state;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarStatus;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("WAITING_DECISION")
@Scope(value = SCOPE_PROTOTYPE)
public class CarStateWaitingDecision extends CarState {
    private CarEntity carEntity;

    @Override
    public void executeProcess(CarEntity carEntity) {
        this.carEntity = carEntity;
        processNextStep(carEntity);
    }

    @Override
    String stateName() {
        return CarStateName.WAITING_DECISION.name();
    }

    @Override
    CarStateName previsionAllowsState() {
        var carStateName = CarStateName.valueOf(carEntity.getState());

        if (carStateName == CarStateName.BLOCK) {
            throw getIllegalStateException(carStateName.name(), stateName());
        }

        return carStateName;
    }

    @Override
    void nextState() {
        validateCycleState(CarStateName.APPROVED.name());
        carEntity.setCarStatus(CarStatus.APPROVED);
        carEntity.setState(CarStateName.APPROVED.name());
    }

    @Override
    void alternativeState() {
        carEntity.setCarStatus(CarStatus.REPROVED);
        carEntity.setState(CarStateName.REPROVED.name());
    }
}
