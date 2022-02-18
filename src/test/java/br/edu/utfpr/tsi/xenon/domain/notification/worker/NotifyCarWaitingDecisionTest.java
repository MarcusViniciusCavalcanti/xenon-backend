package br.edu.utfpr.tsi.xenon.domain.notification.worker;

import static br.edu.utfpr.tsi.xenon.domain.notification.model.TopicApplication.APPLICATION;
import static br.edu.utfpr.tsi.xenon.domain.user.state.CarState.CarStateName.WAITING_DECISION;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.utfpr.tsi.xenon.domain.notification.service.SenderMessageWebSocketService;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - NotifyCarWaitingDecision")
class NotifyCarWaitingDecisionTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private SenderMessageWebSocketService senderMessageWebSocketService;

    @InjectMocks
    private NotifyCarWaitingDecision notifyCarWaitingDecision;

    @Test
    @DisplayName("Deve enviar mensagem quando encontrar carros aguardando aprovação/reprovação")
    void shouldSendMessageWhenCarWaitingDecision() {
        when(carRepository.existsAllByState(WAITING_DECISION.name())).thenReturn(TRUE);

        notifyCarWaitingDecision.tick();

        verify(carRepository).existsAllByState(WAITING_DECISION.name());
        verify(senderMessageWebSocketService).sendRequest(
            argThat(messageRequest -> {
                var equalTopic =
                    APPLICATION.topicTo("new-registry-car").equals(messageRequest.getTopic());
                var equalMessage = messageRequest.getValue().message().equals("Receive new Car");

                return equalMessage && equalTopic;
            }));
    }

    @Test
    @DisplayName("Deve enviar mensagem quando encontrar carros aguardando aprovação/reprovação")
    void shouldNotSendMessageWhenCarWaitingDecision() {
        when(carRepository.existsAllByState(WAITING_DECISION.name())).thenReturn(FALSE);

        notifyCarWaitingDecision.tick();

        verify(carRepository).existsAllByState(WAITING_DECISION.name());
        verify(senderMessageWebSocketService, never()).sendRequest(any());
    }
}
