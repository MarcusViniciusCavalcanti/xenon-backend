package br.edu.utfpr.tsi.xenon.domain.recognize.service;

import br.edu.utfpr.tsi.xenon.application.dto.InputRecognizerDto;
import br.edu.utfpr.tsi.xenon.application.dto.PlatesDto;
import br.edu.utfpr.tsi.xenon.domain.notification.service.SendingMessageService;
import br.edu.utfpr.tsi.xenon.domain.workstations.entity.WorkstationEntity;
import br.edu.utfpr.tsi.xenon.domain.workstations.service.WorkstationService;
import br.edu.utfpr.tsi.xenon.structure.exception.ErrorRecognizeConfidenceIsLow;
import br.edu.utfpr.tsi.xenon.structure.exception.ErrorRecognizeOriginIpNotAllowed;
import br.edu.utfpr.tsi.xenon.structure.exception.ErrorRecognizeWorkstationNotFoundException;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExecutorRecognizerService {

    private static final float LIMIT_CONFIDENCE_VALID = 75.0F;

    private final WorkstationRepository workstationRepository;
    private final WorkstationService workstationService;
    private final CarRepository carRepository;
    private final SendingMessageService sendingMessageService;
    private final ErrorRecognizeService errorRecognizeService;

    @Transactional
    public void accept(InputRecognizerDto input, String key, String ip) {
        log.info("Iniciando verificação das placas");
        log.debug("Placas: {}", input.getRecognizers());
        try {
            var workstation = findByKey(key, input);

            checkOrigin(ip, workstation, input);
            checkPayload(input, workstation);

            var recognizerMajorConfidences = input.getRecognizers().stream()
                .collect(Collectors.groupingBy(PlatesDto::getPlate));

            log.debug(
                "Iniciando executor com {} resultados encontrados de carros cadastrados",
                recognizerMajorConfidences.size());
            new ExecutorResult(carRepository, sendingMessageService)
                .processResult(recognizerMajorConfidences, workstation.getId());
        } catch (Exception exception) {
            log.error(
                "Error ao processar reconhecimento [{}] verifique os logs para mais detalhes",
                exception.getClass().getName());
            errorRecognizeService.insertError(exception, ip, input);
        }
    }

    private WorkstationEntity findByKey(String key, InputRecognizerDto input) {
        log.debug("Recuperando estação de trabalho por key");
        var workstation = workstationRepository.findIdByKey(key);

        if (Objects.isNull(workstation)) {
            throw new ErrorRecognizeWorkstationNotFoundException(key, input);
        }

        return workstation;
    }

    private void checkOrigin(String ip, WorkstationEntity workstationEntity,
        InputRecognizerDto input) {
        log.debug("Verificando origem do ip");
        var normalizeIp = workstationService.formatterIp(ip);

        if (!normalizeIp.equalsIgnoreCase(workstationEntity.getIp())) {
            throw new ErrorRecognizeOriginIpNotAllowed(
                normalizeIp,
                workstationEntity.getName(),
                input);
        }
    }

    private void checkPayload(InputRecognizerDto input, WorkstationEntity workstationEntity) {
        log.debug("Filtrando placas com confiabilidade maior que {}", LIMIT_CONFIDENCE_VALID);
        var recognizerMajorConfidences = input.getRecognizers().stream()
            .filter(platesDto -> platesDto.getConfidence() >= LIMIT_CONFIDENCE_VALID)
            .toList();

        if (recognizerMajorConfidences.isEmpty()) {
            throw new ErrorRecognizeConfidenceIsLow(workstationEntity.getName(), input);
        }
    }
}
