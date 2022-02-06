package br.edu.utfpr.tsi.xenon.application.controller;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.mapper.ObjectMapperType.JACKSON_2;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import br.edu.utfpr.tsi.xenon.AbstractSecurityContext;
import br.edu.utfpr.tsi.xenon.application.dto.InputChangePasswordDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputLoginDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputNameUserDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputNewCarDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputRemoveCarDto;
import br.edu.utfpr.tsi.xenon.application.dto.UserDto.TypeEnum;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarStatus;
import br.edu.utfpr.tsi.xenon.domain.user.state.CarState.CarStateName;
import br.edu.utfpr.tsi.xenon.structure.MessagesMapper;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.github.javafaker.Faker;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

@DisplayName("Test - Integration - Funcionalidade de Perfil de usuário")
class ProfileEndpointTest extends AbstractSecurityContext {

    private static final String URL_ME = "/api/profile/me";
    private static final String URL_CHANGE_NAME = "/api/profile/change-name";
    private static final String URL_INCLUDE_NEW_CAR = "/api/profile/include-new-car";
    private static final String URL_REMOVE_CAR = "/api/profile/remove-car";
    private static final String URL_CHANGE_PASSWORD = "/api/profile/change-password";
    private static final String URL_DISABLE_ACCOUNT = "/api/profile/disable-account";
    private static final String URL_INCLUDE_AVATAR = "/api/profile/avatar";
    private static final String URL_INCLUDE_DOCUMENT = "/api/profile/car/{id}/document";

    @Autowired
    private CarRepository carRepository;

    @MockBean
    private Cloudinary cloudinary;

    private static Stream<Arguments> providerCarInvalid() {
        var plateCar = new String[] {"plateCar"};
        var modelCar = new String[] {"modelCar"};
        var modelCarAndPlate = new String[] {"modelCar", "modelCar"};
        return Stream.of(
            Arguments.of(new InputNewCarDto().plate("").model("model"), plateCar),
            Arguments.of(new InputNewCarDto().plate(" ").model("model"), plateCar),
            Arguments.of(new InputNewCarDto().plate(null).model("model"), plateCar),
            Arguments.of(new InputNewCarDto().plate("invalid").model("model"), plateCar),
            Arguments.of(new InputNewCarDto().model(null).plate("abc-4455"), modelCar),
            Arguments.of(new InputNewCarDto().model(" ").plate("abc-4456"), modelCar),
            Arguments.of(new InputNewCarDto().model("").plate("abc-4457"), modelCar),
            Arguments.of(new InputNewCarDto().model("").plate(""), modelCarAndPlate),
            Arguments.of(new InputNewCarDto().model(" ").plate(" "), modelCarAndPlate),
            Arguments.of(new InputNewCarDto().model(null).plate(null), modelCarAndPlate),
            Arguments.of(new InputNewCarDto().model("").plate(null), modelCarAndPlate),
            Arguments.of(new InputNewCarDto().model(null).plate(""), modelCarAndPlate),
            Arguments.of(new InputNewCarDto().model("        ").plate(null), modelCarAndPlate),
            Arguments.of(new InputNewCarDto().model(null).plate("        "), modelCarAndPlate)
        );
    }

    private static Stream<Arguments> providerCarPlateInvalid() {
        var plate = new String[] {"plate"};
        return Stream.of(
            Arguments.of(new InputRemoveCarDto().plate(""), plate),
            Arguments.of(new InputRemoveCarDto().plate("  "), plate),
            Arguments.of(new InputRemoveCarDto().plate(" "), plate),
            Arguments.of(new InputRemoveCarDto().plate("invalid"), plate),
            Arguments.of(new InputRemoveCarDto().plate("ab12333"), plate),
            Arguments.of(new InputRemoveCarDto().plate("aaaaaaaa"), plate),
            Arguments.of(new InputRemoveCarDto().plate("abc123456789"), plate),
            Arguments.of(new InputRemoveCarDto().plate("abc--1234"), plate),
            Arguments.of(new InputRemoveCarDto().plate("abc-12f4"), plate),
            Arguments.of(new InputRemoveCarDto().plate(null), plate)
        );
    }

