package br.edu.utfpr.tsi.xenon.application.service;

import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.IP_WORKSTATION_EXIST;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.NAME_WORKSTATION_EXIST;

import br.edu.utfpr.tsi.xenon.application.dto.InputWorkstationDto;
import br.edu.utfpr.tsi.xenon.application.dto.WorkstationDto;
import br.edu.utfpr.tsi.xenon.application.dto.WorkstationDto.ModeEnum;
import br.edu.utfpr.tsi.xenon.domain.workstations.entity.WorkstationEntity;
import br.edu.utfpr.tsi.xenon.domain.workstations.service.WorkstationService;
import br.edu.utfpr.tsi.xenon.structure.exception.ResourceNotFoundException;
import br.edu.utfpr.tsi.xenon.structure.exception.WorkStationException;
import br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkstationApplicationService {

    private final WorkstationRepository workstationRepository;
    private final WorkstationService workstationService;

    @Transactional
    public WorkstationDto createNew(InputWorkstationDto input) {
        log.info("Iniciando processo de criação de workstation.");
        log.debug("Iniciando processo de criação de workstation. input: {}", input);
        var workstation = workstationService.create(
            input.getIp(),
            input.getName(),
            input.getMode().name());

        validateData(input.getIp(), input.getName());

        workstationRepository.saveAndFlush(workstation);
        return buildDto(workstation);
    }

    @Transactional
    public WorkstationDto update(InputWorkstationDto input, Long id) {
        log.info("Iniciando processo de atualização de workstation de id: {}", id);
        log.info("Iniciando processo de atualização de workstation de id: {}, input:{}", id, input);
        validateData(input.getIp(), input.getName());
        var workstation = getById(id);
        var workstationUpdated = workstationService.replaceData(
            workstation,
            input.getName(),
            input.getMode().name(),
            input.getIp());


        workstationRepository.saveAndFlush(workstationUpdated);
        return buildDto(workstationUpdated);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Iniciando processo deletar um workstation de id: {}", id);
        var workstation = getById(id);
        workstationRepository.delete(workstation);
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
            .ip(workstation.getIp());
    }

    private void checkName(String name) {
        log.info("Verificando se nome existe.");
        var isExistName = workstationRepository.existsByName(name);
        if (Boolean.TRUE.equals(isExistName)) {
            throw new WorkStationException(NAME_WORKSTATION_EXIST.getCode(), name);
        }
    }

    private void checkIp(String ip) {
        log.info("Verificando se ip existe.");
        var isExistIp = workstationRepository.existsByIp(ip);
        if (Boolean.TRUE.equals(isExistIp)) {
            throw new WorkStationException(IP_WORKSTATION_EXIST.getCode(), ip);
        }
    }
}
