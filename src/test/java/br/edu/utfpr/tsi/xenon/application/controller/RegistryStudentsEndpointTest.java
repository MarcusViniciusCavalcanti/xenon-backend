package br.edu.utfpr.tsi.xenon.application.controller;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.mapper.ObjectMapperType.JACKSON_2;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import br.edu.utfpr.tsi.xenon.AbstractContextTest;
import br.edu.utfpr.tsi.xenon.application.dto.InputRegistryStudentDto;
import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.factory.TypeUser;
import br.edu.utfpr.tsi.xenon.structure.MessagesMapper;
import br.edu.utfpr.tsi.xenon.structure.repository.AccessCardRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.RoleRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.UserRepository;
import com.github.javafaker.Faker;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.ResourceLocks;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("Test - Integration - Funcionalidade registro de usuário")
class RegistryStudentsEndpointTest extends AbstractContextTest {

    private static final String URL_REGISTRY = "/api/new-students/registry";
    private static final String URL_ACTIVE = "/api/activate-registry";

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private AccessCardRepository accessCardRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Deve criar usuário com sucesso")
    @ResourceLocks(value = {
        @ResourceLock(value = "br.edu.utfpr.tsi.xenon.structure.repository.UserRepository"),
        @ResourceLock(value = "br.edu.utfpr.tsi.xenon.structure.repository.AccessCardRepository"),
        @ResourceLock(value = "br.edu.utfpr.tsi.xenon.structure.repository.CarRepository"),
    })
    void shouldHaveRegisterUserSuccessfully() {
        var pass = faker.internet().password();
        var input = new InputRegistryStudentDto()
            .email(faker.bothify("registry-success-#####@alunos.utfpr.edu.br"))
            .name(faker.name().fullName())
            .modelCar(faker.rockBand().name())
            .plateCar(faker.bothify("???-#?##", TRUE))
            .password(pass)
            .confirmPassword(pass);

        given()
            .port(port)
            .accept(APPLICATION_JSON_VALUE)
            .header("Accept-Language", Locale.forLanguageTag("pt_BR"))
            .contentType(JSON)
            .body(input, JACKSON_2)
            .expect()
            .body("id", notNullValue())
            .body("email", is(input.getEmail()))
            .body("type", is(TypeUser.STUDENTS.name()))
            .body("avatar", containsString("/defaultUser.png"))
            .body("cars[0].id", notNullValue())
            .body("cars[0].modelCar", is(input.getModelCar()))
            .body("cars[0].plateCar", is(input.getPlateCar()))
            .body("cars[0].document", nullValue())
            .body("cars[0].lastAcess", nullValue())
            .body("cars[0].numberAccess", is(0))
            .body("roles[0].id", is(1))
            .body("roles[0].name", is("ROLE_DRIVER"))
            .body("roles[0].description", is("Perfil Motorista"))
            .when()
            .post(URL_REGISTRY);

    }

    @Test
    @DisplayName("Deve retornar error de validações de campos")
    void shouldReturnErrorBadRequest() {
        var message =
            messageSource.getMessage(MessagesMapper.ARGUMENT_INVALID.getCode(), null,
                Locale.forLanguageTag("pt_BR"));

        var input = new InputRegistryStudentDto();

        given()
            .port(port)
            .accept(APPLICATION_JSON_VALUE)
            .header("Accept-Language", Locale.forLanguageTag("pt_BR"))
            .contentType(JSON)
            .body(input, JACKSON_2)
            .expect()
            .statusCode(BAD_REQUEST.value())
            .body("message", is(message))
            .body("statusCode", is(400))
            .body("path", is("/new-students/registry"))
            .body("details[].field",
                everyItem(hasItems("email", "name", "password", "confirmPassword", "modelCar",
                    "plateCar")))
            .when()
            .post(URL_REGISTRY);
    }

