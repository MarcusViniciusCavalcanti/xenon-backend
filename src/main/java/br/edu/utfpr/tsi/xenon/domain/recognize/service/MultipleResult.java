package br.edu.utfpr.tsi.xenon.domain.recognize.service;

import br.edu.utfpr.tsi.xenon.application.service.WorkstationApplicationService;
import br.edu.utfpr.tsi.xenon.domain.notification.service.SendingMessageService;
import br.edu.utfpr.tsi.xenon.domain.recognize.model.ResultPlate;
import br.edu.utfpr.tsi.xenon.domain.workstations.entity.WorkstationEntity;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.RecognizerRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class MultipleResult extends ResultHandler {

    public MultipleResult(
        ResultHandler next,
        SendingMessageService sendingMessageService,
        CarRepository carRepository,
        RecognizerRepository recognizerRepository,
        WorkstationApplicationService workstationService) {
        super(next, sendingMessageService, carRepository, recognizerRepository, workstationService);
    }

    @Override
    public void handleResult(List<ResultPlate> input, WorkstationEntity workstation) {
        if (input.size() > 1) {
            var result = input.stream()
                .filter(resultPlate -> {
                    var lastAccess = resultPlate.carEntity().getLastAccess();
                    var currentTimeMinusTenMinute = LocalDateTime.now().minusMinutes(10);

                    if (Objects.isNull(lastAccess)) {
                        return Boolean.TRUE;
                    }

                    return lastAccess.isBefore(currentTimeMinusTenMinute);
                })
                .max(Comparator.comparing(ResultPlate::confidence))
                .orElseGet(() -> input.stream()
                    .max(Comparator.comparing(ResultPlate::confidence))
                    .stream()
                    .toList()
                    .get(0));

            prepareMessageAndSend(result.carEntity(), result, workstation);
        }

        super.handleResult(input, workstation);
    }
}
