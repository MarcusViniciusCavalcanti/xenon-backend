package br.edu.utfpr.tsi.xenon.application.controller;

import br.edu.utfpr.tsi.xenon.application.api.RecognizerApi;
import br.edu.utfpr.tsi.xenon.application.dto.InputRecognizerDto;
import br.edu.utfpr.tsi.xenon.application.service.RecognizeServiceApplication;
import java.util.Optional;
import javax.servlet.ServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@RestController
@RequestMapping("/receive-recognizer")
@RequiredArgsConstructor
public class RecognizeEndpoint implements RecognizerApi {

    private final RecognizeServiceApplication recognizeServiceApplication;

    @Override
    @PostMapping("/{key}")
    public ResponseEntity<Void> receiveRecognizer(@PathVariable("key") String key,
        InputRecognizerDto inputRecognizerDto) {
        log.info("Recebendo {} reconhecimentos", inputRecognizerDto.getRecognizers().size());
        log.debug("Input: {}", inputRecognizerDto);

        var ip = getIpRequest();
        recognizeServiceApplication.receive(inputRecognizerDto, key, ip);
        return ResponseEntity.noContent().build();
    }

    public String getIpRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
            .filter(ServletRequestAttributes.class::isInstance)
            .map(ServletRequestAttributes.class::cast)
            .map(ServletRequestAttributes::getRequest)
            .map(ServletRequest::getRemoteAddr)
            .orElse("");
    }
}
