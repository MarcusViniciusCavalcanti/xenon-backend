package br.edu.utfpr.tsi.xenon.application.service;

import static br.edu.utfpr.tsi.xenon.domain.notification.model.TopicApplication.CHANGE_WORKSTATION;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.IP_WORKSTATION_EXIST;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.NAME_WORKSTATION_EXIST;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import br.edu.utfpr.tsi.xenon.application.dto.InputWorkstationDto;
import br.edu.utfpr.tsi.xenon.application.dto.WorkstationDto;
import br.edu.utfpr.tsi.xenon.application.dto.WorkstationDto.ModeEnum;
import br.edu.utfpr.tsi.xenon.application.dto.WorkstationSummaryDto;
import br.edu.utfpr.tsi.xenon.domain.notification.model.ActionChangeWorkstation;
import br.edu.utfpr.tsi.xenon.domain.notification.model.ActionChangeWorkstation.ActionType;
import br.edu.utfpr.tsi.xenon.domain.notification.model.UpdateWorkstationMessage;
import br.edu.utfpr.tsi.xenon.domain.notification.service.SendingMessageService;
import br.edu.utfpr.tsi.xenon.domain.workstations.entity.WorkstationEntity;
import br.edu.utfpr.tsi.xenon.domain.workstations.service.WorkstationService;
import br.edu.utfpr.tsi.xenon.structure.exception.ResourceNotFoundException;
import br.edu.utfpr.tsi.xenon.structure.exception.WorkStationException;
import br.edu.utfpr.tsi.xenon.structure.repository.RecognizerRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkstationApplicationService {

    private static final String BLANK_REQUEST = "{}";

    private final WorkstationRepository workstationRepository;
    private final WorkstationService workstationService;
    private final SendingMessageService senderMessageWebSocketService;
    private final RecognizerRepository recognizerRepository;
    private final RestTemplate restTemplate;

    @Transactional
    @CacheEvict(cacheNames = {"Workstations", "Workstation", "WorkstationDto"}, allEntries = true)
    public WorkstationDto createNew(InputWorkstationDto input) {
        log.info("Iniciando processo de criação de workstation.");
        log.debug("Iniciando processo de criação de workstation. input: {}", input);
        validateData(workstationService.formatterIp(input.getIp()), input.getName());
        var workstation = workstationService.create(
            input.getIp(),
            input.getName(),
            input.getMode().name(),
            input.getPort());
        workstationRepository.saveAndFlush(workstation);
        return buildDto(workstation);
    }

    @Transactional
    @CacheEvict(cacheNames = {"Workstations", "Workstation", "WorkstationDto"}, allEntries = true)
    public WorkstationDto update(InputWorkstationDto input, Long id) {
        log.info("Iniciando processo de atualização de workstation de id: {}", id);
        log.info("Iniciando processo de atualização de workstation de id: {}, input:{}", id, input);

        var workstation = getById(id);
        var formatterIp = workstationService.formatterIp(input.getIp());
        if (FALSE.equals(workstation.getName().equals(input.getName()))) {
            checkName(input.getName());
        }

        if (FALSE.equals(workstation.getIp().equals(formatterIp))) {
            checkIp(formatterIp);
        }

        var workstationUpdated = workstationService.replaceData(
            workstation,
            input.getName(),
            input.getMode().name(),
            input.getIp(),
            input.getPort());

        workstationRepository.saveAndFlush(workstationUpdated);

        var workstationDto = buildDto(workstationUpdated);

        log.debug("montando ActionChangeWorkstation");
        var actionChangeUpdate = ActionChangeWorkstation.builder()
            .type(ActionType.UPDATE)
            .workstation(workstationDto)
            .build();

        senderMessageWebSocketService.sendBeforeTransactionCommit(
            new UpdateWorkstationMessage(actionChangeUpdate),
            CHANGE_WORKSTATION.topicTo(workstationDto.getId().toString()));

        return workstationDto;
    }

    @Transactional
    @CacheEvict(cacheNames = {"Workstations", "Workstation", "WorkstationDto"}, allEntries = true)
    public void delete(Long id) {
        log.info("Iniciando processo para remover um workstation de id: {}", id);
        log.debug("montando ActionChangeWorkstation");
        var actionChangeDelete = ActionChangeWorkstation.builder()
            .type(ActionType.DELETE)
            .build();

        var workstation = getById(id);
        var messageRequest = new UpdateWorkstationMessage(actionChangeDelete);
        log.debug("marcando envio de message com o tipo: {}, pos transação",
            messageRequest.actionChangeWorkstation());
        senderMessageWebSocketService.sendBeforeTransactionCommit(
            messageRequest,
            CHANGE_WORKSTATION.topicTo(id.toString()));

        workstationRepository.delete(workstation);
    }

    @Transactional
    @Cacheable(cacheNames = "Workstations", unless = "#result.isEmpty()")
    public List<WorkstationDto> getAll() {
        return workstationRepository.findAll().stream()
            .map(this::buildDto)
            .toList();
    }

    @Cacheable(cacheNames = "WorkstationDto", key = "#id")
    public WorkstationDto getWorkstationById(Long id) {
        return buildDto(getById(id));
    }

    @Cacheable(cacheNames = "WorkstationsSummary", unless = "#result.isEmpty()")
    public List<WorkstationSummaryDto> getWorkstationSummary() {
        log.info("Executando a busca do sumário da estação de trabalho e reconhecimentos.");
        return workstationRepository.getWorkstationSummary().stream()
            .map(workstationAndRecognizeSummary -> new WorkstationSummaryDto()
                .id(workstationAndRecognizeSummary.getId())
                .amountAccess(workstationAndRecognizeSummary.getRecognizers())
                .workstationName(workstationAndRecognizeSummary.getName()))
            .toList();
    }

    public void approvedAccess(Long workstationId, Long recognizeId) {
        recognizerRepository.findById(recognizeId)
            .ifPresentOrElse(recognizeEntity -> {
                recognizerRepository.updateAccessAuthorized(recognizeId);
                sendRequestOpen(workstationId);
            }, () -> {
                throw new ResourceNotFoundException("reconhecimento", "id");
            });
    }

    public void sendRequestOpen(Long workstationId) {
        log.info("Executando envio de requisição para estação de trabalho abrir cancela");
        var workstationEntity = getById(workstationId);
        var ip = workstationEntity.getIp();
        var port = workstationEntity.getPort();
        try {
            var url = workstationService.buildUriOpen(ip, port);
            var response = restTemplate.postForEntity(url, BLANK_REQUEST, Void.class);

            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                log.info("requisição enviada com sucesso");
            } else {
                log.warn("Requisição não foi enviada: {}", response);
            }
        } catch (RestClientException exception) {
            log.error("Não foi possível enviar requisição: {}", exception.getMessage());
        }
    }

    void validateData(String ip, String name) {
        checkName(name);
        checkIp(ip);
    }

    private WorkstationEntity getById(Long id) {
        return workstationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("estação de trabalho", "id"));
    }

    private WorkstationDto buildDto(WorkstationEntity workstation) {
        return new WorkstationDto()
            .id(workstation.getId())
            .name(workstation.getName())
            .mode(ModeEnum.fromValue(workstation.getMode()))
            .ip(workstation.getIp())
            .key(workstation.getKey())
            .port(workstation.getPort());
    }

    private void checkName(String name) {
        log.info("Verificando se nome existe.");
        var isExistName = workstationRepository.existsByName(name);
        if (TRUE.equals(isExistName)) {
            throw new WorkStationException(NAME_WORKSTATION_EXIST.getCode(), name);
        }
    }

    private void checkIp(String ip) {
        log.info("Verificando se ip existe.");
        var isExistIp = workstationRepository.existsByIp(ip);
        if (TRUE.equals(isExistIp)) {
            throw new WorkStationException(IP_WORKSTATION_EXIST.getCode(), ip);
        }
    }
}
