package br.edu.utfpr.tsi.xenon.application.controller;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.mapper.ObjectMapperType.JACKSON_2;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.time.Duration.ofMillis;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import br.edu.utfpr.tsi.xenon.AbstractSecurityContext;
import br.edu.utfpr.tsi.xenon.application.dto.InputLoginDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputRecognizerDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputWorkstationDto.ModeEnum;
import br.edu.utfpr.tsi.xenon.application.dto.PlatesDto;
import br.edu.utfpr.tsi.xenon.domain.recognize.entity.ErrorRecognizerEntity;
import br.edu.utfpr.tsi.xenon.domain.recognize.entity.RecognizeEntity;
import br.edu.utfpr.tsi.xenon.domain.workstations.entity.WorkstationEntity;
import br.edu.utfpr.tsi.xenon.structure.repository.ErrorRecognizerRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.RecognizerRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository;
import com.github.javafaker.Faker;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.ResourceLocks;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("Test - Integration - Funcionalidade de reconhecimento")
class RecognizeEndpointTest extends AbstractSecurityContext {

    private static final String URL_RECOGNIZER = "/api/receive-recognizer/{key}";
    private static final String URL_GET_ALL_RECOGNIZERS = "/api/all-recognizer";
    private static final String URL_GET_ERROR_RECOGNIZERS = "/api/error-recognizer/{id}";

    @Autowired
    private ErrorRecognizerRepository errorRecognizerRepository;

    @Autowired
    private WorkstationRepository workstationRepository;

    @Autowired
    private RecognizerRepository recognizerRepository;

    private static Stream<Arguments> providerListPlateInvalid() {
        return Stream.of(
            Arguments.of(new InputRecognizerDto()),
            Arguments.of(new InputRecognizerDto().recognizers(List.of()))
        );
    }

    @BeforeEach
    @AfterEach
    void clearBatabase() {
        clearDb();
    }

    private void clearDb() {
        recognizerRepository.deleteAll();
        errorRecognizerRepository.deleteAll();
    }

    @Test
    @ResourceLocks(value = {
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.ErrorRecognizerRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.RecognizerRepository")
    })
    @DisplayName("Deve retonar o erro do reconhecimento pelo ID")
    void shouldReturnErrorRecognizer() {
        var user = createAdmin();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var faker = Faker.instance();
        var key = faker.internet().password();

        var workstation = new WorkstationEntity();
        workstation.setMode(ModeEnum.NONE.name());
        workstation.setName(faker.name().name());
        workstation.setIp(faker.internet().ipV4Address());
        workstation.setPort(9090);
        workstation.setKey(key);

        workstationRepository.saveAndFlush(workstation);

        var recognizer = new RecognizeEntity();
        recognizer.setConfidence(99.9F);
        recognizer.setEpochTime(LocalDateTime.now());
        recognizer.setPlate("plate");
        recognizer.setOriginIp("ip");
        recognizer.setDriverName("driveName");
        recognizer.setHasError(TRUE);

        var errorRecognizerExpected = new ErrorRecognizerEntity();
        errorRecognizerExpected.setRecognize(recognizer);
        errorRecognizerExpected.setErrorMessage("Error message");
        errorRecognizerExpected.setDate(LocalDateTime.now());
        errorRecognizerExpected.setTrace("trace");
        errorRecognizerExpected.setInput("{}");
        errorRecognizerExpected.setWorkstationName(workstation.getName());
        errorRecognizerExpected.setOriginIp(workstation.getIp());

        errorRecognizerRepository.saveAndFlush(errorRecognizerExpected);

        given(specAuthentication)
            .port(port)
            .accept(APPLICATION_JSON_VALUE)
            .header("Accept-Language", Locale.forLanguageTag("en-US"))
            .pathParam("id", recognizer.getId())
            .expect()
            .statusCode(OK.value())
            .body("errorMessage", is(errorRecognizerExpected.getErrorMessage()))
            .body("workstationName", is(errorRecognizerExpected.getWorkstationName()))
            .body("originIp", is(errorRecognizerExpected.getOriginIp()))
            .body("input", is(errorRecognizerExpected.getInput()))
            .body("trace", is(errorRecognizerExpected.getTrace()))
            .body("date",
                containsString(errorRecognizerExpected.getDate().truncatedTo(MILLIS).toString()))
            .when()
            .get(URL_GET_ERROR_RECOGNIZERS);

        workstationRepository.delete(workstation);
        deleteUser(user);
    }

