package br.edu.utfpr.tsi.xenon.application.controller;

import br.edu.utfpr.tsi.xenon.application.api.SecurityApi;
import br.edu.utfpr.tsi.xenon.application.dto.InputLoginDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputRenewPasswordDto;
import br.edu.utfpr.tsi.xenon.application.dto.ProcessResultDto;
import br.edu.utfpr.tsi.xenon.application.dto.TokenDto;
import br.edu.utfpr.tsi.xenon.application.service.SecurityApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SecurityEndpoint implements SecurityApi, EndpointsTranslator {
    private final SecurityApplicationService securityApplicationService;
    private final MessageSource messageSource;

    @Override
    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(InputLoginDto inputLoginDto) {
        log.info("Recebendo solicitação de token.");
        var token = securityApplicationService.processSignIn(inputLoginDto);
        return ResponseEntity.ok(token);
    }

    @Override
    @PostMapping("/request-renew-pass")
    public ResponseEntity<ProcessResultDto> renewPassword(
        InputRenewPasswordDto inputRenewPasswordDto,
        String acceptLanguage) {
        log.info("Recebendo solicitação para nova senha.");
        var result = securityApplicationService.receiveRequestRenewPass(inputRenewPasswordDto);
        var message = getMessage(
            result.getResult(),
            getLocale(acceptLanguage),
            inputRenewPasswordDto.getEmail());
        return ResponseEntity.accepted().body(new ProcessResultDto().result(message));
    }

    @Override
    @GetMapping("/request-renew-pass")
    public ResponseEntity<ProcessResultDto> renewPasswordConfirm(
        String acceptLanguage,
        String params) {
        var result = securityApplicationService.confirmRequestRenewPass(params);

        var message = getMessage(
            result.getResult(),
            getLocale(acceptLanguage));
        return ResponseEntity.accepted().body(new ProcessResultDto().result(message));
    }

    @Override
    public MessageSource getMessage() {
        return messageSource;
    }
}
