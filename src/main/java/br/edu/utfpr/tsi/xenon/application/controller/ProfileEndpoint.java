package br.edu.utfpr.tsi.xenon.application.controller;

import br.edu.utfpr.tsi.xenon.application.api.ProfileApi;
import br.edu.utfpr.tsi.xenon.application.dto.InputNameUserDto;
import br.edu.utfpr.tsi.xenon.application.dto.ProcessResultDto;
import br.edu.utfpr.tsi.xenon.application.dto.UserDto;
import br.edu.utfpr.tsi.xenon.application.service.UserServiceApplication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileEndpoint implements ProfileApi {

    private final UserServiceApplication userServiceApplication;

    @Override
    @GetMapping("/me")
    public ResponseEntity<UserDto> getUserOwnerToken(String authorization, String acceptLanguage) {
        log.info("Recebendo solicitação para todo do token");
        log.debug("Recebendo solicitação para todo do token, token: '[{}]'", authorization);

        var user = userServiceApplication.getUserByToken(authorization);
        return ResponseEntity.ok(user);
    }

    @Override
    public ResponseEntity<ProcessResultDto> changeName(String authorization,
        InputNameUserDto inputNameUserDto) {
        log.info("Recebendo solicitação troca de nome");
        log.debug("Recebendo solicitação troca de nome '[{}]'", authorization);

        var result = userServiceApplication.changeName(inputNameUserDto, authorization);

        return ProfileApi.super.changeName(authorization, inputNameUserDto);
    }
}
