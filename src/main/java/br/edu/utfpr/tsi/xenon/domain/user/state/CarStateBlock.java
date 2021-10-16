package br.edu.utfpr.tsi.xenon.domain.user.state;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarStatus;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("BLOCK")
@Scope(value = SCOPE_PROTOTYPE)
public class CarStateBlock extends CarState {

    private CarEntity carEntity;

    @Override
    public String stateName() {
        return CarStateName.BLOCK.name();
    }

    @Override
    CarStateName previsionAllowsState() {
        var carStateName = CarStateName.valueOf(carEntity.getState());

        if (carStateName == CarStateName.WAITING_DECISION) {
            throw getIllegalStateException(carStateName.name(), stateName());
        }

        return carStateName;
    }

    @Override
    public void executeProcess(CarEntity carEntity) {
        this.carEntity = carEntity;
        processNextStep(carEntity);
    }

    @Override
    void nextState() {
        validateCycleState(CarStateName.APPROVED.name());
        carEntity.setCarStatus(CarStatus.APPROVED);
        carEntity.setState(CarStateName.APPROVED.name());
    }
}
