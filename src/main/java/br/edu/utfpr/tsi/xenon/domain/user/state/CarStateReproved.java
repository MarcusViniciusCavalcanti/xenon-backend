package br.edu.utfpr.tsi.xenon.domain.user.state;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("REPROVED")
@Scope(value = SCOPE_PROTOTYPE)
public class CarStateReproved extends CarState {

    @Override
    public String stateName() {
        return CarStateName.REPROVED.name();
    }

    @Override
    CarStateName previsionAllowsState() {
        return CarStateName.WAITING_DOCUMENT;
    }

    @Override
    public void executeProcess(CarEntity carEntity) {
        throw new IllegalStateException("Não é possível processar o estado, pois ele é final");
    }
}
