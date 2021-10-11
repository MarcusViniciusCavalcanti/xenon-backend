package br.edu.utfpr.tsi.xenon.application.controller;

import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.ACCESS_DENIED;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.ARGUMENT_INVALID;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.mapper.ObjectMapperType.JACKSON_2;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import br.edu.utfpr.tsi.xenon.AbstractSecurityContext;
import br.edu.utfpr.tsi.xenon.application.dto.InputLoginDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputWorkstationDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputWorkstationDto.ModeEnum;
import br.edu.utfpr.tsi.xenon.application.dto.WorkstationDto;
import br.edu.utfpr.tsi.xenon.domain.workstations.entity.WorkstationEntity;
import br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository;
import java.util.Locale;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

@DisplayName("Test - Integration - Funcionalidade de estação de trabalho")
class WorkstationEndpointTest extends AbstractSecurityContext {

    private static final String URL_WORKSTATION = "/api/workstations";
    private static final String URL_WORKSTATION_PATH_ID = "/api/workstations/{id}";

    @Autowired
    private WorkstationRepository workstationRepository;

    @Test
    @DisplayName("Deve retornar Unauthorized quando usuário tiver papel de motorista")
    void shouldReturnErroForbiddenUserDriver() {
        var locale = Locale.forLanguageTag("pt_BR");
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
    void shouldReturnErroForbiddenUserOperator() {
        var locale = Locale.forLanguageTag("pt_BR");
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
    void shouldReturnBadRequest() {
        var locale = Locale.forLanguageTag("pt_BR");
        var message = messageSource.getMessage(ARGUMENT_INVALID.getCode(), null, locale);

        var user = createAdmin();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", locale)
            .contentType(JSON)
            .body(new InputWorkstationDto().ip("anyIp"), JACKSON_2)
            .expect()
            .statusCode(BAD_REQUEST.value())
            .body("message", is(message))
            .body("statusCode", is(400))
            .body("path", is("/workstations"))
            .body("details.findAll { it }.field", hasItems(
                "ip", "name", "mode")
            )
            .body("details.findAll { it }.descriptionError", hasItems(
                "não deve ser nulo", "não deve ser nulo", "não deve ser nulo")
            )
            .when()
            .post(URL_WORKSTATION);

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve criar com sucesso")
    @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository")
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
    @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository")
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
            .put(URL_WORKSTATION_PATH_ID);

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
    @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository")
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
            .delete(URL_WORKSTATION_PATH_ID);

        assertTrue(workstationRepository.findById(workstation.getId()).isEmpty());
        deleteUser(user);
    }

    @Test
    @DisplayName("Deve retornar lista de Estações de Trabalho")
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

    private void assertForbidden(Locale locale, String message, String s) {
        var inputCreateWorkstation = new InputWorkstationDto()
            .name(faker.rockBand().name())
            .ip(s)
            .mode(ModeEnum.MANUAL)
            .port(9090);

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", locale)
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
