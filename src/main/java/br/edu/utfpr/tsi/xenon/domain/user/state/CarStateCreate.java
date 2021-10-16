package br.edu.utfpr.tsi.xenon.domain.user.state;

import static br.edu.utfpr.tsi.xenon.domain.user.entity.CarStatus.WAITING;
import static br.edu.utfpr.tsi.xenon.domain.user.state.CarState.CarStateName.CREATE;
import static br.edu.utfpr.tsi.xenon.domain.user.state.CarState.CarStateName.WAITING_DOCUMENT;
import static java.lang.Boolean.FALSE;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("CREATE")
@Scope(value = SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class CarStateCreate extends CarState {

    private CarEntity carEntity;

    @Override
    public String stateName() {
        return CREATE.name();
    }

    @Override
    CarStateName previsionAllowsState() {
        var stateName = StringUtils.defaultString(carEntity.getState(), CREATE.name());
        return CarStateName.valueOf(stateName);
    }

    @Override
    public void executeProcess(CarEntity carEntity) {
        this.carEntity = carEntity;
        nextState();
    }

    @Override
    void nextState() {
        validateCycleState(WAITING_DOCUMENT.name());
        carEntity.setAuthorisedAccess(FALSE);
        carEntity.setCarStatus(WAITING);
        carEntity.setState(WAITING_DOCUMENT.name());
    }
}
