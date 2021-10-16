package br.edu.utfpr.tsi.xenon.domain.user.state;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("WAITING_DOCUMENT")
@Scope(value = SCOPE_PROTOTYPE)
public class CarStateWaitingDocument extends CarState {

    private CarEntity carEntity;

    @Override
    public String stateName() {
        return CarStateName.WAITING_DOCUMENT.name();
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
    public void executeProcess(CarEntity carEntity) {
        this.carEntity = carEntity;

        if (StringUtils.isBlank(carEntity.getDocument())) {
            throw new IllegalStateException(
                "Carro sem documento, não pode ser submetido para avaliação ou ser aprovado");
        }

        nextState();
    }

    @Override
    void nextState() {
        validateCycleState(CarStateName.WAITING_DECISION.name());
        carEntity.setAuthorisedAccess(Boolean.FALSE);
        carEntity.setCarStatus(CarStatus.WAITING);
        carEntity.setState(CarStateName.WAITING_DECISION.name());
    }
}
