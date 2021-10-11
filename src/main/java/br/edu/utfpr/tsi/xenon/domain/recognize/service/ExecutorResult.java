package br.edu.utfpr.tsi.xenon.domain.recognize.service;

import br.edu.utfpr.tsi.xenon.application.dto.PlatesDto;
import br.edu.utfpr.tsi.xenon.domain.notification.service.SendingMessageService;
import br.edu.utfpr.tsi.xenon.domain.recognize.model.ResultPlate;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import java.util.List;
import java.util.Map;

public final class ExecutorResult {

    private final CarRepository carRepository;
    private final ResultHandler resultHandler;

    public ExecutorResult(CarRepository carRepository, SendingMessageService messageService) {
        this.carRepository = carRepository;

        var noResult = new NoResult(null, messageService, carRepository);
        var oneResult = new OneResult(noResult, messageService, carRepository);
        this.resultHandler = new MultipleResult(oneResult, messageService, carRepository);
    }

    public void processResult(
        Map<String, List<PlatesDto>> recognizerMajorConfidences, Long workstationId) {

        var resultPlates = getResultPlatesCarsInRepository(recognizerMajorConfidences);
        resultHandler.handleResult(resultPlates, workstationId);
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
