package br.edu.utfpr.tsi.xenon.domain.user.state;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarStatus;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("APPROVED")
@Scope(value = SCOPE_PROTOTYPE)
public class CarStateApproved extends CarState {

    private CarEntity carEntity;

    @Override
    public String stateName() {
        return CarStateName.APPROVED.name();
    }

    @Override
    CarStateName previsionAllowsState() {
        return CarStateName.valueOf(carEntity.getState());
    }

    @Override
    public void executeProcess(CarEntity carEntity) {
        this.carEntity = carEntity;
        processNextStep(carEntity);
    }

    @Override
    void nextState() {
        validateCycleState(CarStateName.APPROVED.name());
        setApprovedState(CarStatus.APPROVED, CarStateName.APPROVED);
    }

    @Override
    void alternativeState() {
        setApprovedState(CarStatus.BLOCK, CarStateName.BLOCK);
    }

    private void setApprovedState(CarStatus status, CarStateName state) {
        carEntity.setCarStatus(status);
        carEntity.setState(state.name());
    }
}
