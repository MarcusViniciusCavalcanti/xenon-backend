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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import br.edu.utfpr.tsi.xenon.AbstractSecurityContext;
import br.edu.utfpr.tsi.xenon.application.dto.InputChangePasswordDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputLoginDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputNameUserDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputNewCarDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputRemoveCarDto;
import br.edu.utfpr.tsi.xenon.application.dto.UserDto.TypeEnum;
import br.edu.utfpr.tsi.xenon.structure.MessagesMapper;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.github.javafaker.Faker;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.mockito.Mockito;
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

    @Autowired
    private CarRepository carRepository;

    @MockBean
    private Cloudinary cloudinary;

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

    @Test
    @DisplayName("Deve retornar bad request quando tentar incluir um novo carro")
    void shouldReturnBadRequestWhenIncludeNewCar() {
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

        var inputChangeName = new InputNewCarDto();

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
            .body("details[].field", everyItem(hasItems("modelCar", "plateCar")))
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
    @DisplayName("Deve remover carro com sucesso")
    @ResourceLock(value = "br.edu.utfpr.tsi.xenon.structure.repository.CarRepository")
    void shouldHaveRemoveCarSuccessfully() {
        var user = createOperator();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var inputRemoveCar = new InputRemoveCarDto()
            .plate(user.getCar().get(0).getPlate());

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
                this.getClass().getResource("/test-file/extention-test/valid/file-png.png"))
            .toURI());

        var user = createDriver();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var uploader = Mockito.mock(Uploader.class);
        Mockito.when(cloudinary.uploader()).thenReturn(uploader);
        Mockito.when(uploader.upload(Mockito.any(), Mockito.any()))
            .thenReturn(Map.of("url", "url"));

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .multiPart(file.toFile())
            .expect()
            .statusCode(OK.value())
            .body("avatar", is("url"))
            .when()
            .post(URL_INCLUDE_AVATAR);

        Mockito.verify(uploader).upload(Mockito.any(), Mockito.any());

        deleteUser(user);
    }
}
