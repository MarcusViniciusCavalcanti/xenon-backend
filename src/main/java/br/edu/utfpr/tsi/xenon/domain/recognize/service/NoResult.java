package br.edu.utfpr.tsi.xenon.domain.recognize.service;

import br.edu.utfpr.tsi.xenon.application.service.WorkstationApplicationService;
import br.edu.utfpr.tsi.xenon.domain.notification.model.ResultProcessRecognizer;
import br.edu.utfpr.tsi.xenon.domain.notification.service.SendingMessageService;
import br.edu.utfpr.tsi.xenon.domain.recognize.model.ResultPlate;
import br.edu.utfpr.tsi.xenon.domain.workstations.entity.WorkstationEntity;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.RecognizerRepository;
import java.util.List;

public class NoResult extends ResultHandler {

    public NoResult(
        ResultHandler next,
        SendingMessageService sendingMessageService,
        CarRepository carRepository,
        RecognizerRepository recognizerRepository,
        WorkstationApplicationService workstationService) {
        super(next, sendingMessageService, carRepository, recognizerRepository, workstationService);
    }

    @Override
    public void handleResult(List<ResultPlate> input, WorkstationEntity workstation) {
        if (input.isEmpty()) {
            var result = ResultProcessRecognizer.builder()
                .confidence(99.99F)
                .identifier(Boolean.FALSE)
                .authorize(Boolean.FALSE)
                .userAvatar("https://res.cloudinary.com/xenonapp/image/upload/v1643555043/avatar/55168-200_pttasq.png")
                .build();

            sending(result, workstation);
        }

        super.handleResult(input, workstation);
    }
}
