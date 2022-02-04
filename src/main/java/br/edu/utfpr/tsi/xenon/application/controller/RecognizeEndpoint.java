package br.edu.utfpr.tsi.xenon.application.controller;

import br.edu.utfpr.tsi.xenon.application.api.RecognizerApi;
import br.edu.utfpr.tsi.xenon.application.dto.ErrorRecognizerDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputRecognizerDto;
import br.edu.utfpr.tsi.xenon.application.dto.PageRecognizerDto;
import br.edu.utfpr.tsi.xenon.application.rules.IsAdmin;
import br.edu.utfpr.tsi.xenon.application.service.RecognizeServiceApplication;
import br.edu.utfpr.tsi.xenon.structure.DirectionEnum;
import br.edu.utfpr.tsi.xenon.structure.ParamsQuerySearchRecognizeDto;
import br.edu.utfpr.tsi.xenon.structure.ParamsQuerySearchRecognizeDto.SortedRecognizePropertyEnum;
import java.util.Optional;
import javax.servlet.ServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RecognizeEndpoint implements RecognizerApi {

    private final RecognizeServiceApplication recognizeServiceApplication;

    @Override
    @PostMapping("/receive-recognizer/{key}")
    public ResponseEntity<Void> receiveRecognizer(@PathVariable("key") String key,
        InputRecognizerDto inputRecognizerDto) {
        log.info("Recebendo {} reconhecimentos", inputRecognizerDto.getRecognizers().size());
        log.debug("Input: {}", inputRecognizerDto);

        if (Boolean.FALSE.equals(inputRecognizerDto.getRecognizers().isEmpty())) {
            var ip = getIpRequest();
            recognizeServiceApplication.receive(inputRecognizerDto, key, ip);
        }
        return ResponseEntity.noContent().build();
    }

    @Override
    @IsAdmin
    @GetMapping("/all-recognizer")
    public ResponseEntity<PageRecognizerDto> getAllRecognizers(
        String authorization,
        Integer size,
        Integer page,
        String sorted,
        String direction,
        String driverName,
        Boolean onlyError) {
        log.info("Recebendo requisição para recuperar todos os reconhecimentos.");

        var directionEnum = DirectionEnum.fromValue(direction);
        var sortedEnum = SortedRecognizePropertyEnum.fromValue(sorted);
        var params = ParamsQuerySearchRecognizeDto.builder()
            .direction(directionEnum)
            .sorted(sortedEnum)
            .page(page)
            .size(size)
            .driverName(driverName)
            .onlyError(onlyError)
            .build();

        var pageRecognizer = recognizeServiceApplication.getAll(params);
        return ResponseEntity.ok(pageRecognizer);
    }

    @Override
    @IsAdmin
    @GetMapping("/error-recognizer/{id}")
    public ResponseEntity<ErrorRecognizerDto> errorRecognizer(
        @PathVariable("id") Long id,
        String authorization) {
        log.info("Recebendo requisição para recuperar erro de reconhecimento.");
        var erroById = recognizeServiceApplication.getErroById(id);
        return ResponseEntity.ok(erroById);
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
