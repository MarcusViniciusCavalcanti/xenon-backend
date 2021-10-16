package br.edu.utfpr.tsi.xenon.domain.user.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarStatus;
import br.edu.utfpr.tsi.xenon.domain.user.state.CarState.CarStateName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;


@DisplayName("Teste - Unidade - CarStateCreate")
class CarStateCreateTest {

    @Test
    @DisplayName("Deve passar o estado do cadastro do carro para aguardando documento")
    void shouldChangeStateCreateCar() {
        var car = new CarEntity();

        var state = new CarStateCreate();
        state.executeProcess(car);

        assertFalse(car.getAuthorisedAccess());
        assertEquals(CarStatus.WAITING, car.getCarStatus());
        assertEquals(CarStateName.WAITING_DOCUMENT.name(), car.getState());
    }

    @Test
    @DisplayName("Deve lançar IllegalStateException quando invocado estado alternativo")
    void shouldThrowsIllegalStateExceptionWhenCallAlternativeState() {
        var state = new CarStateCreate();
        assertThrows(IllegalStateException.class, state::alternativeState);
    }

    @Test
    @DisplayName("Deve retornar o valor CREATE para o nome do estado")
    void shouldReturnCREATE() {
        assertEquals(new CarStateCreate().stateName(), CarStateName.CREATE.name());
    }

    @ParameterizedTest
    @EnumSource(value = CarStateName.class, mode = Mode.EXCLUDE, names = "CREATE")
    void shouldThrowsIllegalStateException(CarStateName carStateName) {
        var car = new CarEntity();
        car.setState(carStateName.name());

        var state = new CarStateCreate();
        assertThrows(IllegalStateException.class, () -> state.executeProcess(car));
    }
}
