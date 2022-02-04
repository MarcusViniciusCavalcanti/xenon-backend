package br.edu.utfpr.tsi.xenon.domain.recognize.service;

import br.edu.utfpr.tsi.xenon.application.dto.PlatesDto;
import br.edu.utfpr.tsi.xenon.application.service.WorkstationApplicationService;
import br.edu.utfpr.tsi.xenon.domain.notification.service.SendingMessageService;
import br.edu.utfpr.tsi.xenon.domain.recognize.model.ResultPlate;
import br.edu.utfpr.tsi.xenon.domain.workstations.entity.WorkstationEntity;
import br.edu.utfpr.tsi.xenon.domain.workstations.service.WorkstationService;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.RecognizerRepository;
import java.util.List;
import java.util.Map;

public final class ExecutorResult {

    private final CarRepository carRepository;
    private final ResultHandler resultHandler;

    public ExecutorResult(
        CarRepository carRepository,
        SendingMessageService messageService,
        RecognizerRepository recognizerRepository,
        WorkstationApplicationService workstationService) {
        this.carRepository = carRepository;

        var noResult =
            new NoResult(null, messageService, carRepository, recognizerRepository, workstationService);
        var oneResult =
            new OneResult(noResult, messageService, carRepository, recognizerRepository, workstationService);
        resultHandler =
            new MultipleResult(oneResult, messageService, carRepository, recognizerRepository, workstationService);
    }

    public void processResult(
        Map<String, List<PlatesDto>> recognizerMajorConfidences,
        WorkstationEntity workstation) {

        var resultPlates = getResultPlatesCarsInRepository(recognizerMajorConfidences);
        resultHandler.handleResult(resultPlates, workstation);
    }

    private List<ResultPlate> getResultPlatesCarsInRepository(
        Map<String, List<PlatesDto>> recognizerMajorConfidences) {
        var plates = recognizerMajorConfidences.keySet().stream().toList();
        return carRepository.findAllByPlateIn(plates).stream()
            .map(carEntity -> {
                var recognize = recognizerMajorConfidences.get(carEntity.getPlate()).get(0);
                return new ResultPlate(carEntity, recognize.getConfidence());
            })
            .toList();
    }
}
