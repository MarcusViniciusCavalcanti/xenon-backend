package br.edu.utfpr.tsi.xenon.domain.user.state;

import static br.edu.utfpr.tsi.xenon.domain.user.entity.CarStatus.BLOCK;
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

@DisplayName("Teste - Unidade - CarStateBlock")
class CarStateBlockTest {

    @Test
    @DisplayName("Deve passar o estado para aprovado o cadastro do carro")
    void shouldPassApproved() {
        var car = new CarEntity();
        car.setAuthorisedAccess(TRUE);
        car.setCarStatus(BLOCK);
        car.setState(BLOCK.name());

        var state = new CarStateBlock();
        state.executeProcess(car);

        assertEquals(CarStateName.APPROVED.name(), car.getState());
        assertEquals(CarStatus.APPROVED, car.getCarStatus());
    }

    @Test
    @DisplayName("Deve retornar o valor BLOCK para o nome do estado")
    void shouldReturnBLOCK() {
        assertEquals(new CarStateBlock().stateName(), CarStateName.BLOCK.name());
    }

    @ParameterizedTest
    @EnumSource(value = CarStateName.class, mode = Mode.EXCLUDE, names = "BLOCK")
    void shouldThrowsIllegalStateException(CarStateName carStateName) {
        var car = new CarEntity();
        car.setAuthorisedAccess(TRUE);
        car.setState(carStateName.name());

        var state = new CarStateBlock();
        assertThrows(IllegalStateException.class, () -> state.executeProcess(car));
    }
}
