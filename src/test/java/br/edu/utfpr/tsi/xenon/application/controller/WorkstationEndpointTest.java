package br.edu.utfpr.tsi.xenon.application.controller;

import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.ACCESS_DENIED;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.ARGUMENT_INVALID;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.mapper.ObjectMapperType.JACKSON_2;
import static java.lang.Boolean.FALSE;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import br.edu.utfpr.tsi.xenon.AbstractSecurityContext;
import br.edu.utfpr.tsi.xenon.application.dto.InputLoginDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputWorkstationDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputWorkstationDto.ModeEnum;
import br.edu.utfpr.tsi.xenon.application.dto.WorkstationDto;
import br.edu.utfpr.tsi.xenon.domain.recognize.entity.RecognizeEntity;
import br.edu.utfpr.tsi.xenon.domain.workstations.entity.WorkstationEntity;
import br.edu.utfpr.tsi.xenon.structure.MessagesMapper;
import br.edu.utfpr.tsi.xenon.structure.repository.RecognizerRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.ResourceLocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

@DisplayName("Test - Integration - Funcionalidade de estação de trabalho")
class WorkstationEndpointTest extends AbstractSecurityContext {

    public static final String URL_WORKSTATION_APPROVED_ACCESS =
        "/api/workstations/{workstationId}/approved-access/recognizer/{recognizerId}";
    private static final String URL_WORKSTATION = "/api/workstations";
    private static final String URL_WORKSTATION_BY_ID = "/api/workstations/{id}";
    @Autowired
    private WorkstationRepository workstationRepository;

    @Autowired
    private RecognizerRepository recognizerRepository;

