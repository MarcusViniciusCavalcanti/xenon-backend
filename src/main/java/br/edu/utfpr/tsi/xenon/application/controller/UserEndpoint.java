package br.edu.utfpr.tsi.xenon.application.controller;

import br.edu.utfpr.tsi.xenon.application.api.UserApi;
import br.edu.utfpr.tsi.xenon.application.dto.CarDto;
import br.edu.utfpr.tsi.xenon.application.dto.DocumentUriDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputAccessUserDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputUpdateUserDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputUserDto;
import br.edu.utfpr.tsi.xenon.application.dto.PageCarWaitingDecisionDto;
import br.edu.utfpr.tsi.xenon.application.dto.PageUserCarAccessDto;
import br.edu.utfpr.tsi.xenon.application.dto.PageUserDto;
import br.edu.utfpr.tsi.xenon.application.dto.ProcessResultDto;
import br.edu.utfpr.tsi.xenon.application.dto.UserDto;
import br.edu.utfpr.tsi.xenon.application.rules.IsAdmin;
import br.edu.utfpr.tsi.xenon.application.service.CarApplicationService;
import br.edu.utfpr.tsi.xenon.application.service.RecognizeServiceApplication;
import br.edu.utfpr.tsi.xenon.application.service.UserCreatorServiceApplication;
import br.edu.utfpr.tsi.xenon.application.service.UserDeleterApplicationService;
import br.edu.utfpr.tsi.xenon.application.service.UserGetterServiceApplication;
import br.edu.utfpr.tsi.xenon.application.service.UserUpdaterServiceApplication;
import br.edu.utfpr.tsi.xenon.structure.DirectionEnum;
import br.edu.utfpr.tsi.xenon.structure.MessagesMapper;
import br.edu.utfpr.tsi.xenon.structure.ParamsQuerySearchUserDto;
import br.edu.utfpr.tsi.xenon.structure.ParamsQuerySearchUserDto.SortedUserPropertyEnum;
import br.edu.utfpr.tsi.xenon.structure.ParamsQuerySearchUserDto.Type;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserEndpoint implements UserApi, EndpointsTranslator {

    private final MessageSource messageSource;
    private final UserCreatorServiceApplication userCreatorServiceApplication;
    private final UserUpdaterServiceApplication userUpdaterServiceApplication;
    private final UserGetterServiceApplication userGetterServiceApplication;
    private final UserDeleterApplicationService userDeleterApplicationService;
    private final CarApplicationService carApplicationService;
    private final RecognizeServiceApplication recognizeServiceApplication;

    @Override
    @IsAdmin
    @PostMapping
    public ResponseEntity<UserDto> createNewUser(InputUserDto inputUserDto, String authorization) {
        log.info("Recebendo requisição para criar usuário.");
        log.debug(
            "Recebendo requisição para criar usuário. input: {} authorization: {}",
            inputUserDto,
            authorization);

        var user = userCreatorServiceApplication.createNewUser(inputUserDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @Override
    @IsAdmin
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable("id") Long id, String authorization,
        String acceptLanguage) {
        log.info("Recebendo solicitação para recuperar usuário por id");
        log.debug("Recebendo solicitação para recuperar usuário por id: {}", id);
        var user = userGetterServiceApplication.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @Override
    @IsAdmin
    @DeleteMapping
    public ResponseEntity<ProcessResultDto> disabledUserAccount(
        InputAccessUserDto inputAccessUserDto, String authorization, String acceptLanguage) {
        log.info("Recebendo solicitação para desativar usuário.");
        log.debug("Recebendo solicitação para desativar usuário. input: {}", inputAccessUserDto);
        log.debug("autorização: {}", authorization);
        var result =
            userDeleterApplicationService.disableAccount(inputAccessUserDto);
        var locale = getLocale(acceptLanguage);
        var message = getMessage(result.getResult(), locale);
        var response = new ProcessResultDto().result(message);

        return ResponseEntity.ok(response);
    }

    @Override
    @IsAdmin
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
        @PathVariable("id") Long id,
        InputUpdateUserDto inputUpdateUserDto,
        String authorization) {
        log.info("Recebendo requisição para atualizar usuário.");
        log.debug(
            "Recebendo requisição para atualizar usuário. id: {}, input: {} authorization: {}",
            id,
            inputUpdateUserDto,
            authorization);
        var user = userUpdaterServiceApplication.updateUser(inputUpdateUserDto, id);
        return ResponseEntity.ok(user);
    }

    @Override
    @IsAdmin
    @PatchMapping("/disabled/access")
    public ResponseEntity<ProcessResultDto> disableUserAccess(InputAccessUserDto inputAccessUserDto,
        String authorization, String acceptLanguage) {
        log.info("Recebendo requisição para remover acesso do usuário.");
        log.debug(
            "Recebendo requisição para remover acesso do usuário. input: {} authorization: {}",
            inputAccessUserDto,
            authorization);
        var locale = getLocale(acceptLanguage);
        var result = userUpdaterServiceApplication.unauthorizedAccess(inputAccessUserDto);
        var message = getMessage(result.getResult(), locale);
        var response = new ProcessResultDto().result(message);

        return ResponseEntity.ok(response);
    }

    @Override
    @IsAdmin
    @PatchMapping("/enabled/access")
    public ResponseEntity<ProcessResultDto> enabledUserAccess(
        InputAccessUserDto inputAccessUserDto,
        String authorization,
        String acceptLanguage) {
        log.info("Recebendo requisição para autorizar acesso usuário.");
        log.debug(
            "Recebendo requisição para autorizar acesso usuário. input: {} authorization: {}",
            inputAccessUserDto,
            authorization);

        var locale = getLocale(acceptLanguage);
        var result = userUpdaterServiceApplication.authorizedAccess(inputAccessUserDto);
        var message = getMessage(result.getResult(), locale);
        var response = new ProcessResultDto().result(message);

        return ResponseEntity.ok(response);
    }

    @Override
    @IsAdmin
    @GetMapping("/all")
    public ResponseEntity<PageUserDto> getAllUsers(String authorization, Integer size, Integer page,
        String sorted, String direction, String nameOrEmail, String type) {
        log.info("Recebendo requisição para recuperar todos usuário.");
        var directionEnum = DirectionEnum.fromValue(direction);
        var sortedEnum = SortedUserPropertyEnum.fromValue(sorted);
        var params = ParamsQuerySearchUserDto.builder()
            .direction(directionEnum)
            .nameOrEmail(nameOrEmail)
            .page(page.longValue())
            .size(size.longValue())
            .type(Type.fromValue(type))
            .sorted(sortedEnum)
            .build();

        var pageUser = userGetterServiceApplication.getAllUser(params);
        return ResponseEntity.ok(pageUser);
    }

    @Override
    @IsAdmin
    @PatchMapping("/car/{id}/approved")
    public ResponseEntity<Void> documentApproved(
        @PathVariable("id") Long id,
        String authorization) {
        log.info("Recebendo requisição para aprovar documento");
        log.debug("de id: {}", id);

        carApplicationService.authorisedCar(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @IsAdmin
    @PatchMapping("/car/{id}/reproved")
    public ResponseEntity<Void> documentReproved(
        @PathVariable("id") Long id,
        String authorization,
        String acceptLanguage) {
        log.info("Recebendo requisição para reprovar documento");
        log.debug("de id: {}", id);

        var locale = getLocale(acceptLanguage);
        var reason = getMessage(MessagesMapper.CAR_DOCUMENT_INVALID.getCode(), locale);

        carApplicationService.unauthorisedCar(id, reason);
        return ResponseEntity.noContent().build();
    }

    @Override
    @IsAdmin
    @GetMapping("{id}/cars")
    public ResponseEntity<List<CarDto>> getCars(@PathVariable("id") Long id, String authorization) {
        log.info("Recebendo requisição para listar todos carros");
        var cars = carApplicationService.getAllCarsByUser(id);
        return ResponseEntity.ok(cars);
    }

    @Override
    @IsAdmin
    @GetMapping("/car/{id}/access")
    public ResponseEntity<PageUserCarAccessDto> getAllAccess(
        @PathVariable("id") Long id,
        Integer size,
        Integer page) {
        log.info("Recebendo requisição para listar todos os reconhecimentos");
        log.debug("de id: {}, size: {}, page: {}", id, size, page);
        var pageResponse = recognizeServiceApplication.getAllAccessCarUser(id, size, page);
        return ResponseEntity.ok(pageResponse);
    }

    @Override
    @IsAdmin
    @GetMapping("/{id}/prepare-download/document")
    public ResponseEntity<DocumentUriDto> downloadDocument(
        @PathVariable("id") Long id,
        String authorization) {
        log.info("Recebendo requisição para processar download de documento");
        var documentUri = carApplicationService.downloadDocument(id);
        return ResponseEntity.ok(documentUri);
    }

    @Override
    @IsAdmin
    @GetMapping("/cars/waiting-decision")
    public ResponseEntity<PageCarWaitingDecisionDto> getAllCarsWaitingDecision(
        Integer size,
        Integer page) {
        log.info("Recebendo requisição para retornar lista de carros aguardando decisão");
        var pageResponse = carApplicationService.pageCarWaitingDecision(size, page);
        return ResponseEntity.ok(pageResponse);
    }

    @Override
    public MessageSource getMessageSource() {
        return messageSource;
    }
}