    @Test
    @ResourceLocks(value = {
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.ErrorRecognizerRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.RecognizerRepository")
    })
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
            .statusCode(NO_CONTENT.value())
            .when()
            .post(URL_RECOGNIZER);

        await()
            .with()
            .pollInterval(Duration.ofMillis(500))
            .atMost(ofMillis(5000)).until(() -> {
                var allError = errorRecognizerRepository.findAll();

                return allError.stream()
                    .anyMatch(error -> error.getErrorMessage()
                            .equals("Estação de trabalho para key key não encontrada"));
            });
    }

    @Test
    @ResourceLocks(value = {
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.ErrorRecognizerRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.RecognizerRepository")
    })
    @DisplayName("Deve salvar Error quando Estação de trabalho tem um ip diferente da requisição")
    void shouldSaveErrorWorkstationIpNotAllows() {
        var faker = Faker.instance();
        var key = faker.internet().password();
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
            .statusCode(NO_CONTENT.value())
            .when()
            .post(URL_RECOGNIZER);

        await()
            .with()
            .pollInterval(Duration.ofMillis(500))
            .atMost(ofMillis(3000)).until(() -> {
                var allError = errorRecognizerRepository.findAll();
                return allError.stream()
                    .anyMatch(error ->
                        error.getErrorMessage().equals(
                            "Ip de origem diferente do ip 127.000.000.001 vinculado a estação"));
            });

        workstationRepository.delete(workstation);
    }

    @Test
    @ResourceLocks(value = {
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.ErrorRecognizerRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.RecognizerRepository")
    })
    @DisplayName("Deve salvar Error quando reconhecimento está abaixo de 75.0 de confiabilidade")
    void shouldSaveErrorWorkstationConfidenceMinorLimits() {
        var faker = Faker.instance();
        var key = faker.internet().password();
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
            .statusCode(NO_CONTENT.value())
            .when()
            .post(URL_RECOGNIZER);

        await()
            .with()
            .pollInterval(Duration.ofMillis(1000))
            .atMost(ofMillis(3000)).until(() -> {
                var allError = errorRecognizerRepository.findAll();
                return allError.stream()
                    .anyMatch(error ->
                        error.getErrorMessage()
                            .equals("Reconhecimentos abaixo da confidencialidade [valor < 75.00]"));
            });

        workstationRepository.delete(workstation);
    }

    @ParameterizedTest
    @ResourceLocks(value = {
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.ErrorRecognizerRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.RecognizerRepository")
    })
    @MethodSource("providerListPlateInvalid")
    @DisplayName("Deve não fazer nada quando input de placa esta vazio")
    void shouldNothingWhenPlatesIsEmpty(InputRecognizerDto input) {
        var faker = Faker.instance();
        var key = faker.internet().password();

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
            .statusCode(NO_CONTENT.value())
            .when()
            .post(URL_RECOGNIZER);

        await().timeout(ofMillis(3000));

        var error = errorRecognizerRepository.findAll();
        var rec = recognizerRepository.findAll();

        assertThat(error).isEmpty();
        assertThat(rec).isEmpty();

        workstationRepository.delete(workstation);
    }

    @Test
    @ResourceLocks(value = {
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.ErrorRecognizerRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.RecognizerRepository")
    })
    @DisplayName("Deve retornar uma página de reconhecimentos")
    void shouldReturnPageRecognize() {
        var user = createAdmin();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var faker = Faker.instance();
        var key = "key";

        var workstation = new WorkstationEntity();
        workstation.setMode(ModeEnum.NONE.name());
        workstation.setName(faker.name().name());
        workstation.setIp(faker.internet().ipV4Address());
        workstation.setPort(9090);
        workstation.setKey(key);

        workstationRepository.saveAndFlush(workstation);

        var recognizerExpected = new RecognizeEntity();
        recognizerExpected.setConfidence(99.9F);
        recognizerExpected.setEpochTime(LocalDateTime.now());
        recognizerExpected.setPlate("plate");
        recognizerExpected.setOriginIp("ip");
        recognizerExpected.setDriverName("driveName");
        recognizerExpected.setHasError(FALSE);

        recognizerRepository.saveAndFlush(recognizerExpected);

        given(specAuthentication)
            .port(port)
            .accept(APPLICATION_JSON_VALUE)
            .header("Accept-Language", Locale.forLanguageTag("en-US"))
            .expect()
            .statusCode(OK.value())
            .body("size", is(5))
            .body("page", is(0))
            .body("sorted", is("CREATED"))
            .body("totalElements", is(1))
            .body("totalPage", is(1))
            .body("items[0].id", is(recognizerExpected.getId().intValue()))
            .body("items[0].originIp", is(recognizerExpected.getOriginIp()))
            .body("items[0].epochTime",
                containsString(recognizerExpected.getEpochTime().truncatedTo(SECONDS).toString()))
            .body("items[0].plate", is(recognizerExpected.getPlate()))
            .body("items[0].confidence", is(recognizerExpected.getConfidence()))
            .body("items[0].hasError", is(recognizerExpected.getHasError()))
            .body("items[0].hasError", is(recognizerExpected.getHasError()))
            .body("items[0].errorDetails", nullValue())
            .body("items[0].accessGranted", is(recognizerExpected.getAccessGranted()))
            .body("items[0].driverName", is(recognizerExpected.getDriverName()))
            .when()
            .get(URL_GET_ALL_RECOGNIZERS);

        workstationRepository.delete(workstation);
        deleteUser(user);
    }

    @Test
    @ResourceLocks(value = {
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.ErrorRecognizerRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.RecognizerRepository")
    })
    @DisplayName("Deve retornar uma página de reconhecimentos com o valor padrão de sorted")
    void shouldReturnPageRecognizeWhitDefaultSorted() {
        var user = createAdmin();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var faker = Faker.instance();
        var key = "key";

        var workstation = new WorkstationEntity();
        workstation.setMode(ModeEnum.NONE.name());
        workstation.setName(faker.name().name());
        workstation.setIp(faker.internet().ipV4Address());
        workstation.setPort(9090);
        workstation.setKey(key);

        workstationRepository.saveAndFlush(workstation);

        var recognizerExpected = new RecognizeEntity();
        recognizerExpected.setConfidence(99.9F);
        recognizerExpected.setEpochTime(LocalDateTime.now());
        recognizerExpected.setPlate("plate");
        recognizerExpected.setOriginIp("ip");
        recognizerExpected.setDriverName("driveName");
        recognizerExpected.setHasError(FALSE);

        recognizerRepository.saveAndFlush(recognizerExpected);

        given(specAuthentication)
            .port(port)
            .accept(APPLICATION_JSON_VALUE)
            .header("Accept-Language", Locale.forLanguageTag("en-US"))
            .queryParams("sorted", "other")
            .expect()
            .statusCode(OK.value())
            .body("size", is(5))
            .body("page", is(0))
            .body("sorted", is("CREATED"))
            .body("totalElements", is(1))
            .body("totalPage", is(1))
            .when()
            .get(URL_GET_ALL_RECOGNIZERS);

        workstationRepository.delete(workstation);
        deleteUser(user);
    }

    @Test
    @DisplayName("Deve retornar não autorizado para requisição de erro de reconhecimento quando operador")
    @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository")
    void shouldReturnUnauthorizedGetErrorOperator() {
        var user = createOperator();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        submitRequest();
    }

    @Test
    @DisplayName("Deve retornar não autorizado para requisição de erro de reconhecimento quando motorista")
    @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository")
    void shouldReturnUnauthorizedGetErrorDriver() {
        var user = createDriver();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        submitRequest();
    }

    private void submitRequest() {
        given(specAuthentication)
            .port(port)
            .accept(APPLICATION_JSON_VALUE)
            .header("Accept-Language", Locale.forLanguageTag("en-US"))
            .expect()
            .statusCode(FORBIDDEN.value())
            .when()
            .get(URL_GET_ALL_RECOGNIZERS);
    }
}
