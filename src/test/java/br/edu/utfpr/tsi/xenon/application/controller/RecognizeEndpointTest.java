package br.edu.utfpr.tsi.xenon.application.controller;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.mapper.ObjectMapperType.JACKSON_2;
import static java.time.Duration.*;
import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import br.edu.utfpr.tsi.xenon.AbstractContextTest;
import br.edu.utfpr.tsi.xenon.application.dto.InputRecognizerDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputWorkstationDto.ModeEnum;
import br.edu.utfpr.tsi.xenon.application.dto.PlatesDto;
import br.edu.utfpr.tsi.xenon.domain.workstations.entity.WorkstationEntity;
import br.edu.utfpr.tsi.xenon.structure.repository.ErrorRecognizerRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.RecognizerRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository;
import com.github.javafaker.Faker;
import java.time.Duration;
import java.util.Locale;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.ResourceLocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

@ResourceLocks(value = {
    @ResourceLock(
        value = "br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository"
    ),
    @ResourceLock(
        value = "br.edu.utfpr.tsi.xenon.structure.repository.ErrorRecognizerRepository"
    ),
    @ResourceLock(
        value = "br.edu.utfpr.tsi.xenon.structure.repository.RecognizerRepository"
    )
})
@DisplayName("Test - Integration - Funcionalidade de reconhecimento")
class RecognizeEndpointTest extends AbstractContextTest {

    private static final String URL_RECOGNIZER = "/api/receive-recognizer/{key}";

    @Autowired
    private ErrorRecognizerRepository errorRecognizerRepository;

    @Autowired
    private WorkstationRepository workstationRepository;

    @Autowired
    private RecognizerRepository recognizerRepository;

    @BeforeEach
    void clearBatabase() {
        recognizerRepository.deleteAll();
        errorRecognizerRepository.deleteAll();
    }

    @AfterEach
    void clearAfterDatabae() {
        recognizerRepository.deleteAll();
        errorRecognizerRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve salvar Error quando Estação de trabalho não foi encontrada")
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

        await()
            .with()
            .pollInterval(Duration.ofMillis(1000))
            .atMost(ofMillis(3000)).until(() -> {
            var allError = errorRecognizerRepository.findAll();

            return allError.stream()
                .anyMatch(error ->
                    error.getErrorMessage().equals("Estação de trabalho para key key não encontrada"));
        });
    }

    @Test
    @DisplayName("Deve salvar Error quando Estação de trabalho tem um ip diferente da requisição")
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

        await()
            .with()
            .pollInterval(Duration.ofMillis(1000))
            .atMost(ofMillis(3000)).until(() -> {
            var allError = errorRecognizerRepository.findAll();
            return allError.stream()
                .anyMatch(error ->
                    error.getErrorMessage().equals("Ip de origem diferente do ip 127.000.000.001 vinculado a estação"));
        });

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

        await()
            .with()
            .pollInterval(Duration.ofMillis(1000))
            .atMost(ofMillis(3000)).until(() -> {
            var allError = errorRecognizerRepository.findAll();
            return allError.stream()
                .anyMatch(error ->
                    error.getErrorMessage().equals("Reconhecimentos abaixo da confidencialidade [valor < 75.00]"));
        });

        workstationRepository.delete(workstation);
    }

    @Test
    @DisplayName("Deve não fazer nada quando input de placa esta vazio")
    void shouldNothingWhenPlatesIsEmpty() {
        var faker = Faker.instance();
        var key = "key";
        var input = new InputRecognizerDto();

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

        await().timeout(ofMillis(3000));

        var error = errorRecognizerRepository.findAll();
        var rec = recognizerRepository.findAll();

        assertThat(error).isEmpty();
        assertThat(rec).isEmpty();

        workstationRepository.delete(workstation);
    }
}
