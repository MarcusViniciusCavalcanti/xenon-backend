package br.edu.utfpr.tsi.xenon.domain.recognize.service;

import br.edu.utfpr.tsi.xenon.application.dto.InputRecognizerDto;
import br.edu.utfpr.tsi.xenon.application.dto.PlatesDto;
import br.edu.utfpr.tsi.xenon.application.service.WorkstationApplicationService;
import br.edu.utfpr.tsi.xenon.domain.notification.service.SendingMessageService;
import br.edu.utfpr.tsi.xenon.domain.workstations.entity.WorkstationEntity;
import br.edu.utfpr.tsi.xenon.domain.workstations.service.WorkstationService;
import br.edu.utfpr.tsi.xenon.structure.exception.ErrorRecognizeConfidenceIsLow;
import br.edu.utfpr.tsi.xenon.structure.exception.ErrorRecognizeOriginIpNotAllowed;
import br.edu.utfpr.tsi.xenon.structure.exception.ErrorRecognizeWorkstationNotFoundException;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.RecognizerRepository;
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
    private final WorkstationApplicationService workstationApplicationService;
    private final CarRepository carRepository;
    private final SendingMessageService sendingMessageService;
    private final ErrorRecognizeService errorRecognizeService;
    private final RecognizerRepository recognizerRepository;

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
            var executorResult = new ExecutorResult(
                carRepository,
                sendingMessageService,
                recognizerRepository,
                workstationApplicationService);
            executorResult.processResult(recognizerMajorConfidences, workstation);
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

    private void checkOrigin(
        String ip,
        WorkstationEntity workstationEntity,
        InputRecognizerDto input) {
        var ipWorkstation = workstationEntity.getIp();
        var normalizeIp = workstationService.formatterIp(ip);

        log.info("Verificando origem do ip");
        log.info(
            "Avaliando ip da requisição {} com o ip da estação {}",
            normalizeIp,
            ipWorkstation
        );

        if (!normalizeIp.equalsIgnoreCase(ipWorkstation)) {
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
