package br.edu.utfpr.tsi.xenon.domain.user.aggregator;

import static org.junit.jupiter.api.Assertions.*;

import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarStatus;
import br.edu.utfpr.tsi.xenon.domain.user.state.CarStateApproved;
import br.edu.utfpr.tsi.xenon.domain.user.state.CarStateBlock;
import br.edu.utfpr.tsi.xenon.domain.user.state.CarStateCreate;
import br.edu.utfpr.tsi.xenon.domain.user.state.CarStateReproved;
import br.edu.utfpr.tsi.xenon.domain.user.state.CarStateWaitingDecision;
import br.edu.utfpr.tsi.xenon.domain.user.state.CarStateWaitingDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {
    ChangeStateCar.class,
    CarStateApproved.class,
    CarStateBlock.class,
    CarStateReproved.class,
    CarStateWaitingDocument.class,
    CarStateWaitingDecision.class,
    CarStateCreate.class
})
@DisplayName("Teste - Unidade - ChangeStateCar")
class ChangeStateCarTest {

    @Autowired
    private ChangeStateCar changeStateCar;

    @Test
    @DisplayName("Deve passar o estado do carro para aguardando documento")
    void shouldChangeStateWaitingDocument() {
        var car = new CarEntity();

        var resultProcess = changeStateCar.executeProcess(car);
        assertEquals("WAITING_DOCUMENT", resultProcess.getState());
        assertEquals(CarStatus.WAITING, resultProcess.getCarStatus());
    }

    @Test
    @DisplayName("Deve passar o estado do carro para aguardando decisão do administrador")
    void shouldChangeStateWaitingDecision() {
        var car = new CarEntity();
        car.setState("WAITING_DOCUMENT");
        car.setCarStatus(CarStatus.WAITING);
        car.setDocument("document");

        var resultProcess = changeStateCar.executeProcess(car);
        assertEquals("WAITING_DECISION", resultProcess.getState());
        assertEquals(CarStatus.WAITING, resultProcess.getCarStatus());
    }

    @Test
    @DisplayName("Deve passar o estado do carro para aprovado")
    void shouldChangeStateApproved() {
        var car = new CarEntity();
        car.setAuthorisedAccess(Boolean.TRUE);
        car.setState("WAITING_DECISION");
        car.setCarStatus(CarStatus.WAITING);

        var resultProcess = changeStateCar.executeProcess(car);
        assertEquals("APPROVED", resultProcess.getState());
        assertEquals(CarStatus.APPROVED, resultProcess.getCarStatus());
    }

    @Test
    @DisplayName("Deve passar o estado do carro para reprovado")
    void shouldChangeStateReproved() {
        var car = new CarEntity();
        car.setAuthorisedAccess(Boolean.FALSE);
        car.setState("WAITING_DECISION");
        car.setCarStatus(CarStatus.WAITING);

        var resultProcess = changeStateCar.executeProcess(car);
        assertEquals("REPROVED", resultProcess.getState());
        assertEquals(CarStatus.REPROVED, resultProcess.getCarStatus());
    }

    @Test
    @DisplayName("Deve passar o estado do carro para bloqueado")
    void shouldChangeStateBlock() {
        var car = new CarEntity();
        car.setAuthorisedAccess(Boolean.FALSE);
        car.setState("APPROVED");
        car.setCarStatus(CarStatus.APPROVED);

        var resultProcess = changeStateCar.executeProcess(car);
        assertEquals("BLOCK", resultProcess.getState());
        assertEquals(CarStatus.BLOCK, resultProcess.getCarStatus());
    }
}
