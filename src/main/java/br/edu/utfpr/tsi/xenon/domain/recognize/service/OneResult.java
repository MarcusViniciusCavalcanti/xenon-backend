package br.edu.utfpr.tsi.xenon.domain.recognize.service;

import br.edu.utfpr.tsi.xenon.application.service.WorkstationApplicationService;
import br.edu.utfpr.tsi.xenon.domain.notification.service.SendingMessageService;
import br.edu.utfpr.tsi.xenon.domain.recognize.model.ResultPlate;
import br.edu.utfpr.tsi.xenon.domain.workstations.entity.WorkstationEntity;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.RecognizerRepository;
import java.util.List;

public class OneResult extends ResultHandler {

    public OneResult(
        ResultHandler next,
        SendingMessageService sendingMessageService,
        CarRepository carRepository,
        RecognizerRepository recognizerRepository,
        WorkstationApplicationService workstationService) {
        super(next, sendingMessageService, carRepository, recognizerRepository, workstationService);
    }

    @Override
    public void handleResult(List<ResultPlate> input, WorkstationEntity workstation) {
        if (input.size() == 1) {
            var resultPlate = input.get(0);
            prepareMessageAndSend(resultPlate.carEntity(), resultPlate, workstation);
        }

        super.handleResult(input, workstation);
    }
}
