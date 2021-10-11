package br.edu.utfpr.tsi.xenon.application.service;

import br.edu.utfpr.tsi.xenon.application.dto.InputRecognizerDto;
import br.edu.utfpr.tsi.xenon.domain.recognize.service.ExecutorRecognizerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecognizeServiceApplication {

    private final ExecutorRecognizerService executorRecognizerService;

    public void receive(InputRecognizerDto input, String key, String ip) {
        new Thread(() -> executorRecognizerService.accept(input, key, ip)).start();
    }
}