    private static Stream<Arguments> providerPasswordInvalid() {
        var confirmPassword = new String[] {"confirmPassword"};
        var password = new String[] {"password"};
        var passwordAndConfirm = new String[] {"password", "confirmPassword"};
        return Stream.of(
            Arguments.of(new InputChangePasswordDto().actualPassword(PASS).confirmPassword("")
                .password("12345670"), confirmPassword),
            Arguments.of(new InputChangePasswordDto().actualPassword(PASS).confirmPassword("  ")
                .password("12345670"), confirmPassword),
            Arguments.of(new InputChangePasswordDto().actualPassword(PASS).confirmPassword(null)
                .password("12345670"), confirmPassword),
            Arguments.of(new InputChangePasswordDto().actualPassword(PASS).confirmPassword(
                "12345678909876543").password("12345670"), confirmPassword),
            Arguments.of(new InputChangePasswordDto().actualPassword(PASS).confirmPassword(
                "1234567").password("12345670"), confirmPassword),
            Arguments.of(new InputChangePasswordDto().actualPassword(PASS).confirmPassword(
                "87654321").password(""), password),
            Arguments.of(new InputChangePasswordDto().actualPassword(PASS).confirmPassword("   ")
                .password("12345670"), password),
            Arguments.of(new InputChangePasswordDto().actualPassword(PASS).confirmPassword(
                "1234567").password("12345678909876543"), confirmPassword),
            Arguments.of(new InputChangePasswordDto().actualPassword(PASS).confirmPassword(
                "1234567").password("12345670"), confirmPassword),
            Arguments.of(new InputChangePasswordDto().actualPassword(PASS).confirmPassword(null)
                .password("12345670"), confirmPassword),
            Arguments.of(new InputChangePasswordDto().actualPassword(PASS).confirmPassword(
                "1234567").password("1234567"), confirmPassword),
            Arguments.of(new InputChangePasswordDto().actualPassword("").confirmPassword(
                "12345679").password("12345679"), confirmPassword),
            Arguments.of(new InputChangePasswordDto().actualPassword("   ").confirmPassword(
                "12345679").password("12345679"), confirmPassword),
            Arguments.of(new InputChangePasswordDto().actualPassword("1234567").confirmPassword(
                "12345679").password("12345679"), confirmPassword),
            Arguments.of(
                new InputChangePasswordDto().actualPassword("12345678909876543").confirmPassword(
                    "12345679").password("12345679"), confirmPassword),
            Arguments.of(new InputChangePasswordDto().actualPassword(null).confirmPassword(
                "12345679").password("12345679"), confirmPassword),
            Arguments.of(new InputChangePasswordDto().actualPassword(null).confirmPassword(null)
                .password(null), passwordAndConfirm)

        );
    }

    @Test
    @DisplayName("Deve retornar usuário do contexto de segurança 'dono do token'")
    @ResourceLock(value = "br.edu.utfpr.tsi.xenon.structure.repository.UserRepository")
    void shouldReturnUserContextSecurity() {
        var user = createAdmin();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", Locale.getDefault().getLanguage())
            .contentType(JSON)
            .expect()
            .statusCode(OK.value())
            .body("id", notNullValue())
            .body("name", is(user.getName()))
            .body("email", is(user.getAccessCard().getUsername()))
            .body("avatar", is(user.getAvatar()))
            .body("type", is(TypeEnum.SERVICE.name()))
            .when()
            .get(URL_ME);

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve alterar nome com sucesso")
    void shouldHaveChangeName() {
        var user = createOperator();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var message = messageSource.getMessage(
            MessagesMapper.NAME_CHANGED_SUCCESSFULLY.getCode(),
            new String[0],
            Locale.getDefault()
        );

        var inputChangeName = new InputNameUserDto().name(Faker.instance().name().fullName());

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", Locale.getDefault().getLanguage())
            .contentType(JSON)
            .body(inputChangeName, JACKSON_2)
            .expect()
            .statusCode(OK.value())
            .body("result", is(message))
            .when()
            .patch(URL_CHANGE_NAME);

        //noinspection OptionalGetWithoutIsPresent
        var userUpdated = userRepository.findById(user.getId()).get();
        assertEquals(inputChangeName.getName(), userUpdated.getName());

        deleteUser(user);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "  ",
        "12345678909876543212345678901234567890987654321234567890123456789098765432123456789"
        + "0123456789098765432123456789012345678909876543212345678901234567890987654321234567"
        + "89012345678909876543212345678901234567890987654321234567890123456789098765432123456"
        + "78901234567890987654321234567890123456789098765432123456789012345678909876543212345"
        + "6789012345678909876543212345678901234567890987654321234567890123456789098765432123"
        + "45678901234567890987654321234567890123456789098765432123456789012345678909876543212"
        + "34567890123456789098765432123456789012345678909876543212345678901234567890987654321"
        + "23456789012345678909876543212345678901234567890987654321234567890123456789098765432"
        + "12345678901234567890987654321234567890123456789098765432123456789012345678909876543"
        + "12345678901234567890987654321234567890123456789098765432123456789012345678909876543"
        + "12345678901234567890987654321234567890123456789098765432123456789012345678909876543"
        + "12345678901234567890987654321234567890123456789098765432123456789012345678909876543"
        + "12345678901234567890987654321234567890123456789098765432123456789012345678909876543"
        + "2123456789012345678909876543212345678901234567890987654321234567890",
        "1234"
    })
    @NullSource
    @DisplayName("Deve retornar Bad request quand nome está invalido")
    void shouldHReturnBadqreqyestWhenChangeNameIsInvalid(String name) {
        var user = createOperator();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var message = messageSource.getMessage(
            MessagesMapper.ARGUMENT_INVALID.getCode(),
            new String[0],
            Locale.getDefault()
        );

        var inputChangeName = new InputNameUserDto().name(name);

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", Locale.getDefault().getLanguage())
            .contentType(JSON)
            .body(inputChangeName, JACKSON_2)
            .expect()
            .statusCode(BAD_REQUEST.value())
            .body("message", is(message))
            .body("statusCode", is(400))
            .body("path", is("/profile/change-name"))
            .body("details[].field", everyItem(hasItems("name")))
            .when()
            .patch(URL_CHANGE_NAME);

        //noinspection OptionalGetWithoutIsPresent
        var userEntity = userRepository.findById(user.getId()).get();
        assertEquals(user.getName(), userEntity.getName());

        deleteUser(user);
    }

