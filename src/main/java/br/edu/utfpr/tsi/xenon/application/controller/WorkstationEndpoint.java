package br.edu.utfpr.tsi.xenon.application.controller;

import br.edu.utfpr.tsi.xenon.application.api.WorkstationApi;
import br.edu.utfpr.tsi.xenon.application.dto.InputWorkstationDto;
import br.edu.utfpr.tsi.xenon.application.dto.WorkstationDto;
import br.edu.utfpr.tsi.xenon.application.rules.IsAdmin;
import br.edu.utfpr.tsi.xenon.application.rules.IsOperatorOrAdmin;
import br.edu.utfpr.tsi.xenon.application.service.WorkstationApplicationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/workstations")
public class WorkstationEndpoint implements WorkstationApi {

    private final WorkstationApplicationService workstationApplicationService;

    @Override
    @IsAdmin
    @PostMapping
    public ResponseEntity<WorkstationDto> createStation(
        InputWorkstationDto inputWorkstationDto,
        String authorization) {
        log.info("Recebendo solicitação para criar workstation.");
        log.debug("Recebendo solicitação para criar workstation input: {}", inputWorkstationDto);
        var workstation = workstationApplicationService.createNew(inputWorkstationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(workstation);
    }

    @Override
    @IsAdmin
    @PutMapping("/{id}")
    public ResponseEntity<WorkstationDto> updateStation(
        @PathVariable("id") Long id,
        InputWorkstationDto inputWorkstationDto,
        String authorization) {
        log.info("Recebendo solicitação para atualizar workstation id: {}", id);
        log.debug(
            "Recebendo solicitação para atualizar workstation id: {}, input: {}",
            id,
            inputWorkstationDto);
        var workstation = workstationApplicationService.update(inputWorkstationDto, id);
        return ResponseEntity.ok(workstation);
    }

    @Override
    @IsAdmin
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStation(@PathVariable("id") Long id, String authorization) {
        log.info("Recebendo requisição para deletar workstation id: {}", id);
        workstationApplicationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @IsOperatorOrAdmin
    @GetMapping
    public ResponseEntity<List<WorkstationDto>> getAll(String authorization) {
        log.info("Recebendo requisição para recupera todas as estações de trabalho");
        var workstations = workstationApplicationService.getAll();
        return ResponseEntity.ok(workstations);
    }

}

