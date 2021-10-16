package br.edu.utfpr.tsi.xenon.domain.user.state;

import static br.edu.utfpr.tsi.xenon.domain.user.entity.CarStatus.APPROVED;
import static br.edu.utfpr.tsi.xenon.domain.user.entity.CarStatus.WAITING;
import static br.edu.utfpr.tsi.xenon.domain.user.state.CarState.CarStateName.WAITING_DECISION;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarStatus;
import br.edu.utfpr.tsi.xenon.domain.user.state.CarState.CarStateName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

@DisplayName("Teste - Unidade - CarStateApproved")
class CarStateApprovedTest {

    @Test
    @DisplayName("Deve passar o estado para aprovado o cadastro do carro")
    void shouldPassApproved() {
        var car = new CarEntity();
        car.setAuthorisedAccess(TRUE);
        car.setCarStatus(WAITING);
        car.setState(WAITING_DECISION.name());

        var state = new CarStateApproved();
        state.executeProcess(car);

        assertEquals(CarStateName.APPROVED.name(), car.getState());
        assertEquals(CarStatus.APPROVED, car.getCarStatus());
    }

    @Test
    @DisplayName("Deve passar o estado para bloqueado o cadastro do carro")
    void shouldPassReproved() {
        var car = new CarEntity();
        car.setAuthorisedAccess(FALSE);
        car.setCarStatus(APPROVED);
        car.setState(APPROVED.name());

        var state = new CarStateApproved();
        state.executeProcess(car);

        assertEquals(CarStateName.BLOCK.name(), car.getState());
        assertEquals(CarStatus.BLOCK, car.getCarStatus());
    }

    @ParameterizedTest
    @EnumSource(value = CarStateName.class, mode = Mode.EXCLUDE, names = {
        "WAITING_DECISION",
        "APPROVED",
        "BLOCK"})
    void shouldThrowsIllegalStateException(CarStateName carStateName) {
        var car = new CarEntity();
        car.setAuthorisedAccess(TRUE);
        car.setState(carStateName.name());

        var state = new CarStateApproved();
        assertThrows(IllegalStateException.class, () -> state.executeProcess(car));
    }

    @Test
    @DisplayName("Deve retornar o valor APPROVED para o nome do estado")
    void shouldReturnAPPROVED() {
        assertEquals(new CarStateApproved().stateName(), CarStateName.APPROVED.name());
    }
}
