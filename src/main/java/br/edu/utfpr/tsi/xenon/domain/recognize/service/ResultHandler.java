package br.edu.utfpr.tsi.xenon.domain.recognize.service;

import br.edu.utfpr.tsi.xenon.domain.notification.model.MessageReceiveRecognizer;
import br.edu.utfpr.tsi.xenon.domain.notification.model.ResultProcessRecognizer;
import br.edu.utfpr.tsi.xenon.domain.notification.model.TopicApplication;
import br.edu.utfpr.tsi.xenon.domain.notification.service.SendingMessageService;
import br.edu.utfpr.tsi.xenon.domain.recognize.model.ResultPlate;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class ResultHandler {

    private final ResultHandler next;

    private final SendingMessageService sendingMessageService;
    private final CarRepository carRepository;

    public void handleResult(List<ResultPlate> input, Long workstation) {
        if (Objects.nonNull(next)) {
            getNext().handleResult(input, workstation);
        }
    }

    public ResultHandler getNext() {
        return next;
    }

    protected void sending(ResultProcessRecognizer processRecognizer, Long workstationId) {
        log.info("{} resultado do processo '{}'", this, processRecognizer.getIdentifier());
        log.debug(
            "{} resultado do processo '{}' id da estação de trabalho: {}",
            this,
            processRecognizer,
            workstationId);
        var message = new MessageReceiveRecognizer(processRecognizer);
        sendingMessageService.sendBeforeTransactionCommit(
            message,
            TopicApplication.RECOGNIZER.topicTo("%d/recognizer".formatted(workstationId)));
    }

    protected void prepareMessageAndSend(
        CarEntity carEntity,
        ResultPlate resultPlate,
        Long workstation) {
        var result = ResultProcessRecognizer.builder()
            .authorize(carEntity.getAuthorisedAccess())
            .confidence(resultPlate.confidence())
            .driverName(carEntity.getUser().getName())
            .identifier(Boolean.TRUE)
            .modelCar(carEntity.getModel())
            .plate(carEntity.getPlate())
            .userAvatar(carEntity.getUser().getAvatar())
            .build();

        sending(result, workstation);
        carEntity.setLastAccess(LocalDateTime.now());
        carRepository.saveAndFlush(carEntity);
    }
}
