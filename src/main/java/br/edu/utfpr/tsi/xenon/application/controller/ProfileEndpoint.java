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
import br.edu.utfpr.tsi.xenon.application.service.UserDeleterApplicationService;
import br.edu.utfpr.tsi.xenon.application.service.SecurityApplicationService;
import br.edu.utfpr.tsi.xenon.application.service.UserGetterServiceApplication;
import br.edu.utfpr.tsi.xenon.application.service.UserUpdaterServiceApplication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileEndpoint implements ProfileApi, EndpointsTranslator {

    private final SecurityApplicationService securityApplicationService;
    private final CarApplicationService carApplicationService;
    private final UserGetterServiceApplication userGetterServiceApplication;
    private final UserUpdaterServiceApplication userUpdaterServiceApplication;
    private final UserDeleterApplicationService userDeleterApplicationService;

    private final MessageSource messageSource;

    @Override
    @GetMapping("/me")
    public ResponseEntity<UserDto> getUserOwnerToken(String authorization, String acceptLanguage) {
        log.info("Recebendo solicita????o para todo do token");
        log.debug("Recebendo solicita????o para todo do token, token: '[{}]'", authorization);

        var user = userGetterServiceApplication.getUserByToken(authorization);
        return ResponseEntity.ok(user);
    }

    @Override
    @PatchMapping("/change-name")
    public ResponseEntity<ProcessResultDto> changeName(
        String authorization,
        String acceptLanguage,
        InputNameUserDto inputNameUserDto) {
        log.info("Recebendo solicita????o troca de nome");
        log.debug("Recebendo solicita????o troca de nome '[{}]'", authorization);

        var result = userUpdaterServiceApplication.changeName(inputNameUserDto, authorization);

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
        log.info("Recebendo solicita????o troca de nome");
        log.debug(
            "Recebendo solicita????o troca de senha  autoriza????o:'[{}]', input: '[{}]'",
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
        log.info("Recebendo solicita????o desativa????o de conta");
        log.debug("Recebendo solicita????o desativa????o de conta '[{}]'", authorization);

        userDeleterApplicationService.disableAccount(authorization, "Usu??rio desativou a conta.");
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/include-new-car")
    public ResponseEntity<CarDto> includeNewCar(
        String authorization,
        InputNewCarDto inputNewCarDto) {
        log.info("Recebendo solicita????o inclus??o de novo carro");
        log.debug("Recebendo solicita????o inclus??o de novo carro '[{}]' input: '[{}]'",
            authorization,
            inputNewCarDto);

        var car = carApplicationService.includeNewCar(inputNewCarDto, authorization);
        return ResponseEntity.ok(car);
    }

    @Override
    @DeleteMapping("/remove-car")
    public ResponseEntity<Void> removeCar(String authorization,
        InputRemoveCarDto inputRemoveCarDto) {
        log.info("Recebendo solicita????o remo????o de carro");
        log.debug("Recebendo solicita????o remo????o de carro '[{}]' input: '[{}]'",
            authorization,
            inputRemoveCarDto);

        carApplicationService.removeCar(inputRemoveCarDto, authorization);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/avatar")
    public ResponseEntity<UserDto> includeAvatar(String authorization, MultipartFile fileName) {
        var user = userUpdaterServiceApplication.changeAvatar(fileName, authorization);
        return ResponseEntity.ok(user);
    }

    @Override
    @PostMapping("/car/{id}/document")
    public ResponseEntity<Void> includeDocumentCar(
        @PathVariable("id") Long id,
        String authorization,
        MultipartFile fileName) {
        log.info("Recebendo requisi????o para incluir documento de carro");
        log.debug("carro de id {}", id);

        carApplicationService.includeDocument(id, fileName, authorization);

        return ResponseEntity.noContent().build();
    }

    @Override
    public MessageSource getMessageSource() {
        return messageSource;
    }
}
