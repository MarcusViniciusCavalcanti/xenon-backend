package br.edu.utfpr.tsi.xenon.domain.user.state;

import static br.edu.utfpr.tsi.xenon.domain.user.entity.CarStatus.WAITING;
import static br.edu.utfpr.tsi.xenon.domain.user.state.CarState.CarStateName.BLOCK;
import static br.edu.utfpr.tsi.xenon.domain.user.state.CarState.CarStateName.WAITING_DECISION;
import static br.edu.utfpr.tsi.xenon.domain.user.state.CarState.CarStateName.WAITING_DOCUMENT;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.domain.user.state.CarState.CarStateName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Teste - Unidade - CarStateWaitingDocument")
class CarStateWaitingDocumentTest {

    @Test
    @DisplayName("Deve passar o estado para aprovado o cadastro do carro")
    void shouldPassApproved() {
        var car = new CarEntity();
        car.setAuthorisedAccess(TRUE);
        car.setCarStatus(WAITING);
        car.setDocument("document");
        car.setState(WAITING_DOCUMENT.name());

        var state = new CarStateWaitingDocument();
        state.executeProcess(car);

        assertEquals(CarStateName.WAITING_DECISION.name(), car.getState());
        assertEquals(WAITING, car.getCarStatus());
    }

    @ParameterizedTest
    @EnumSource(value = CarStateName.class, mode = Mode.EXCLUDE, names = {"WAITING_DOCUMENT", "BLOCK"})
    @DisplayName("Deve lançar IllegalStateException quando ciclo de estado está invalidado")
    void shouldThrowsIllegalStateException(CarStateName carStateName) {
        var car = new CarEntity();
        car.setAuthorisedAccess(TRUE);
        car.setState(carStateName.name());
        car.setDocument("document");

        var state = new CarStateWaitingDocument();
        var exception =
            assertThrows(IllegalStateException.class, () -> state.executeProcess(car));
        var msg = "Ciclo de estado impossível, o estado %s não pode passar para %s".formatted(
            carStateName.name(),
            WAITING_DECISION.name()
        );
        assertEquals(msg, exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar IllegalStateException na tentativa de passar para o estado WAITING_DOCUMENT, quando o estado do carro é BLOCK")
    void shouldThrowsIllegalStateExceptionWhenBloc() {
        var car = new CarEntity();
        car.setAuthorisedAccess(TRUE);
        car.setState(BLOCK.name());
        car.setDocument("document");

        var state = new CarStateWaitingDocument();
        var exception =
            assertThrows(IllegalStateException.class, () -> state.executeProcess(car));
        var msg = "Ciclo de estado impossível, o estado %s não pode passar para %s".formatted(
            BLOCK.name(),
            WAITING_DOCUMENT.name()
        );
        assertEquals(msg, exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    @NullSource
    @DisplayName("Deve lançar IllegalStateException quando o carro estiver com documento em branco")
    void shouldThrowsIllegalStateExceptionWhenDocumentIsBlank(String document) {
        var car = new CarEntity();
        car.setAuthorisedAccess(TRUE);
        car.setState(BLOCK.name());
        car.setDocument(document);

        var state = new CarStateWaitingDocument();
        var exception =
            assertThrows(IllegalStateException.class, () -> state.executeProcess(car));
        var msg = "Carro sem documento, não pode ser submetido para avaliação ou ser aprovado";

        assertEquals(msg, exception.getMessage());
    }

    @Test
    @DisplayName("Deve retornar o valor WAITING_DOCUMENT para o nome do estado")
    void shouldReturnWAITING_DOCUMENT() {
        assertEquals(new CarStateWaitingDocument().stateName(), WAITING_DOCUMENT.name());
    }
}
