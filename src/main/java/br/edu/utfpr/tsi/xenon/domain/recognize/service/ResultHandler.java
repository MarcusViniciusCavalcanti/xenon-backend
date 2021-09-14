package br.edu.utfpr.tsi.xenon.domain.recognize.service;

import br.edu.utfpr.tsi.xenon.application.dto.InputRecognizerDto;
import br.edu.utfpr.tsi.xenon.domain.notification.model.MessageReceiveRecognizer;
import br.edu.utfpr.tsi.xenon.domain.notification.model.ResultProcessRecognizer;
import br.edu.utfpr.tsi.xenon.domain.notification.model.TopicApplication;
import br.edu.utfpr.tsi.xenon.domain.notification.service.SendingMessageService;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class ResultHandler {
    private final ResultHandler next;

    private final SendingMessageService sendingMessageService;

    public void handleResult(List<InputRecognizerDto> input) {
        if (Objects.nonNull(next)) {
            getNext().handleResult(input);
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
            TopicApplication.RECOGNIZER.topicTo("/%d/recognizer".formatted(workstationId)));
    }
}
