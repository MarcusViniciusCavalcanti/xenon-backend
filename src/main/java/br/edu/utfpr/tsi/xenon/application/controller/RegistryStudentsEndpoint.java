package br.edu.utfpr.tsi.xenon.application.controller;

import br.edu.utfpr.tsi.xenon.application.api.RegistryApi;
import br.edu.utfpr.tsi.xenon.application.dto.InputRegistryStudentDto;
import br.edu.utfpr.tsi.xenon.application.dto.ProcessResultDto;
import br.edu.utfpr.tsi.xenon.application.dto.UserDto;
import br.edu.utfpr.tsi.xenon.application.service.RegistryNewStudentsApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RegistryStudentsEndpoint implements RegistryApi, EndpointsTranslator {

    private final MessageSource messageSource;

    private final RegistryNewStudentsApplicationService registryNewStudentsApplicationService;

    @Override
    @PostMapping("/new-students/registry")
    public ResponseEntity<UserDto> registerNewStudents(
        InputRegistryStudentDto inputRegistryStudentDto, String acceptLanguage) {
        log.info("Recebendo solicitação de registro.");
        log.debug("Recebendo solicitação de registro. input: '[{}]'", inputRegistryStudentDto);

        var user =
            registryNewStudentsApplicationService.registryNewStudents(inputRegistryStudentDto);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(user);
    }

    @Override
    @GetMapping("/activate-registry")
    public ResponseEntity<ProcessResultDto> activateAccount(String acceptLanguage, String params) {
        log.info("Recebendo solicitação para ativar conta");
        log.debug("Recebendo solicitação ativar conta. params: '[{}]'", params);

        var result = registryNewStudentsApplicationService.activateAccount(params);
        var locale = getLocale(acceptLanguage);
        var message = getMessage(result.getResult(), locale);
        var response = new ProcessResultDto().result(message);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @Override
    public MessageSource getMessageSource() {
        return messageSource;
    }
}
