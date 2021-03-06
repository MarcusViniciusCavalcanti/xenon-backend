package br.edu.utfpr.tsi.xenon.domain.user.state;

import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.domain.user.state.CarState.CarStateName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("Teste - Unidade - CarStateReproved")
class CarStateReprovedTest {

    @ParameterizedTest
    @EnumSource(value = CarStateName.class)
    @DisplayName("Deve lançar IllegalStateException na tentativa de processar o estado")
    void shouldThrowsIllegalStateException(CarStateName carStateName) {
        var car = new CarEntity();
        car.setAuthorisedAccess(TRUE);
        car.setState(carStateName.name());

        var state = new CarStateReproved();
        assertThrows(IllegalStateException.class, state::nextState);
        var illegalStateException =
            assertThrows(IllegalStateException.class, () -> state.executeProcess(car));

        assertEquals("Não é possível processar o estado, pois ele é final", illegalStateException.getMessage());
    }

    @Test
    @DisplayName("Deve retornar o valor REPROVED para o nome do estado")
    void shouldReturnREPROVED() {
        assertEquals(new CarStateReproved().stateName(), CarStateName.REPROVED.name());
    }

    @Test
    @DisplayName("Deve retornar o valor WAITING_DOCUMENT para o nome do estado anterior")
    void shouldReturnWAITING_DOCUMENT() {
        assertEquals(CarStateName.WAITING_DOCUMENT, new CarStateReproved().previsionAllowsState());
    }

    @Test
    @DisplayName("Deve lançar IllegalStateException quando tentar processar o próximo estado")
    void shouldThrowsIllegalStateExceptionWhenExecuteProcess() {
        var carStateReproved = new CarStateReproved();
        var exception  = assertThrows(IllegalStateException.class,
            () -> carStateReproved.executeProcess(new CarEntity()));
        assertEquals("Não é possível processar o estado, pois ele é final", exception.getMessage());
    }
}