    @Test
    @DisplayName("Deve retornar erro que placa do carro já cadastrada")
    @ResourceLock(value = "br.edu.utfpr.tsi.xenon.structure.repository.CarRepository")
    void shouldReturnErrorPlateExist() {
        var email = faker.bothify("plate-exist-####@alunos.utfpr.edu.br");
        var pass = faker.regexify("????????");
        var plateCar = faker.bothify("???-####", Boolean.TRUE);
        var modelCar = faker.rockBand().name();
        var input = new InputRegistryStudentDto()
            .email(email)
            .name(faker.name().fullName())
            .password(pass)
            .confirmPassword(pass)
            .modelCar(modelCar)
            .plateCar(plateCar);

        var user = new UserEntity();
        user.setId(1L);
        var carEntity = new CarEntity();
        carEntity.setPlate(plateCar);
        carEntity.setNumberAccess(0);
        carEntity.setModel(modelCar);
        carEntity.setDocument("");
        carEntity.setLastAccess(LocalDateTime.now());

        carRepository.saveAndFlush(carEntity);
        var message = messageSource.getMessage(
            MessagesMapper.PLATE_ALREADY.getCode(),
            new String[] {plateCar},
            Locale.getDefault()
        );

        given()
            .port(port)
            .accept(APPLICATION_JSON_VALUE)
            .contentType(JSON)
            .header("Accept-Language", Locale.getDefault().getLanguage())
            .body(input, JACKSON_2)
            .expect()
            .statusCode(CONFLICT.value())
            .body("message", is(message))
            .body("statusCode", is(CONFLICT.value()))
            .body("path", is("/new-students/registry"))
            .when()
            .post(URL_REGISTRY);
    }

    @Test
    @DisplayName("Deve retornar error quando e-mail já está em uso")
    @ResourceLock(value = "br.edu.utfpr.tsi.xenon.structure.repository.AccessCardRepository")
    void shouldReturnErrorWhenEmailExist() {
        var email = faker.bothify("email-exist-######@alunos.utfpr.edu.br");
        var pass = faker.internet().password();

        var accessCardEntity = new AccessCardEntity();
        accessCardEntity.setUsername(email);
        accessCardEntity.setPassword(pass);
        var realAccessCard = accessCardRepository.saveAndFlush(accessCardEntity);

        var input = new InputRegistryStudentDto()
            .email(email)
            .name(faker.name().fullName())
            .password(pass)
            .confirmPassword(pass)
            .modelCar(faker.rockBand().name())
            .plateCar(faker.bothify("???-####", Boolean.TRUE));

        var message = messageSource
            .getMessage(MessagesMapper.EMAIL_EXIST.getCode(),
                new String[] {input.getEmail()}, Locale.getDefault());

        given()
            .port(port)
            .accept(APPLICATION_JSON_VALUE)
            .contentType(JSON)
            .header("Accept-Language", Locale.getDefault().getLanguage())
            .body(input, JACKSON_2)
            .expect()
            .statusCode(CONFLICT.value())
            .body("message", is(message))
            .body("statusCode", is(CONFLICT.value()))
            .body("path", is("/new-students/registry"))
            .when()
            .post(URL_REGISTRY);

        accessCardRepository.deleteById(realAccessCard.getId());
    }

    @Test
    @DisplayName("Deve ativar conta")
    @ResourceLocks(value = {
        @ResourceLock(value = "br.edu.utfpr.tsi.xenon.structure.repository.UserRepository"),
        @ResourceLock(
            value = "br.edu.utfpr.tsi.xenon.structure.repository.RoleRepository",
            mode = ResourceAccessMode.READ
        )
    })
    void shouldHaveActiveAccount() {
        var faker = Faker.instance();
        var email = faker.internet().emailAddress();

        var user = new UserEntity();
        user.setTypeUser(TypeUser.STUDENTS.name());
        user.setName(faker.name().fullName());
        user.setAvatar(faker.internet().avatar());
        user.setAuthorisedAccess(FALSE);

        var accessCard = new AccessCardEntity();
        accessCard.setEnabled(FALSE);
        accessCard.setPassword(faker.internet().password());
        accessCard.setUsername(email);
        accessCard.setUser(user);

        var roles = roleRepository.findById(1L).get();
        accessCard.setRoleEntities(List.of(roles));
        user.setAccessCard(accessCard);

        var userSaved = userRepository.saveAndFlush(user);
        var params = new String(
            Base64.getEncoder().encode(email.getBytes(UTF_8)),
            UTF_8
        );

        var message = messageSource
            .getMessage(MessagesMapper.ACTIVATE_ACCOUNT.getCode(),
                new String[0], Locale.getDefault());

        given()
            .port(port)
            .accept(APPLICATION_JSON_VALUE)
            .contentType(JSON)
            .header("Accept-Language", Locale.getDefault().getLanguage())
            .queryParam("params", params)
            .expect()
            .statusCode(ACCEPTED.value())
            .body("result", is(message))
            .when()
            .get(URL_ACTIVE);

        var userUpdated = userRepository.findById(userSaved.getId()).get();
        assertTrue(userUpdated.getAccessCard().isEnabled());
        assertTrue(userUpdated.getAuthorisedAccess());

        userRepository.delete(userUpdated);
    }

}