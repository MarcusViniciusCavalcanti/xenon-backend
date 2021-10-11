package br.edu.utfpr.tsi.xenon.domain.recognize.service;

import br.edu.utfpr.tsi.xenon.domain.notification.model.ResultProcessRecognizer;
import br.edu.utfpr.tsi.xenon.domain.notification.service.SendingMessageService;
import br.edu.utfpr.tsi.xenon.domain.recognize.model.ResultPlate;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import java.util.List;
import java.util.Objects;

public class NoResult extends ResultHandler {

    public NoResult(
        ResultHandler next,
        SendingMessageService sendingMessageService,
        CarRepository carRepository) {
        super(next, sendingMessageService, carRepository);
    }

    @Override
    public void handleResult(List<ResultPlate> input, Long workstation) {
        if (input.isEmpty()) {
            var result = ResultProcessRecognizer.builder()
                .confidence(99.99F)
                .identifier(Boolean.FALSE)
                .build();

            sending(result, workstation);
        }

        super.handleResult(input, workstation);
    }
}
