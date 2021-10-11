package br.edu.utfpr.tsi.xenon.domain.recognize.service;

import br.edu.utfpr.tsi.xenon.domain.notification.service.SendingMessageService;
import br.edu.utfpr.tsi.xenon.domain.recognize.model.ResultPlate;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import java.util.List;

public class OneResult extends ResultHandler {

    public OneResult(
        ResultHandler next,
        SendingMessageService sendingMessageService,
        CarRepository carRepository) {
        super(next, sendingMessageService, carRepository);
    }

    @Override
    public void handleResult(List<ResultPlate> input, Long workstation) {
        if (input.size() == 1) {
            var resultPlate = input.get(0);
            prepareMessageAndSend(resultPlate.carEntity(), resultPlate, workstation);
        }

        super.handleResult(input, workstation);
    }
}
