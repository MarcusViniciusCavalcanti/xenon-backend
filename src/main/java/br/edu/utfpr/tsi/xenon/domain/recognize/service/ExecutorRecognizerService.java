package br.edu.utfpr.tsi.xenon.domain.recognize.service;

import br.edu.utfpr.tsi.xenon.application.dto.InputRecognizerDto;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExecutorRecognizerService implements Consumer<InputRecognizerDto> {



    @Override
    public void accept(InputRecognizerDto input) {
        var plates = input.getRecognizers().stream()
            .filter(plate -> plate.getConfidence() >= 75.00)
            .toList();


    }
}
