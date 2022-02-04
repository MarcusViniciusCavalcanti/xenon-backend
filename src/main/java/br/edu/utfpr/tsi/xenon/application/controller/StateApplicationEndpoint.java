package br.edu.utfpr.tsi.xenon.application.controller;

import br.edu.utfpr.tsi.xenon.application.api.StateSystemApi;
import br.edu.utfpr.tsi.xenon.application.dto.RecognizerSummaryDto;
import br.edu.utfpr.tsi.xenon.application.dto.UserCarsSummaryDto;
import br.edu.utfpr.tsi.xenon.application.dto.UsersRegistrySummaryDto;
import br.edu.utfpr.tsi.xenon.application.dto.WorkstationSummaryDto;
import br.edu.utfpr.tsi.xenon.application.rules.IsAdmin;
import br.edu.utfpr.tsi.xenon.application.service.CarApplicationService;
import br.edu.utfpr.tsi.xenon.application.service.RecognizeServiceApplication;
import br.edu.utfpr.tsi.xenon.application.service.UserGetterServiceApplication;
import br.edu.utfpr.tsi.xenon.application.service.WorkstationApplicationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
public class StateApplicationEndpoint implements StateSystemApi {

    private final WorkstationApplicationService workstationApplicationService;
    private final CarApplicationService carApplicationService;
    private final RecognizeServiceApplication recognizeServiceApplication;
    private final UserGetterServiceApplication userGetterServiceApplication;

    @Override
    @IsAdmin
    @GetMapping("/workstations-and-recognizer")
    public ResponseEntity<List<WorkstationSummaryDto>> workstationAndRecognizer() {
        log.info("Recebendo requisição para retornar sumário de estação de trabalho e acessos");
        var summary = workstationApplicationService.getWorkstationSummary();
        return ResponseEntity.ok(summary);
    }

    @Override
    @IsAdmin
    @GetMapping("/registry-cars")
    public ResponseEntity<UserCarsSummaryDto> registryCars() {
        log.info("Recebendo requisição para retornar sumário carros cadastrados");
        var summary = carApplicationService.getUserCarsSummary();
        return ResponseEntity.ok(summary);
    }

    @Override
    @IsAdmin
    @GetMapping("/recognizers-week")
    public ResponseEntity<RecognizerSummaryDto> recognizserWeek() {
        log.info("Recebendo requisição para retornar sumário reconhecimento na semana");
        var summary = recognizeServiceApplication.getRecognizerSummary();
        return ResponseEntity.ok(summary);
    }

    @Override
    @IsAdmin
    @GetMapping("/registry-users")
    public ResponseEntity<UsersRegistrySummaryDto> registryUser() {
        log.info("Recebendo requisição para retornar sumário dos usuários cadastrados por tipo");
        var summary = userGetterServiceApplication.usersRegistrySummary();
        return ResponseEntity.ok(summary);
    }
}
