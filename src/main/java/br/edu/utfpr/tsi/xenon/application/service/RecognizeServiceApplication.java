package br.edu.utfpr.tsi.xenon.application.service;

import br.edu.utfpr.tsi.xenon.application.config.bean.SpecificationConfiguration;
import br.edu.utfpr.tsi.xenon.application.dto.ErrorRecognizerDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputRecognizerDto;
import br.edu.utfpr.tsi.xenon.application.dto.PageRecognizerDto;
import br.edu.utfpr.tsi.xenon.application.dto.PageUserCarAccessDto;
import br.edu.utfpr.tsi.xenon.application.dto.RecognizerDto;
import br.edu.utfpr.tsi.xenon.application.dto.RecognizerSummaryDto;
import br.edu.utfpr.tsi.xenon.application.dto.UserCarAccessDto;
import br.edu.utfpr.tsi.xenon.domain.recognize.entity.RecognizeEntity;
import br.edu.utfpr.tsi.xenon.domain.recognize.service.ExecutorRecognizerService;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.structure.ParamsQuerySearchRecognizeDto;
import br.edu.utfpr.tsi.xenon.structure.exception.ResourceNotFoundException;
import br.edu.utfpr.tsi.xenon.structure.repository.BasicSpecification;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.ErrorRecognizerRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.RecognizerRepository;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class RecognizeServiceApplication {

    private final ExecutorRecognizerService executorRecognizerService;
    private final BasicSpecification<RecognizeEntity, ParamsQuerySearchRecognizeDto> specification;
    private final RecognizerRepository recognizerRepository;
    private final ErrorRecognizerRepository errorRecognizerRepository;
    private final CarRepository carRepository;

    public RecognizeServiceApplication(
        ExecutorRecognizerService executorRecognizerService,
        @Qualifier(SpecificationConfiguration.QUALIFIER_GET_ALL_RECOGNIZE)
            BasicSpecification<RecognizeEntity, ParamsQuerySearchRecognizeDto> specification,
        RecognizerRepository recognizerRepository,
        ErrorRecognizerRepository errorRecognizerRepository,
        CarRepository carRepository) {
        this.executorRecognizerService = executorRecognizerService;
        this.specification = specification;
        this.recognizerRepository = recognizerRepository;
        this.errorRecognizerRepository = errorRecognizerRepository;
        this.carRepository = carRepository;
    }

    public void receive(InputRecognizerDto input, String key, String ip) {
        log.info("Executando o processo para receber reconhecimento.");
        new Thread(() -> executorRecognizerService.accept(input, key, ip)).start();
    }

    public PageRecognizerDto getAll(ParamsQuerySearchRecognizeDto params) {
        log.info("Executando processo para buscar reconhecimentos.");
        var filter = specification.filterBy(params);
        var sort = Sort.by(
            Sort.Direction.fromString(params.getDirection().name()), params.getSorted().getValue());
        var pageable = PageRequest.of(params.getPage(), params.getSize(), sort);

        log.debug("Enviando busca com os seguintes paramentros: {}", pageable);
        var page = recognizerRepository.findAll(filter, pageable);

        log.debug("Encontrado {} reconhecimentos", page.getSize());
        var result = page
            .getContent()
            .stream()
            .map(recognizeEntity -> new RecognizerDto()
                .accessGranted(recognizeEntity.getAccessGranted())
                .confidence(recognizeEntity.getConfidence())
                .driverName(recognizeEntity.getDriverName())
                .epochTime(recognizeEntity.getEpochTime())
                .errorDetails(
                    Objects.nonNull(recognizeEntity.getErrorRecognizer())
                        ? recognizeEntity.getErrorRecognizer().getId() : null)
                .id(recognizeEntity.getId()))
            .toList();

        log.info("Preparando resposta.");
        log.debug("Preparando DTO para {}", result);
        var pageRecognizer = new PageRecognizerDto().items(result);
        pageRecognizer.direction(params.getDirection().name());
        pageRecognizer.sorted(params.getSorted().name());
        pageRecognizer.size(page.getSize());
        pageRecognizer.page(page.getNumber());
        pageRecognizer.totalElements(page.getTotalElements());
        pageRecognizer.setTotalPage(page.getTotalPages());

        return pageRecognizer;
    }

    @Transactional
    public ErrorRecognizerDto getErroById(Long id) {
        log.info("Recuperando erros do reconhecimento do ID {}", id);
        return errorRecognizerRepository.findErrorRecognizerEntityByRecognizeId(id)
            .map(errorRecognizer -> new ErrorRecognizerDto()
                .errorMessage(errorRecognizer.getErrorMessage())
                .date(errorRecognizer.getDate())
                .originIp(errorRecognizer.getOriginIp())
                .workstationName(errorRecognizer.getWorkstationName())
                .trace(errorRecognizer.getTrace()))
            .orElseThrow(() -> new ResourceNotFoundException("Error", "id"));
    }

    @Transactional
    public PageUserCarAccessDto getAllAccessCarUser(Long id, Integer size, Integer page) {
        log.info("Recuperando acessos do usuário  de id: {}", id);
        var plates = carRepository.findByUserId(id).stream()
            .map(CarEntity::getPlate)
            .toList();

        log.debug("Recuperando acessos do carros com as seguintes placas {}", plates);
        var sort = Sort.by(Direction.DESC, "createdAt");
        var pageRequest = PageRequest.of(page, size, sort);

        var pageable = recognizerRepository.findByAccessGrantedTrueAndPlateIn(plates, pageRequest);

        log.debug("Encontrado {} de reconhecimentos", pageable.getSize());
        var content = pageable.getContent().stream()
            .map(recognizeEntity -> new UserCarAccessDto()
                .carPlate(recognizeEntity.getPlate())
                .epochTime(recognizeEntity.getEpochTime())
                .confidence(recognizeEntity.getConfidence())
                .grandAccess(recognizeEntity.getAccessGranted()))
            .toList();

        log.debug("Montando resposta");
        var pageUserCarAccessDto = new PageUserCarAccessDto().items(content);
        pageUserCarAccessDto.setAmountCars(plates.size());
        pageUserCarAccessDto.direction(Direction.DESC.name());
        pageUserCarAccessDto.sorted("createdAt");
        pageUserCarAccessDto.size(pageable.getSize());
        pageUserCarAccessDto.page(pageable.getNumber());
        pageUserCarAccessDto.totalElements(pageable.getTotalElements());
        pageUserCarAccessDto.setTotalPage(pageable.getTotalPages());

        return pageUserCarAccessDto;
    }

    @Cacheable(cacheNames = "RecognizerWeekSummary")
    public RecognizerSummaryDto getRecognizerSummary() {
        log.info("Processando busca para recuperar sumário de reconhecimento por semana");
        var summary = recognizerRepository.getRecognizerSummaryWeek();

        log.debug("enviado o sumário criado dos reconhecimentos por semana. {}", summary);
        return new RecognizerSummaryDto()
            .seventhDayBefore(summary.getSevenDay())
            .sixthDayBefore(summary.getSixDay())
            .fifthDayBefore(summary.getFiveDay())
            .fourthDayBefore(summary.getFourDay())
            .thirdhDayBefore(summary.getThreeDay())
            .secondDayBefore(summary.getTwoDay())
            .firstDayBefore(summary.getOneDay())
            .now(summary.getNow());
    }
}
