package br.edu.utfpr.tsi.xenon.domain.recognize.service;

import br.edu.utfpr.tsi.xenon.application.service.WorkstationApplicationService;
import br.edu.utfpr.tsi.xenon.domain.notification.model.MessageReceiveRecognizer;
import br.edu.utfpr.tsi.xenon.domain.notification.model.ResultProcessRecognizer;
import br.edu.utfpr.tsi.xenon.domain.notification.model.TopicApplication;
import br.edu.utfpr.tsi.xenon.domain.notification.service.SendingMessageService;
import br.edu.utfpr.tsi.xenon.domain.recognize.entity.RecognizeEntity;
import br.edu.utfpr.tsi.xenon.domain.recognize.model.ResultPlate;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.domain.workstations.entity.WorkstationEntity;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.RecognizerRepository;
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
    private final RecognizerRepository recognizerRepository;
    private final WorkstationApplicationService workstationService;

    public void handleResult(List<ResultPlate> input, WorkstationEntity workstation) {
        if (Objects.nonNull(next)) {
            getNext().handleResult(input, workstation);
        }
    }

    public ResultHandler getNext() {
        return next;
    }

    protected void sending(ResultProcessRecognizer processRecognizer,
        WorkstationEntity workstationId) {
        log.info("{} resultado do processo '{}'", this, processRecognizer.getIdentifier());
        log.debug(
            "{} resultado do processo '{}' id da estação de trabalho: {}",
            this,
            processRecognizer,
            workstationId);
        var message = new MessageReceiveRecognizer(processRecognizer);

        log.debug("Criado messagem (payload: {}, result: {})",
            message.message(),
            message.resultProcessRecognizer());
        sendingMessageService.sendBeforeTransactionCommit(
            message,
            TopicApplication.RECOGNIZER.topicTo("%d/recognizer".formatted(workstationId.getId())));
    }

    protected void prepareMessageAndSend(
        CarEntity carEntity,
        ResultPlate resultPlate,
        WorkstationEntity workstation) {

        var id = saveNewRecognizer(carEntity, workstation, resultPlate.confidence());
        var result = buildProcessRecognizer(carEntity, resultPlate, workstation, id);
        sending(result, workstation);

        carEntity.setLastAccess(LocalDateTime.now());
        carRepository.saveAndFlush(carEntity);
    }

    private Long saveNewRecognizer(
        CarEntity carEntity,
        WorkstationEntity workstation,
        Float confidence) {
        var recognizeEntity = new RecognizeEntity();
        var automatic = workstation.getMode().equals("AUTOMATIC");
        recognizeEntity.setDriverName(carEntity.getUser().getName());
        recognizeEntity.setPlate(carEntity.getPlate());
        recognizeEntity.setEpochTime(LocalDateTime.now());
        recognizeEntity.setAccessGranted(automatic);
        recognizeEntity.setConfidence(confidence);
        recognizeEntity.setOriginIp(workstation.getIp());

        var id = recognizerRepository.saveAndFlush(recognizeEntity).getId();
        if (automatic) {
            workstationService.sendRequestOpen(workstation.getId());
        }

        return id;
    }

    private ResultProcessRecognizer buildProcessRecognizer(
        CarEntity carEntity,
        ResultPlate resultPlate,
        WorkstationEntity workstation,
        Long recognizeId) {
        return ResultProcessRecognizer.builder()
            .recognizerId(recognizeId)
            .authorize(carEntity.getAuthorisedAccess())
            .confidence(resultPlate.confidence())
            .driverName(carEntity.getUser().getName())
            .identifier(workstation.getMode().equals("AUTOMATIC"))
            .modelCar(carEntity.getModel())
            .plate(carEntity.getPlate())
            .userAvatar(carEntity.getUser().getAvatar())
            .build();
    }
}