    @ParameterizedTest
    @MethodSource("providerCarInvalid")
    @DisplayName("Deve retornar bad request quando tentar incluir um novo carro")
    void shouldReturnBadRequestWhenIncludeNewCar(InputNewCarDto inputChangeName, String[] fields) {
        var user = createOperator();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var message = messageSource.getMessage(
            MessagesMapper.ARGUMENT_INVALID.getCode(),
            new String[0],
            Locale.getDefault()
        );

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", Locale.getDefault().getLanguage())
            .contentType(JSON)
            .body(inputChangeName, JACKSON_2)
            .expect()
            .statusCode(BAD_REQUEST.value())
            .body("message", is(message))
            .body("statusCode", is(400))
            .body("path", is("/profile/include-new-car"))
            .body("details[].field", everyItem(hasItems(fields)))
            .when()
            .patch(URL_INCLUDE_NEW_CAR);

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve retornar conflict quando tentar incluir um novo é a placa já existe")
    void shouldReturnConflictWhenIncludeNeCarButPlateExist() {
        var user = createDriver();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        user.setId(1L);
        var carEntity = new CarEntity();
        var plate = faker.bothify("???-####", TRUE);
        carEntity.setPlate(plate);
        carEntity.setNumberAccess(0);
        carEntity.setModel("Model");
        carEntity.setDocument("");
        carEntity.setLastAccess(LocalDateTime.now());

        var inputNewCarDto = new InputNewCarDto()
            .plate(plate)
            .model("Model Car");

        carRepository.saveAndFlush(carEntity);
        var message = messageSource.getMessage(
            MessagesMapper.PLATE_ALREADY.getCode(),
            new String[] {plate},
            Locale.getDefault()
        );

        given(specAuthentication)
            .port(port)
            .accept(APPLICATION_JSON_VALUE)
            .contentType(JSON)
            .header("Accept-Language", Locale.getDefault().getLanguage())
            .body(inputNewCarDto, JACKSON_2)
            .expect()
            .statusCode(CONFLICT.value())
            .body("message", is(message))
            .body("statusCode", is(CONFLICT.value()))
            .body("path", is("/profile/include-new-car"))
            .when()
            .patch(URL_INCLUDE_NEW_CAR);

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve deve incluir carro com sucesso")
    @ResourceLock(value = "br.edu.utfpr.tsi.xenon.structure.repository.CarRepository")
    void shouldHaveIncludeNewCarSuccessfully() {
        var user = createOperator();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var faker = Faker.instance();
        var inputNewCarDto = new InputNewCarDto()
            .model(faker.rockBand().name())
            .plate(faker.bothify("???-####", TRUE));

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", Locale.getDefault().getLanguage())
            .contentType(JSON)
            .body(inputNewCarDto, JACKSON_2)
            .expect()
            .statusCode(OK.value())
            .body("id", notNullValue())
            .body("modelCar", is(inputNewCarDto.getModel()))
            .body("plateCar", is(inputNewCarDto.getPlate()))
            .body("document", nullValue())
            .body("numberAccess", is(0))
            .when()
            .patch(URL_INCLUDE_NEW_CAR);

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve retornar erro quando cadastro de carro atingiu o limite de 5")
    void shouldReturnErrorWhenRegisterCarExcceded() {
        var user = createOperator();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var cars = IntStream.range(0, 5)
            .mapToObj(index -> {
                var car = new CarEntity();
                car.setPlate(faker.bothify("???-####", TRUE));
                car.setModel("Gol 1.0");
                user.getCar().add(car);
                car.setUser(user);
                car.setCarStatus(CarStatus.APPROVED);
                car.setState(CarStateName.APPROVED.name());
                return car;
            }).toList();

        carRepository.saveAllAndFlush(cars);

        var faker = Faker.instance();
        var inputNewCarDto = new InputNewCarDto()
            .model(faker.rockBand().name())
            .plate(faker.bothify("???-####", TRUE));

        var message = messageSource.getMessage(
            MessagesMapper.LIMIT_EXCEEDED_CAR.getCode(),
            new String[] {inputNewCarDto.getPlate()},
            Locale.getDefault()
        );

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", Locale.getDefault().getLanguage())
            .contentType(JSON)
            .body(inputNewCarDto, JACKSON_2)
            .expect()
            .statusCode(UNPROCESSABLE_ENTITY.value())
            .body("statusCode", is(UNPROCESSABLE_ENTITY.value()))
            .body("message", is(message))
            .body("path", is("/profile/include-new-car"))
            .when()
            .patch(URL_INCLUDE_NEW_CAR).andReturn().prettyPrint();

        deleteUser(user);
        carRepository.deleteAll();
    }

    @ParameterizedTest
    @MethodSource("providerCarPlateInvalid")
    @DisplayName("Não deve remover carro com quando placa informa esta invalida")
    @ResourceLock(value = "br.edu.utfpr.tsi.xenon.structure.repository.CarRepository")
    void shouldHNotaveRemoveCar(InputRemoveCarDto inputRemoveCar, String[] fields) {
        var user = createOperator();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var message = messageSource.getMessage(
            MessagesMapper.ARGUMENT_INVALID.getCode(),
            new String[0],
            Locale.getDefault()
        );

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", Locale.getDefault().getLanguage())
            .contentType(JSON)
            .body(inputRemoveCar, JACKSON_2)
            .expect()
            .statusCode(BAD_REQUEST.value())
            .body("message", is(message))
            .body("statusCode", is(400))
            .body("path", is("/profile/remove-car"))
            .body("details[].field", everyItem(hasItems(fields)))
            .when()
            .delete(URL_REMOVE_CAR);

        var cars = carRepository.findByUser(user);
        assertFalse(cars.stream()
            .anyMatch(carEntity -> carEntity.getPlate().equals(inputRemoveCar.getPlate())));

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve remover carro com sucesso")
    @ResourceLock(value = "br.edu.utfpr.tsi.xenon.structure.repository.CarRepository")
    void shouldHaveRemoveCarSuccessfully() {
        var user = createOperator();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var inputRemoveCar = new InputRemoveCarDto()
            .plate(user.firstCar().getPlate());

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", Locale.getDefault().getLanguage())
            .contentType(JSON)
            .body(inputRemoveCar, JACKSON_2)
            .expect()
            .statusCode(NO_CONTENT.value())
            .when()
            .delete(URL_REMOVE_CAR);

        var cars = carRepository.findByUser(user);
        assertFalse(cars.stream()
            .anyMatch(carEntity -> carEntity.getPlate().equals(inputRemoveCar.getPlate())));

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve alterar a senha com sucesso")
    void shouldHaveChangePasswordSuccessfully() {
        var user = createDriver();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var message = messageSource.getMessage(
            MessagesMapper.CHANGE_PASS_SUCCESSFULLY.getCode(),
            new String[0],
            Locale.getDefault()
        );

        var pass = Faker.instance().internet().password();
        var inputChangePass = new InputChangePasswordDto()
            .password(pass)
            .confirmPassword(pass)
            .actualPassword(PASS);

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", Locale.getDefault().getLanguage())
            .contentType(JSON)
            .body(inputChangePass, JACKSON_2)
            .expect()
            .statusCode(ACCEPTED.value())
            .body("result", is(message))
            .when()
            .post(URL_CHANGE_PASSWORD);

        deleteUser(user);
    }

    @ParameterizedTest
    @MethodSource("providerPasswordInvalid")
    @DisplayName("Deve retornar bad request quando o input esta invalido")
    void shouldReturnBadrequestWhenInputIsInvalid(
        InputChangePasswordDto inputChangePass,
        String[] fields) {
        var user = createDriver();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var message = messageSource.getMessage(
            MessagesMapper.ARGUMENT_INVALID.getCode(),
            new String[0],
            Locale.getDefault()
        );

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", Locale.getDefault().getLanguage())
            .contentType(JSON)
            .body(inputChangePass, JACKSON_2)
            .expect()
            .statusCode(BAD_REQUEST.value())
            .body("message", is(message))
            .body("statusCode", is(400))
            .body("path", is("/profile/change-password"))
            .body("details[].field", everyItem(hasItems(fields)))
            .when()
            .post(URL_CHANGE_PASSWORD);

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve retornar erro quando senha atual não coincide")
    void shouldReturnErrorActualPassNotMatch() {
        var user = createDriver();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var message = messageSource.getMessage(
            MessagesMapper.PASS_ACTUAL_NOT_MATCH.getCode(),
            new String[0],
            Locale.getDefault()
        );

        var pass = Faker.instance().internet().password();
        var inputChangePass = new InputChangePasswordDto()
            .password(pass)
            .confirmPassword(pass)
            .actualPassword("87654321");

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", Locale.getDefault().getLanguage())
            .contentType(JSON)
            .body(inputChangePass, JACKSON_2)
            .expect()
            .statusCode(UNPROCESSABLE_ENTITY.value())
            .body("message", is(message))
            .body("statusCode", is(422))
            .body("path", is("/profile/change-password"))
            .when()
            .post(URL_CHANGE_PASSWORD);
    }

    @Test
    @DisplayName("Deve retornar erro quando senha e a confirmação de senha não coincide")
    void shouldReturnErrorActualPassAndConfirmPassNotMatch() {
        var user = createDriver();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var message = messageSource.getMessage(
            MessagesMapper.PASS_AND_CONFIRM_NOT_MATCH.getCode(),
            new String[0],
            Locale.getDefault()
        );

        var pass = Faker.instance().internet().password();
        var inputChangePass = new InputChangePasswordDto()
            .password(pass)
            .confirmPassword("999999999999999")
            .actualPassword(PASS);

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", Locale.getDefault().getLanguage())
            .contentType(JSON)
            .body(inputChangePass, JACKSON_2)
            .expect()
            .statusCode(UNPROCESSABLE_ENTITY.value())
            .body("message", is(message))
            .body("statusCode", is(422))
            .body("path", is("/profile/change-password"))
            .when()
            .post(URL_CHANGE_PASSWORD);

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve desativar conta com sucesso")
    @ResourceLock(value = "br.edu.utfpr.tsi.xenon.structure.repository.CarRepository")
    void shouldHaveDisableAccount() {
        var user = createDriver();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", Locale.getDefault().getLanguage())
            .contentType(JSON)
            .expect()
            .statusCode(NO_CONTENT.value())
            .when()
            .delete(URL_DISABLE_ACCOUNT);

        var cars = carRepository.findByUser(user);
        assertTrue(cars.isEmpty());

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve incluir avatar com sucesso")
    void shouldHaveIncludeUserSuccessFully() throws URISyntaxException, IOException {
        var file = Paths.get(Objects
            .requireNonNull(
                this.getClass().getResource("/test-file/avatar-extention-test/valid/file-png.png"))
            .toURI());

        var user = createDriver();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var uploader = mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(), any()))
            .thenReturn(Map.of("url", "url"));

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .multiPart(file.toFile())
            .expect()
            .statusCode(OK.value())
            .body("avatar", is("url"))
            .when()
            .post(URL_INCLUDE_AVATAR);

        verify(uploader).upload(any(), any());

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve incluir arquivo com sucesso")
    @ResourceLock(value = "br.edu.utfpr.tsi.xenon.structure.repository.CarRepository")
    void shouldHaveIncludeDocumento() throws URISyntaxException, IOException {
        var file = Paths.get(Objects
            .requireNonNull(
                this.getClass().getResource("/test-file/document-extention-test/valid/valid.pdf"))
            .toURI());

        var user = createDriver();
        var car = new CarEntity();
        car.setCarStatus(CarStatus.WAITING);
        car.setAuthorisedAccess(Boolean.FALSE);
        car.setState("WAITING_DOCUMENT");
        car.setUser(user);
        car.setPlate(faker.bothify("###-???", TRUE));
        car.setModel("MODEL");

        carRepository.saveAndFlush(car);

        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);
        var uploader = mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(), any()))
            .thenReturn(Map.of("public_id", "public_id"));

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .multiPart(file.toFile())
            .pathParam("id", car.getId())
            .expect()
            .statusCode(NO_CONTENT.value())
            .when()
            .post(URL_INCLUDE_DOCUMENT);

        //noinspection OptionalGetWithoutIsPresent
        var carEntity = carRepository.findById(car.getId()).get();

        assertEquals(CarStatus.WAITING, carEntity.getCarStatus());
        assertEquals("WAITING_DECISION", carEntity.getState());

        verify(uploader).upload(any(), any());

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve retornar error arquivo é invalido")
    @ResourceLock(value = "br.edu.utfpr.tsi.xenon.structure.repository.CarRepository")
    void shouldReturnErrorHaveIncludeDocumento() throws URISyntaxException {
        var file = Paths.get(Objects
            .requireNonNull(
                this.getClass()
                    .getResource("/test-file/document-extention-test/invalid/doc-x.docx"))
            .toURI());

        var user = createDriver();
        var car = new CarEntity();
        car.setCarStatus(CarStatus.WAITING);
        car.setAuthorisedAccess(Boolean.FALSE);
        car.setState("WAITING_DOCUMENT");
        car.setUser(user);
        car.setPlate(faker.bothify("###-???", TRUE));
        car.setModel("MODEL");

        carRepository.saveAndFlush(car);

        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var message = messageSource.getMessage(
            MessagesMapper.FILE_ALLOWED.getCode(),
            new String[] {"pdf"},
            Locale.US
        );

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .multiPart(file.toFile())
            .pathParam("id", car.getId())
            .expect()
            .statusCode(BAD_REQUEST.value())
            .body("message", is(message))
            .body("statusCode", is(400))
            .body("path", is("/profile/car/%d/document".formatted(car.getId())))
            .when()
            .post(URL_INCLUDE_DOCUMENT);

        //noinspection OptionalGetWithoutIsPresent
        var carEntity = carRepository.findById(car.getId()).get();

        assertEquals(CarStatus.WAITING, carEntity.getCarStatus());
        assertEquals("WAITING_DOCUMENT", carEntity.getState());

        verify(cloudinary, never()).uploader();

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve retornar error quando store file retorna error")
    @ResourceLock(value = "br.edu.utfpr.tsi.xenon.structure.repository.CarRepository")
    void shouldReturnErrorHaveIncludeDocumentoWhenClouadnaryReturnError()
        throws URISyntaxException, IOException {
        var file = Paths.get(Objects
            .requireNonNull(
                this.getClass()
                    .getResource("/test-file/document-extention-test/valid/valid.pdf"))
            .toURI());

        var user = createDriver();
        var car = new CarEntity();
        car.setCarStatus(CarStatus.WAITING);
        car.setAuthorisedAccess(Boolean.FALSE);
        car.setState("WAITING_DOCUMENT");
        car.setUser(user);
        car.setPlate(faker.bothify("###-???", TRUE));
        car.setModel("MODEL");

        carRepository.saveAndFlush(car);

        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var uploader = mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(), any())).thenThrow(IOException.class);

        var message = messageSource.getMessage(
            MessagesMapper.KNOWN.getCode(),
            new String[0],
            Locale.US
        );

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .multiPart(file.toFile())
            .pathParam("id", car.getId())
            .expect()
            .statusCode(UNPROCESSABLE_ENTITY.value())
            .body("message", is(message))
            .body("statusCode", is(422))
            .body("path", is("/profile/car/%d/document".formatted(car.getId())))
            .when()
            .post(URL_INCLUDE_DOCUMENT);

        //noinspection OptionalGetWithoutIsPresent
        var carEntity = carRepository.findById(car.getId()).get();

        assertEquals(CarStatus.WAITING, carEntity.getCarStatus());
        assertEquals("WAITING_DOCUMENT", carEntity.getState());

        deleteUser(user);
    }
}
