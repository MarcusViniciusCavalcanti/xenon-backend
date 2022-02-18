package br.edu.utfpr.tsi.xenon.domain.notification.worker;

import static java.lang.Boolean.TRUE;

import br.edu.utfpr.tsi.xenon.domain.notification.model.MessageSendRequest;
import br.edu.utfpr.tsi.xenon.domain.notification.model.NewRegistryCarWaitingDecisionMessage;
import br.edu.utfpr.tsi.xenon.domain.notification.model.TopicApplication;
import br.edu.utfpr.tsi.xenon.domain.notification.service.SenderMessageWebSocketService;
import br.edu.utfpr.tsi.xenon.domain.user.state.CarState.CarStateName;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotifyCarWaitingDecision {

    public static final Long DELAY = 5L;

    private final CarRepository carRepository;
    private final SenderMessageWebSocketService senderMessageWebSocketService;

    public void tick() {
        log.info("Executando job para enviar notificação de carros aguardando decisão");
        var existCarWaitingDecision =
            carRepository.existsAllByState(CarStateName.WAITING_DECISION.name());

        if (TRUE.equals(existCarWaitingDecision)) {
            log.debug("Alguns carros foram encontrados, enviando mensagem");
            var sendRequest = MessageSendRequest.builder()
                .message(new NewRegistryCarWaitingDecisionMessage())
                .topic(TopicApplication.APPLICATION.topicTo("new-registry-car"))
                .build();

            senderMessageWebSocketService.sendRequest(sendRequest);
        }
    }
}