    @Test
    @DisplayName("Deve retornar Unauthorized quando usuário tiver papel de motorista")
    @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository")
    void shouldReturnErroForbiddenUserDriver() {
        var locale = Locale.US;
        var message = messageSource.getMessage(ACCESS_DENIED.getCode(), null, locale);

        var user = createDriver();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);
        assertForbidden(locale, message, faker.internet().ipV4Address());

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve retornar Unauthorized quando usuário tiver papel de motorista")
    @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository")
    void shouldReturnErroForbiddenUserOperator() {
        var locale = Locale.US;
        var message = messageSource.getMessage(ACCESS_DENIED.getCode(), null, locale);

        var user = createOperator();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);
        assertForbidden(locale, message, faker.internet().ipV6Address());

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve retornar bad request quando corpo da chamada está inválido")
    @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository")
    void shouldReturnBadRequest() {
        var locale = Locale.US;
        var message = messageSource.getMessage(ARGUMENT_INVALID.getCode(), null, locale);

        var user = createAdmin();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", locale.toLanguageTag())
            .contentType(JSON)
            .body(new InputWorkstationDto(), JACKSON_2)
            .expect()
            .statusCode(BAD_REQUEST.value())
            .body("message", is(message))
            .body("statusCode", is(400))
            .body("path", is("/workstations"))
            .body("details.findAll { it }.field", hasItems(
                "ip", "name", "mode")
            )
            .body("details.findAll { it }.descriptionError", hasItems(
                "must not be null", "must not be null", "must not be null", "must not be null")
            )
            .when()
            .post(URL_WORKSTATION);

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve criar com sucesso")
    @ResourceLocks(value = {
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository")
    })
    void shouldHaveCriarWorkstation() {
        var user = createAdmin();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var inputCreateWorkstation = new InputWorkstationDto()
            .name(faker.rockBand().name())
            .ip(faker.internet().ipV6Address())
            .mode(ModeEnum.MANUAL)
            .port(9090);

        var id = given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .contentType(JSON)
            .body(inputCreateWorkstation, JACKSON_2)
            .expect()
            .statusCode(CREATED.value())
            .body("id", notNullValue())
            .body("name", is(inputCreateWorkstation.getName()))
            .body("mode", is(inputCreateWorkstation.getMode().name()))
            .body("port", is(inputCreateWorkstation.getPort()))
            .when()
            .post(URL_WORKSTATION)
            .body().as(WorkstationDto.class).getId();

        workstationRepository.deleteById(id);
        deleteUser(user);
    }

    @Test
    @DisplayName("Deve atualizar workstation com sucesso")
    @ResourceLocks(value = {
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository")
    })
    void shouldHaveUpdateWorkstation() {
        var user = createAdmin();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var workstation = new WorkstationEntity();
        workstation.setMode(ModeEnum.NONE.name());
        workstation.setName(faker.name().name());
        workstation.setIp(faker.internet().ipV4Address());
        workstation.setPort(9090);
        workstation.setKey("key");

        workstation = workstationRepository.saveAndFlush(workstation);

        var inputCreateWorkstation = new InputWorkstationDto()
            .name(faker.rockBand().name())
            .ip(faker.internet().ipV6Address())
            .mode(ModeEnum.MANUAL)
            .port(8080);

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .contentType(JSON)
            .pathParam("id", workstation.getId())
            .body(inputCreateWorkstation, JACKSON_2)
            .expect()
            .statusCode(OK.value())
            .body("id", is(workstation.getId().intValue()))
            .body("name", is(inputCreateWorkstation.getName()))
            .body("mode", is(inputCreateWorkstation.getMode().name()))
            .body("ip", is(inputCreateWorkstation.getIp()))
            .body("port", is(inputCreateWorkstation.getPort()))
            .when()
            .put(URL_WORKSTATION_BY_ID);

        //noinspection OptionalGetWithoutIsPresent
        var workstationUpdated = workstationRepository.findById(workstation.getId()).get();

        assertEquals(inputCreateWorkstation.getMode().name(), workstationUpdated.getMode());
        assertEquals(inputCreateWorkstation.getIp(), workstationUpdated.getIp());
        assertEquals(inputCreateWorkstation.getName(), workstationUpdated.getName());
        assertEquals(inputCreateWorkstation.getPort(), workstationUpdated.getPort());

        workstationRepository.delete(workstationUpdated);
        deleteUser(user);
    }

    @Test
    @DisplayName("Deve deletar workstation com sucesso")
    @ResourceLocks(value = {
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository")
    })
    void shouldHaveDeleteWorkstation() {
        var user = createAdmin();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var workstation = new WorkstationEntity();
        workstation.setMode(ModeEnum.NONE.name());
        workstation.setName(faker.name().name());
        workstation.setIp(faker.internet().ipV4Address());
        workstation.setPort(9090);
        workstation.setKey("key");

        workstation = workstationRepository.saveAndFlush(workstation);

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .contentType(JSON)
            .pathParam("id", workstation.getId())
            .expect()
            .statusCode(NO_CONTENT.value())
            .when()
            .delete(URL_WORKSTATION_BY_ID);

        assertTrue(workstationRepository.findById(workstation.getId()).isEmpty());
        deleteUser(user);
    }

    @Test
    @DisplayName("Deve retornar lista de Estações de Trabalho")
    @ResourceLocks(value = {
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository")
    })
    void shouldReturnListWorkstation() {
        var user = createAdmin();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var workstation = new WorkstationEntity();
        workstation.setMode(ModeEnum.NONE.name());
        workstation.setName(faker.name().name());
        workstation.setIp(faker.internet().ipV4Address());
        workstation.setPort(9090);
        workstation.setKey("key");

        workstationRepository.saveAndFlush(workstation);

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .contentType(JSON)
            .expect()
            .statusCode(OK.value())
            .body("[0].id", is(workstation.getId().intValue()))
            .body("[0].name", is(workstation.getName()))
            .body("[0].ip", is(workstation.getIp()))
            .body("[0].mode", is(workstation.getMode()))
            .body("[0].port", is(workstation.getPort()))
            .body("[0].key", is(workstation.getKey()))
            .when()
            .get(URL_WORKSTATION);

        deleteUser(user);
        workstationRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve retornar Estação de Trabalho pelo id")
    @ResourceLocks(value = {
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository")
    })
    void shouldReturnWorkstationById() {
        var user = createAdmin();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var workstation = new WorkstationEntity();
        workstation.setMode(ModeEnum.NONE.name());
        workstation.setName(faker.name().name());
        workstation.setIp(faker.internet().ipV4Address());
        workstation.setPort(9090);
        workstation.setKey("key");

        workstationRepository.saveAndFlush(workstation);

        var id = given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .contentType(JSON)
            .pathParam("id", workstation.getId())
            .expect()
            .statusCode(OK.value())
            .body("id", is(workstation.getId().intValue()))
            .body("name", is(workstation.getName()))
            .body("mode", is(workstation.getMode()))
            .body("port", is(workstation.getPort()))
            .body("key", is(workstation.getKey()))
            .body("ip", is(workstation.getIp()))
            .when()
            .get(URL_WORKSTATION_BY_ID)
            .body().as(WorkstationDto.class).getId();

        workstationRepository.deleteById(id);
        deleteUser(user);
    }

    @Test
    @DisplayName("Deve retornar not found quando buscar estação de trabalho e id não existe")
    @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository")
    void shouldReturnNotFound() {
        var user = createAdmin();

        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var id = Math.abs(new Random().nextLong());
        var message = messageSource.getMessage(
            MessagesMapper.RESOURCE_NOT_FOUND.getCode(),
            new String[] {"estação de trabalho", "%d".formatted(id)},
            Locale.US
        );

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", Locale.US.toLanguageTag())
            .pathParam("id", id)
            .contentType(JSON)
            .expect()
            .statusCode(NOT_FOUND.value())
            .body("message", is(message))
            .body("statusCode", is(NOT_FOUND.value()))
            .body("path", is("/workstations/%d".formatted(id)))
            .when()
            .get(URL_WORKSTATION_BY_ID);

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve retornar not found quando liberar acesso e estação de trabalho e id não existe")
    @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository")
    void shouldReturnNotFoundApprovedAccess() {
        var user = createAdmin();

        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var id = Math.abs(new Random().nextLong());
        var message = messageSource.getMessage(
            MessagesMapper.RESOURCE_NOT_FOUND.getCode(),
            new String[] {"reconhecimento", "%d".formatted(id)},
            Locale.US
        );

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", Locale.US.toLanguageTag())
            .pathParam("workstationId", id)
            .pathParam("recognizerId", id)
            .contentType(JSON)
            .expect()
            .statusCode(NOT_FOUND.value())
            .body("message", is(message))
            .body("statusCode", is(NOT_FOUND.value()))
            .body("path", is("/workstations/%d/approved-access/recognizer/%d".formatted(id, id)))
            .when()
            .post(URL_WORKSTATION_APPROVED_ACCESS);

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve retornar not found quando liberar acesso e reconhecimento e id não existe")
    @ResourceLocks(value = {
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository")
    })
    void shouldReturnNotFoundApprovedAccessRecognizer() {
        var user = createAdmin();

        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var workstation = new WorkstationEntity();
        workstation.setMode(ModeEnum.NONE.name());
        workstation.setName(faker.name().name());
        workstation.setIp(faker.internet().ipV4Address());
        workstation.setPort(9090);
        workstation.setKey("key");

        workstationRepository.saveAndFlush(workstation);

        var id = Math.abs(new Random().nextLong());
        var message = messageSource.getMessage(
            MessagesMapper.RESOURCE_NOT_FOUND.getCode(),
            new String[] {"reconhecimento", "%d".formatted(id)},
            Locale.US
        );

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", Locale.US.toLanguageTag())
            .pathParam("workstationId", workstation.getId())
            .pathParam("recognizerId", id)
            .contentType(JSON)
            .expect()
            .statusCode(NOT_FOUND.value())
            .body("message", is(message))
            .body("statusCode", is(NOT_FOUND.value()))
            .body("path",
                is("/workstations/%d/approved-access/recognizer/%d".formatted(workstation.getId(),
                    id)))
            .when()
            .post(URL_WORKSTATION_APPROVED_ACCESS);

        deleteUser(user);
        workstationRepository.delete(workstation);
    }

    @Test
    @DisplayName("Deve aprovar e enviar requisição para liberar acesso")
    @ResourceLocks(value = {
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.RecognizerRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository")
    })
    void shouldaHaveApprovedAccess() {
        var user = createAdmin();

        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var workstation = new WorkstationEntity();
        workstation.setMode(ModeEnum.NONE.name());
        workstation.setName(faker.name().name());
        workstation.setIp(faker.internet().ipV4Address());
        workstation.setPort(9090);
        workstation.setKey("key");

        workstationRepository.saveAndFlush(workstation);

        var recognizerExpected = new RecognizeEntity();
        recognizerExpected.setConfidence(99.9F);
        recognizerExpected.setEpochTime(LocalDateTime.now());
        recognizerExpected.setPlate("plate");
        recognizerExpected.setOriginIp("ip");
        recognizerExpected.setDriverName("driveName");
        recognizerExpected.setHasError(FALSE);

        recognizerRepository.saveAndFlush(recognizerExpected);

        var wireMockServer =
            new WireMockServer(new WireMockConfiguration().dynamicPort());
        wireMockServer.start();

        wireMockServer.stubFor(
            get(urlEqualTo("/%d/open".formatted(workstation.getId())))
                .willReturn(aResponse()
                    .withStatus(NO_CONTENT.value())
                )
        );

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", Locale.US.toLanguageTag())
            .pathParam("workstationId", workstation.getId())
            .pathParam("recognizerId", recognizerExpected.getId())
            .contentType(JSON)
            .expect()
            .statusCode(NO_CONTENT.value())
            .when()
            .post(URL_WORKSTATION_APPROVED_ACCESS);

        deleteUser(user);
        recognizerRepository.delete(recognizerExpected);
        workstationRepository.delete(workstation);
    }

    private void assertForbidden(Locale locale, String message, String s) {
        var inputCreateWorkstation = new InputWorkstationDto()
            .name(faker.rockBand().name())
            .ip(s)
            .mode(ModeEnum.MANUAL)
            .port(9090);

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", locale.toLanguageTag())
            .contentType(JSON)
            .body(inputCreateWorkstation, JACKSON_2)
            .expect()
            .statusCode(FORBIDDEN.value())
            .body("message", is(message))
            .body("statusCode", is(403))
            .when()
            .post(URL_WORKSTATION);
    }
}
