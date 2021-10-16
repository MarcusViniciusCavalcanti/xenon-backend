package br.edu.utfpr.tsi.xenon.application.controller;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.mapper.ObjectMapperType.JACKSON_2;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import br.edu.utfpr.tsi.xenon.AbstractContextTest;
import br.edu.utfpr.tsi.xenon.application.dto.InputRecognizerDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputWorkstationDto.ModeEnum;
import br.edu.utfpr.tsi.xenon.application.dto.PlatesDto;
import br.edu.utfpr.tsi.xenon.domain.user.factory.TypeUser;
import br.edu.utfpr.tsi.xenon.domain.workstations.entity.WorkstationEntity;
import br.edu.utfpr.tsi.xenon.structure.repository.ErrorRecognizerRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository;
import com.github.javafaker.Faker;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.ResourceLocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

@DisplayName("Test - Integration - Funcionalidade de reconhecimento")
class RecognizeEndpointTest extends AbstractContextTest {

    private static final String URL_RECOGNIZER = "/api/receive-recognizer/{key}";

    @MockBean
    private ErrorRecognizerRepository errorRecognizerRepository;

    @Autowired
    private WorkstationRepository workstationRepository;

    @Test
    @DisplayName("Deve salvar Error quando Estação de trabalho não foi encontrada")
    @ResourceLock(
        value = "br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository",
        mode = ResourceAccessMode.READ
    )
    void shouldSaveErrorWorkstationNotFound() {
        var faker = Faker.instance();
        var key = "key";
        var input = new InputRecognizerDto()
            .addRecognizersItem(
                new PlatesDto()
                    .plate(faker.bothify("???-###", true))
                    .confidence(78.0F));

        given()
            .port(port)
            .accept(APPLICATION_JSON_VALUE)
            .header("Accept-Language", Locale.forLanguageTag("en-US"))
            .contentType(JSON)
            .body(input, JACKSON_2)
            .pathParam("key", key)
            .expect()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .when()
            .post(URL_RECOGNIZER);

        verify(errorRecognizerRepository, timeout(1000)).save(argThat(error ->
            error.getErrorMessage().equals("Estação de trabalho para key key não encontrada")
        ));
    }

    @Test
    @DisplayName("Deve salvar Error quando Estação de trabalho tem um ip diferente da requisição")
    @ResourceLock(
        value = "br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository"
    )
    void shouldSaveErrorWorkstationIpNotAllows() {
        var faker = Faker.instance();
        var key = "key";
        var input = new InputRecognizerDto()
            .addRecognizersItem(
                new PlatesDto()
                    .plate(faker.bothify("???-###", true))
                    .confidence(78.0F));

        var workstation = new WorkstationEntity();
        workstation.setMode(ModeEnum.NONE.name());
        workstation.setName(faker.name().name());
        workstation.setIp(faker.internet().ipV4Address());
        workstation.setPort(9090);
        workstation.setKey(key);

        workstationRepository.saveAndFlush(workstation);

        given()
            .port(port)
            .accept(APPLICATION_JSON_VALUE)
            .header("Accept-Language", Locale.forLanguageTag("en-US"))
            .contentType(JSON)
            .body(input, JACKSON_2)
            .pathParam("key", key)
            .expect()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .when()
            .post(URL_RECOGNIZER);

        verify(errorRecognizerRepository, timeout(1000)).save(argThat(error ->
            error.getErrorMessage().equals("Ip de origem diferente do ip 127.000.000.001 vinculado a estação")
        ));

        workstationRepository.delete(workstation);
    }

    @Test
    @DisplayName("Deve salvar Error quando reconhecimento está abaixo de 75.0 de confiabilidade")
    @ResourceLock(
        value = "br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository"
    )
    void shouldSaveErrorWorkstationConfidenceMinorLimits() {
        var faker = Faker.instance();
        var key = "key";
        var input = new InputRecognizerDto()
            .addRecognizersItem(
                new PlatesDto()
                    .plate(faker.bothify("???-###", true))
                    .confidence(74.99F));

        var workstation = new WorkstationEntity();
        workstation.setMode(ModeEnum.NONE.name());
        workstation.setName(faker.name().name());
        workstation.setIp("127.000.000.001");
        workstation.setPort(9090);
        workstation.setKey(key);

        workstationRepository.saveAndFlush(workstation);

        given()
            .port(port)
            .accept(APPLICATION_JSON_VALUE)
            .header("Accept-Language", Locale.forLanguageTag("en-US"))
            .contentType(JSON)
            .body(input, JACKSON_2)
            .pathParam("key", key)
            .expect()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .when()
            .post(URL_RECOGNIZER);

        verify(errorRecognizerRepository, timeout(1000)).save(argThat(error ->
            error.getErrorMessage().equals("Reconhecimentos abaixo da confidencialidade [valor < 75.00]")
        ));

        workstationRepository.delete(workstation);
    }
}
