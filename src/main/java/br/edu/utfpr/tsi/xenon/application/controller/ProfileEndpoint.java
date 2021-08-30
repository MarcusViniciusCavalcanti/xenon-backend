package br.edu.utfpr.tsi.xenon.application.controller;

import br.edu.utfpr.tsi.xenon.application.api.ProfileApi;
import br.edu.utfpr.tsi.xenon.application.dto.CarDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputChangePasswordDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputNameUserDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputNewCarDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputRemoveCarDto;
import br.edu.utfpr.tsi.xenon.application.dto.ProcessResultDto;
import br.edu.utfpr.tsi.xenon.application.dto.UserDto;
import br.edu.utfpr.tsi.xenon.application.service.CarApplicationService;
import br.edu.utfpr.tsi.xenon.application.service.SecurityApplicationService;
import br.edu.utfpr.tsi.xenon.application.service.UserServiceApplication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileEndpoint implements ProfileApi, EndpointsTranslator {

    private final UserServiceApplication userServiceApplication;
    private final SecurityApplicationService securityApplicationService;
    private final CarApplicationService carApplicationService;

    private final MessageSource messageSource;

    @Override
    @GetMapping("/me")
    public ResponseEntity<UserDto> getUserOwnerToken(String authorization, String acceptLanguage) {
        log.info("Recebendo solicitação para todo do token");
        log.debug("Recebendo solicitação para todo do token, token: '[{}]'", authorization);

        var user = userServiceApplication.getUserByToken(authorization);
        return ResponseEntity.ok(user);
    }

    @Override
    @PatchMapping("/change-name")
    public ResponseEntity<ProcessResultDto> changeName(
        String authorization,
        String acceptLanguage,
        InputNameUserDto inputNameUserDto) {
        log.info("Recebendo solicitação troca de nome");
        log.debug("Recebendo solicitação troca de nome '[{}]'", authorization);

        var result = userServiceApplication.changeName(inputNameUserDto, authorization);

        var locale = getLocale(acceptLanguage);
        var message = getMessage(result.getResult(), locale);
        var response = new ProcessResultDto().result(message);
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/change-password")
    public ResponseEntity<ProcessResultDto> changePassword(
        InputChangePasswordDto inputChangePasswordDto,
        String authorization,
        String acceptLanguage) {
        log.info("Recebendo solicitação troca de nome");
        log.debug(
            "Recebendo solicitação troca de senha  autorização:'[{}]', input: '[{}]'",
            authorization,
            inputChangePasswordDto);

        var result = securityApplicationService.changePass(inputChangePasswordDto, authorization);

        var locale = getLocale(acceptLanguage);
        var message = getMessage(result.getResult(), locale);
        var response = new ProcessResultDto().result(message);
        return ResponseEntity.accepted().body(response);
    }

    @Override
    @DeleteMapping("/disable-account")
    public ResponseEntity<Void> disableAccount(String authorization) {
        log.info("Recebendo solicitação desativação de conta");
        log.debug("Recebendo solicitação desativação de conta '[{}]'", authorization);

        securityApplicationService.disableAccount(authorization, "Usuário desativou a conta.");
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/include-new-car")
    public ResponseEntity<CarDto> includeNewCar(
        String authorization,
        InputNewCarDto inputNewCarDto) {
        log.info("Recebendo solicitação inclusão de novo carro");
        log.debug("Recebendo solicitação inclusão de novo carro '[{}]' input: '[{}]'",
            authorization,
            inputNewCarDto);

        var car = carApplicationService.includeNewCar(inputNewCarDto, authorization);
        return ResponseEntity.ok(car);
    }

    @Override
    @DeleteMapping("/remove-car")
    public ResponseEntity<Void> removeCar(String authorization,
        InputRemoveCarDto inputRemoveCarDto) {
        log.info("Recebendo solicitação remoção de carro");
        log.debug("Recebendo solicitação remoção de carro '[{}]' input: '[{}]'",
            authorization,
            inputRemoveCarDto);

        carApplicationService.removeCar(inputRemoveCarDto, authorization);
        return ResponseEntity.noContent().build();
    }

    @Override
    public MessageSource getMessageSource() {
        return messageSource;
    }
}
