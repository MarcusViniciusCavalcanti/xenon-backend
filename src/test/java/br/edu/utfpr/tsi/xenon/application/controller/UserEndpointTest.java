package br.edu.utfpr.tsi.xenon.application.controller;

import static br.edu.utfpr.tsi.xenon.application.dto.InputUserDto.TypeUserEnum.SERVICE;
import static br.edu.utfpr.tsi.xenon.application.dto.InputUserDto.TypeUserEnum.SPEAKER;
import static br.edu.utfpr.tsi.xenon.application.dto.InputUserDto.TypeUserEnum.STUDENTS;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.ACCESS_DENIED;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.ADD_AUTHORIZATION_ACCESS;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.ARGUMENT_INVALID;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.EMAIL_NOT_INSTITUTIONAL;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.REMOVE_AUTHORIZATION_ACCESS;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.USER_ACCOUNT_DEACTIVATED;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.mapper.ObjectMapperType.JACKSON_2;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import br.edu.utfpr.tsi.xenon.AbstractSecurityContext;
import br.edu.utfpr.tsi.xenon.application.dto.InputAccessUserDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputLoginDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputUpdateUserDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputUserDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputUserDto.TypeUserEnum;
import br.edu.utfpr.tsi.xenon.application.dto.UserDto;
import br.edu.utfpr.tsi.xenon.domain.recognize.entity.RecognizeEntity;
import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.domain.security.entity.RoleEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarStatus;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.factory.TypeUser;
import br.edu.utfpr.tsi.xenon.structure.MessagesMapper;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.RecognizerRepository;
import com.github.javafaker.Faker;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.ResourceLocks;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;

@DisplayName("Test - Integration - Funcionalidade de Usuários")
class UserEndpointTest extends AbstractSecurityContext {

    private static final String URL_USER = "/api/users";
    private static final String URL_USER_WITH_ID = "/api/users/{id}";
    private static final String URL_USER_ALL = "/api/users/all";
    private static final String URL_USER_REMOVE_AUTHORIZATION = "/api/users/disabled/access";
    private static final String URL_USER_ADD_AUTHORIZATION = "/api/users/enabled/access";
    private static final String URL_USER_APPROVED_CAR = "/api/users/car/{id}/approved";
    private static final String URL_USER_REPROVED_CAR = "/api/users/car/{id}/reproved";
    private static final String URL_USER_CARS = "/api/users/{id}/cars";
    private static final String URL_USER_CARS_ACCESS = "/api/users/car/{id}/access";
    private static final String URL_USER_DOCUMENT = "/api/users/{id}/prepare-download/document";
    private static final String URL_USER_CARS_WAITING_DECISION = "/api/users/cars/waiting-decision";

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private RecognizerRepository recognizerRepository;

    private static Stream<Arguments> providerInputUserInvalid() {
        var emailField = new String[] {"email"};
        var namelField = new String[] {"name"};
        var typeField = new String[] {"typeUser"};
        var rolesField = new String[] {"roles"};
        var invalidEmailOrBlank = new String[] {"Invalid argument, email is invalid or blank."};
        var notBeNull = new String[] {"must not be null"};
        var notNeBlank = new String[] {"must not be blank"};
        var sizeBetweenMin5Max255 = new String[] {"size must be between 5 and 255"};
        var sizeBetweenMin1Max3 = new String[] {"size must be between 1 and 3"};
        return Stream.of(
            Arguments.of(
                buildInputUser("    ", "Name 1 User", SERVICE, List.of(1L)),
                emailField,
                invalidEmailOrBlank
            ),
            Arguments.of(
                buildInputUser("", "Name 2 User", SERVICE, List.of(1L)),
                emailField,
                invalidEmailOrBlank
            ),
            Arguments.of(
                buildInputUser("@com", "Name 3 User", SERVICE, List.of(1L)),
                emailField,
                invalidEmailOrBlank
            ),
            Arguments.of(
                buildInputUser(null, "Name 4 User", SERVICE, List.of(1L)),
                emailField,
                notBeNull
            ),
            Arguments.of(
                buildInputUser(
                    randomAlphabetic(260) + "@com.br",
                    "Name 5 User",
                    SERVICE,
                    List.of(1L)),
                emailField,
                sizeBetweenMin5Max255
            ),
            Arguments.of(
                buildInputUser("email@email.com", "     ", SERVICE, List.of(1L)),
                namelField,
                notNeBlank
            ),
            Arguments.of(
                buildInputUser("email@email.com", "", SERVICE, List.of(1L)),
                namelField,
                notNeBlank
            ),
            Arguments.of(
                buildInputUser("email@email.com", null, SERVICE, List.of(1L)),
                namelField,
                notBeNull
            ),
            Arguments.of(
                buildInputUser("email@email.com", "1234", SERVICE, List.of(1L)),
                namelField,
                sizeBetweenMin5Max255
            ),
            Arguments.of(
                buildInputUser("email@email.com", randomAlphabetic(256), SERVICE, List.of(1L)),
                namelField,
                sizeBetweenMin5Max255
            ),
            Arguments.of(
                buildInputUser("email@email.com", "Name 6 User", null, List.of(1L)),
                typeField,
                notBeNull
            ),
            Arguments.of(
                buildInputUser("email@email.com", "Name 7 User", SERVICE, List.of(1L, 2L, 3L, 4L)),
                rolesField,
                sizeBetweenMin1Max3
            ),
            Arguments.of(
                buildInputUser("email@email.com", "Name 8 User", SERVICE, List.of()),
                rolesField,
                sizeBetweenMin1Max3
            ),
            Arguments.of(
                buildInputUser("email@email.com", "Name 9 User", SERVICE, null),
                rolesField,
                notBeNull
            )
        );
    }

    private static InputUserDto buildInputUser(
        String email,
        String name,
        TypeUserEnum type,
        List<Long> roles) {
        return new InputUserDto()
            .typeUser(type)
            .email(email)
            .roles(roles)
            .authorisedAccess(TRUE)
            .name(name)
            .enabled(TRUE);
    }

    @Test
    @DisplayName("Deve retornar Unauthorized quando usuário tiver papel de motorista")
    @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository")
    void shouldReturnUnauthorizedWhenUserIsDriveRole() {
        var locale = Locale.US;
        var message = messageSource.getMessage(ACCESS_DENIED.getCode(), null, locale);

        var user = createDriver();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var inputNewUser = new InputUserDto()
            .typeUser(SPEAKER)
            .name(faker.name().fullName())
            .addRolesItem(1L)
            .email(faker.internet().emailAddress())
            .enabled(TRUE)
            .authorisedAccess(TRUE);

        assertionForbidden(locale, message, inputNewUser);

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve retornar Unauthorized quando usuário tiver papel de operador")
    @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository")
    void shouldReturnUnauthorizedWhenUserIsOperatorRole() {
        var locale = Locale.forLanguageTag("en-US");
        var message = messageSource.getMessage(ACCESS_DENIED.getCode(), null, locale);

        var user = createOperator();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var inputNewUser = new InputUserDto()
            .typeUser(SERVICE)
            .name(faker.name().fullName())
            .addRolesItem(1L)
            .email(faker.internet().emailAddress())
            .enabled(TRUE)
            .authorisedAccess(TRUE);

        assertionForbidden(locale, message, inputNewUser);
        deleteUser(user);
    }

    @ParameterizedTest
    @MethodSource("providerInputUserInvalid")
    @DisplayName("Deve retornar erro de bad request para criar quando campos estão inválidos")
    @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository")
    void shouldReturnBadRequestInCreateWhenFieldsInvalid(
        InputUserDto inputUserDto,
        String[] fields,
        String[] messages
    ) {
        var locale = Locale.US;
        var message = messageSource.getMessage(ARGUMENT_INVALID.getCode(), null, locale);

        var user = createAdmin();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", locale.toLanguageTag())
            .contentType(JSON)
            .body(inputUserDto, JACKSON_2)
            .expect()
            .statusCode(BAD_REQUEST.value())
            .body("message", is(message))
            .body("statusCode", is(400))
            .body("path", is("/users"))
            .body("details.findAll { it }.field", hasItems(fields))
            .body("details.findAll { it }.descriptionError", hasItems(messages))
            .when()
            .post(URL_USER);

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve retonar que e-mail não é do domínio da utfpr")
    @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository")
    void shouldReturnBadrequestWheEmailNotBeInstituional() {
        var inputUser = buildInputUser("email@email.com", "Name 1 User", STUDENTS, List.of(1L));

        var locale = Locale.US;
        var message = messageSource.getMessage(
            EMAIL_NOT_INSTITUTIONAL.getCode(),
            new String[] {inputUser.getEmail()},
            locale
        );

        var user = createAdmin();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", locale.toLanguageTag())
            .contentType(JSON)
            .body(inputUser, JACKSON_2)
            .expect()
            .statusCode(BAD_REQUEST.value())
            .body("message", is(message))
            .body("statusCode", is(400))
            .body("path", is("/users"))
            .when()
            .post(URL_USER);

        deleteUser(user);
    }

    @ParameterizedTest
    @EnumSource(value = TypeUserEnum.class, mode = Mode.EXCLUDE, names = "SERVICE")
    @DisplayName("Deve remover roles que não fazem parte do tipo do usuário")
    @ResourceLocks(value = {
        @ResourceLock(value = "br.edu.utfpr.tsi.xenon.structure.repository.UserRepository"),
        @ResourceLock(value = "br.edu.utfpr.tsi.xenon.structure.repository.AccessCardRepository"),
    })
    void shouldReturnErrorWhenUserStudentsOrSpeakerReceiveRolesInvalid(TypeUserEnum typeUser) {
        var faker = Faker.instance();
        var inputUser = buildInputUser(
            faker.bothify("roles-invalid-######@alunos.utfpr.edu.br"),
            faker.name().fullName(),
            typeUser,
            List.of(1L, 2L)
        );

        var user = createAdmin();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var userResponse = given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", Locale.US.toLanguageTag())
            .contentType(JSON)
            .body(inputUser, JACKSON_2)
            .expect()
            .statusCode(CREATED.value())
            .body("id", notNullValue())
            .body("name", is(inputUser.getName()))
            .body("email", is(inputUser.getEmail()))
            .body("type", is(inputUser.getTypeUser().name()))
            .body("cars", nullValue())
            .body("roles.findAll { it }.id", hasItems(1))
            .body("avatar", notNullValue())
            .body("disableReason", nullValue())
            .body("authorisedAccess", is(TRUE))
            .body("enabled", is(TRUE))
            .when()
            .post(URL_USER).body().as(UserDto.class);

        userRepository.deleteById(userResponse.getId());
        deleteUser(user);
    }

    @Test
    @DisplayName("Deve criar usuário com sucesso")
    @ResourceLocks(value = {
        @ResourceLock(value = "br.edu.utfpr.tsi.xenon.structure.repository.UserRepository"),
        @ResourceLock(value = "br.edu.utfpr.tsi.xenon.structure.repository.AccessCardRepository"),
    })
    void shouldHaveCreateUserSuccessfully() {
        var locale = Locale.forLanguageTag("en-US");

        var user = createAdmin();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var inputNewUser = new InputUserDto()
            .typeUser(SERVICE)
            .name(faker.name().fullName())
            .addRolesItem(1L)
            .email(faker.internet().emailAddress())
            .enabled(TRUE)
            .authorisedAccess(TRUE);

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", locale)
            .contentType(JSON)
            .body(inputNewUser, JACKSON_2)
            .expect()
            .statusCode(CREATED.value())
            .body("id", notNullValue())
            .body("name", is(inputNewUser.getName()))
            .body("email", is(inputNewUser.getEmail()))
            .body("type", is(inputNewUser.getTypeUser().name()))
            .body("cars", nullValue())
            .body("roles.findAll { it }.id", hasItems(1))
            .body("avatar", notNullValue())
            .body("disableReason", nullValue())
            .body("authorisedAccess", is(TRUE))
            .body("enabled", is(TRUE))
            .when()
            .post(URL_USER);
        deleteUser(user);
    }

    @Test
    @DisplayName("Deve retornar erro de bad request para atualizar quando campos estão inválidos")
    @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository")
    void shouldReturnBadRequestInUpdateWhenFieldsInvalid() {
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
            .body(new InputUpdateUserDto(), JACKSON_2)
            .pathParam("id", 1)
            .expect()
            .statusCode(BAD_REQUEST.value())
            .body("message", is(message))
            .body("statusCode", is(400))
            .body("path", is("/users/1"))
            .body("details.findAll { it }.field", hasItems(
                "email", "name", "typeUser", "roles")
            )
            .body("details.findAll { it }.descriptionError", hasItems(
                "must not be null", "must not be null", "size must be between 1 and 3",
                "must not be null")
            )
            .when()
            .put(URL_USER_WITH_ID);

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve atualizar usuário com sucesso")
    @ResourceLocks(value = {
        @ResourceLock(value = "br.edu.utfpr.tsi.xenon.structure.repository.UserRepository"),
        @ResourceLock(value = "br.edu.utfpr.tsi.xenon.structure.repository.AccessCardRepository"),
    })
    void shouldHaveUpdateUserSuccessfully() {
        var locale = Locale.forLanguageTag("en-US");
        var user = createAdmin();
        var userToUpdate = createDriver();
        var disabledReason = "disabled";

        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var inputUpdateUser = new InputUpdateUserDto()
            .typeUser(InputUpdateUserDto.TypeUserEnum.SERVICE)
            .name(faker.name().fullName())
            .email(faker.internet().emailAddress())
            .addRolesItem(1L)
            .addRolesItem(2L)
            .addRolesItem(3L)
            .enabled(TRUE)
            .authorisedAccess(FALSE)
            .disableReason(disabledReason);

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", locale)
            .contentType(JSON)
            .body(inputUpdateUser, JACKSON_2)
            .pathParam("id", userToUpdate.getId())
            .expect()
            .statusCode(OK.value())
            .body("id", is(userToUpdate.getId().intValue()))
            .body("name", is(inputUpdateUser.getName()))
            .body("email", is(inputUpdateUser.getEmail()))
            .body("type", is(inputUpdateUser.getTypeUser().name()))
            .body("roles.findAll { it }.id", hasItems(1, 2, 3))
            .body("enabled", is(TRUE))
            .body("authorisedAccess", is(FALSE))
            .body("disableReason", is(disabledReason))
            .when()
            .put(URL_USER_WITH_ID);

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        var userResultUpdate = userRepository.findById(userToUpdate.getId()).get();

        assertEquals(inputUpdateUser.getName(), userResultUpdate.getName());
        assertEquals(inputUpdateUser.getEmail(), userResultUpdate.getAccessCard().getUsername());
        assertEquals(inputUpdateUser.getAuthorisedAccess(), userResultUpdate.getAuthorisedAccess());
        assertEquals(inputUpdateUser.getDisableReason(), userResultUpdate.getDisableReason());
        assertEquals(inputUpdateUser.getTypeUser().name(), userResultUpdate.getTypeUser());
        assertEquals(inputUpdateUser.getRoles(),
            userResultUpdate.getAccessCard().getRoleEntities()
                .stream()
                .map(RoleEntity::getId)
                .collect(Collectors.toList())
        );

        deleteUser(userToUpdate);
        deleteUser(user);
    }

    @Test
    @DisplayName("Deve retornar uma objeto page com usuários")
    @ResourceLocks(value = {
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.AccessCardRepository"),
    })
    void shouldReturnPageUser() {
        var locale = Locale.forLanguageTag("en-US");
        setupDatabase();
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
            .queryParam("size", 5)
            .queryParam("page", 0)
            .queryParam("sorted", "name")
            .queryParam("direction", "asc")
            .queryParam("nameOrEmail", user.getName())
            .queryParam("type", "")
            .expect()
            .statusCode(OK.value())
            .body("size", is(5))
            .body("page", is(0))
            .body("sorted", is("NAME"))
            .body("direction", is("ASC"))
            .body("totalElements", is(1))
            .body("totalPage", is(1))
            .body("items.size()", is(1))
            .body("items.findAll { it }.email", hasItems(user.getAccessCard().getUsername()))
            .when()
            .get(URL_USER_ALL);

        clearDatabase();
    }

    @Test
    @DisplayName("Deve retornar erro que o valor de limite de elementos exibido")
    @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository")
    void shouldReturnErroSize() {
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
            .queryParam("size", 1001)
            .queryParam("page", 0)
            .queryParam("sorted", "name")
            .queryParam("direction", "asc")
            .queryParam("nameOrEmail", user.getName())
            .queryParam("type", "")
            .expect()
            .statusCode(BAD_REQUEST.value())
            .body("statusCode", is(400))
            .body("message", is(message))
            .body("path", is("/users/all"))
            .body("details.findAll { it }.field", hasItems("size"))
            .body("details.findAll { it }.descriptionError",
                hasItems("must be less than or equal to 100"))
            .when()
            .get(URL_USER_ALL);

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve retornar usuário por id")
    @ResourceLocks(value = {
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.AccessCardRepository"),
    })
    void shouldReturnUserById() {
        var user = createAdmin();
        var userSearch = createOperator();

        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .contentType(JSON)
            .pathParam("id", userSearch.getId().intValue())
            .expect()
            .statusCode(OK.value())
            .body("id", is(userSearch.getId().intValue()))
            .body("name", is(userSearch.getName()))
            .body("email", is(userSearch.getAccessCard().getUsername()))
            .body("type", is(userSearch.getTypeUser()))
            .body("cars.findAll { it }.id", hasItems(userSearch.firstCar().getId().intValue()))
            .body("roles.findAll { it }.id", hasItems(1, 3))
            .body("avatar", is(userSearch.getAvatar()))
            .body("disableReason", nullValue())
            .body("authorisedAccess", is(userSearch.getAuthorisedAccess()))
            .body("enabled", is(userSearch.getAccessCard().isEnabled()))
            .when()
            .get(URL_USER_WITH_ID);

        deleteUser(user);
        deleteUser(userSearch);
    }

    @Test
    @DisplayName("Deve desativar conta do usuário")
    @ResourceLocks(value = {
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.AccessCardRepository"),
    })
    void shouldHaveDisableAccountUser() {
        var locale = Locale.US;
        var message = messageSource.getMessage(USER_ACCOUNT_DEACTIVATED.getCode(), null, locale);
        var reason = "Usuário removido";

        var user = createAdmin();
        var userDisabled = createOperator();

        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var inoutDisableUser = new InputAccessUserDto()
            .reason(reason)
            .userId(userDisabled.getId());

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", locale.toLanguageTag())
            .contentType(JSON)
            .body(inoutDisableUser, JACKSON_2)
            .expect()
            .statusCode(OK.value())
            .body("result", is(message))
            .when()
            .delete(URL_USER);

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        var userResult = userRepository.findById(userDisabled.getId()).get();
        assertFalse(userResult.getAuthorisedAccess());
        assertFalse(userResult.getAccessCard().isEnabled());
        assertEquals(reason, userResult.getDisableReason());

        deleteUser(user);
        deleteUser(userDisabled);
    }

    @Test
    @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository")
    @DisplayName("Deve retornar not found quando desativar conta do usuário")
    void shouldReturnNotFoundWhenDisableAccountUser() {
        var locale = Locale.US;
        var user = createAdmin();

        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);
        var id = Math.abs(new Random().nextLong());
        var message = messageSource.getMessage(
            MessagesMapper.RESOURCE_NOT_FOUND.getCode(),
            new String[]{"usuário", "%d".formatted(id)},
            locale
        );

        var inoutDisableUser = new InputAccessUserDto()
            .reason("Usuário removido")
            .userId(id);

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", locale.toLanguageTag())
            .contentType(JSON)
            .body(inoutDisableUser, JACKSON_2)
            .expect()
            .statusCode(NOT_FOUND.value())
            .body("message", is(message))
            .body("statusCode", is(NOT_FOUND.value()))
            .body("path", is("/users"))
            .when()
            .delete(URL_USER);

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve remover autorização do usuário")
    @ResourceLocks(value = {
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.AccessCardRepository"),
    })
    void shouldHaveRemoveAuthorizationUser() {
        var locale = Locale.US;
        var message = messageSource.getMessage(REMOVE_AUTHORIZATION_ACCESS.getCode(), null, locale);
        var reason = "Usuário removendo autorização";

        var user = createAdmin();
        var userRemoveAuthorization = createOperator();

        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var inoutDisableUser = new InputAccessUserDto()
            .reason(reason)
            .userId(userRemoveAuthorization.getId());

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", locale.toLanguageTag())
            .contentType(JSON)
            .body(inoutDisableUser, JACKSON_2)
            .expect()
            .statusCode(OK.value())
            .body("result", is(message))
            .when()
            .patch(URL_USER_REMOVE_AUTHORIZATION);

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        var userResult = userRepository.findById(userRemoveAuthorization.getId()).get();
        assertFalse(userResult.getAuthorisedAccess());
        assertTrue(userResult.getAccessCard().isEnabled());
        assertEquals(reason, userResult.getDisableReason());

        deleteUser(user);
        deleteUser(userRemoveAuthorization);
    }

    @Test
    @DisplayName("Deve retonar not found quando remover autorização e usuário não existe")
    @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository")
    void shouldNotFoundRemoveAuthorizationUser() {
        var user = createAdmin();

        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var id = Math.abs(new Random().nextLong());
        var message = messageSource.getMessage(
            MessagesMapper.RESOURCE_NOT_FOUND.getCode(),
            new String[]{"usuário", "%d".formatted(id)},
            Locale.US
        );

        var inoutDisableUser = new InputAccessUserDto()
            .reason("reason")
            .userId(id);

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", Locale.US.toLanguageTag())
            .contentType(JSON)
            .body(inoutDisableUser, JACKSON_2)
            .expect()
            .statusCode(NOT_FOUND.value())
            .body("message", is(message))
            .body("statusCode", is(NOT_FOUND.value()))
            .body("path", is("/users/disabled/access"))
            .when()
            .patch(URL_USER_REMOVE_AUTHORIZATION);

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve adicionar autorização do usuário")
    @ResourceLocks(value = {
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.AccessCardRepository"),
    })
    void shouldHaveAddAuthorizationUser() {
        var locale = Locale.US;
        var message = messageSource.getMessage(ADD_AUTHORIZATION_ACCESS.getCode(), null, locale);
        var reason = "Usuário autorização";

        var user = createAdmin();
        var userAddAuthorization = createOperator();

        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var inoutDisableUser = new InputAccessUserDto()
            .reason(reason)
            .userId(userAddAuthorization.getId());

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", locale.toLanguageTag())
            .contentType(JSON)
            .body(inoutDisableUser, JACKSON_2)
            .expect()
            .statusCode(OK.value())
            .body("result", is(message))
            .when()
            .patch(URL_USER_ADD_AUTHORIZATION);

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        var userResult = userRepository.findById(userAddAuthorization.getId()).get();
        assertTrue(userResult.getAuthorisedAccess());
        assertTrue(userResult.getAccessCard().isEnabled());
        assertNull(userResult.getDisableReason());

        deleteUser(user);
        deleteUser(userAddAuthorization);
    }

    @Test
    @DisplayName("Deve retonar not found quando remover autorização e usuário não existe")
    @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository")
    void shouldNotFoundAddAuthorizationUser() {
        var user = createAdmin();

        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var id = Math.abs(new Random().nextLong());
        var message = messageSource.getMessage(
            MessagesMapper.RESOURCE_NOT_FOUND.getCode(),
            new String[]{"usuário", "%d".formatted(id)},
            Locale.US
        );

        var inoutDisableUser = new InputAccessUserDto()
            .reason("reason")
            .userId(id);

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", Locale.US.toLanguageTag())
            .contentType(JSON)
            .body(inoutDisableUser, JACKSON_2)
            .expect()
            .statusCode(NOT_FOUND.value())
            .body("message", is(message))
            .body("statusCode", is(NOT_FOUND.value()))
            .body("path", is("/users/enabled/access"))
            .when()
            .patch(URL_USER_ADD_AUTHORIZATION);

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve reprovar documentação do carro com sucesso")
    @ResourceLocks(value = {
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.AccessCardRepository"),
    })
    void shouldHaveReprovedDocument() {
        var faker = Faker.instance();
        var user = createAdmin();
        var roles = roleRepository.findAll();

        var userCar = getUserEntity(faker, roles, TypeUser.SERVICE);
        var car = new CarEntity();
        car.setCarStatus(CarStatus.WAITING);
        car.setAuthorisedAccess(Boolean.FALSE);
        car.setState("WAITING_DECISION");
        car.setUser(user);
        car.setPlate("ABC-1234");
        car.setModel("MODEL");
        car.setUser(userCar);
        userCar.getCar().add(car);

        userRepository.saveAndFlush(userCar);

        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .pathParam("id", car.getId())
            .contentType(JSON)
            .expect()
            .statusCode(NO_CONTENT.value())
            .when()
            .patch(URL_USER_REPROVED_CAR);

        //noinspection OptionalGetWithoutIsPresent
        var carSaved = carRepository.findById(car.getId()).get();

        assertFalse(carSaved.getAuthorisedAccess());
        assertEquals(CarStatus.REPROVED, carSaved.getCarStatus());
        assertEquals("REPROVED", carSaved.getState());
    }

    @Test
    @DisplayName("Deve retonar not found quando reprovar documentação e carro não existe")
    @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository")
    void shouldReturnNotFoundRepropprovedDocument() {
        var user = createAdmin();

        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var id = Math.abs(new Random().nextLong());
        var message = messageSource.getMessage(
            MessagesMapper.RESOURCE_NOT_FOUND.getCode(),
            new String[]{"Carro", "%d".formatted(id)},
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
            .body("path", is("/users/car/%d/reproved".formatted(id)))
            .when()
            .patch(URL_USER_REPROVED_CAR);

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve aprovar documentação do carro com sucesso")
    @ResourceLocks(value = {
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.AccessCardRepository"),
    })
    void shouldHaveApprovedDocument() {
        var faker = Faker.instance();
        var user = createAdmin();
        var roles = roleRepository.findAll();

        var userCar = getUserEntity(faker, roles, TypeUser.SERVICE);
        var car = new CarEntity();
        car.setCarStatus(CarStatus.WAITING);
        car.setAuthorisedAccess(Boolean.FALSE);
        car.setState("WAITING_DECISION");
        car.setUser(user);
        car.setPlate(faker.bothify("###-?#??", TRUE));
        car.setModel("MODEL");
        car.setUser(userCar);
        userCar.getCar().add(car);

        userRepository.saveAndFlush(userCar);

        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .pathParam("id", car.getId())
            .contentType(JSON)
            .expect()
            .statusCode(NO_CONTENT.value())
            .when()
            .patch(URL_USER_APPROVED_CAR);

        //noinspection OptionalGetWithoutIsPresent
        var carSaved = carRepository.findById(car.getId()).get();

        assertTrue(carSaved.getAuthorisedAccess());
        assertEquals(CarStatus.APPROVED, carSaved.getCarStatus());
        assertEquals("APPROVED", carSaved.getState());
    }

    @Test
    @DisplayName("Deve retonar not found quando aprovar documentação e carro não existe")
    @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository")
    void shouldReturnNotFoundApprovedDocument() {
        var user = createAdmin();

        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var id = Math.abs(new Random().nextLong());
        var message = messageSource.getMessage(
            MessagesMapper.RESOURCE_NOT_FOUND.getCode(),
            new String[]{"Carro", "%d".formatted(id)},
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
            .body("path", is("/users/car/%d/approved".formatted(id)))
            .when()
            .patch(URL_USER_APPROVED_CAR);

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve retonar o carro do usuário com sucesso")
    @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository")
    void shouldReturnCar() {
        var user = createAdmin();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var car = user.getCar().get(0);

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .pathParam("id", user.getId())
            .contentType(JSON)
            .expect()
            .statusCode(OK.value())
            .body("[0].id", is(car.getId().intValue()))
            .body("[0].modelCar", is(car.getModel()))
            .body("[0].plateCar", is(car.getPlate()))
            .body("[0].lastAcess", nullValue())
            .body("[0].numberAccess", is(car.getNumberAccess()))
            .body("[0].authorisedAccess", is(car.getAuthorisedAccess()))
            .body("[0].status", is(car.getCarStatus().name()))
            .body("[0].reasonLock", nullValue())
            .body("[0].document", is(car.getDocument()))
            .when()
            .get(URL_USER_CARS);

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve retornar lista de acesso de um carro")
    @ResourceLocks(value = {
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.AccessCardRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.RecognizerRepository")
    })
    void shouldReturnListAccessByCar() {
        var user = createAdmin();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var car = user.getCar().get(0);
        var recognizerExpected = new RecognizeEntity();
        recognizerExpected.setConfidence(99.9F);
        recognizerExpected.setEpochTime(LocalDateTime.now());
        recognizerExpected.setPlate(car.getPlate());
        recognizerExpected.setOriginIp("ip");
        recognizerExpected.setDriverName(car.getUser().getName());
        recognizerExpected.setHasError(FALSE);
        recognizerExpected.setAccessGranted(TRUE);

        recognizerRepository.saveAndFlush(recognizerExpected);

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .pathParam("id", user.getId())
            .contentType(JSON)
            .expect()
            .statusCode(OK.value())
            .body("size", is(5))
            .body("page", is(0))
            .body("sorted", is("createdAt"))
            .body("direction", is(Direction.DESC.name()))
            .body("totalElements", is(1))
            .body("totalPage", is(1))
            .body("amountCars", is(1))
            .body("items[0].carPlate", is(car.getPlate()))
            .body("items[0].epochTime",
                containsString(recognizerExpected.getEpochTime().truncatedTo(ChronoUnit.SECONDS).toString()))
            .body("items[0].confidence", is(recognizerExpected.getConfidence()))
            .body("items[0].grandAccess", is(recognizerExpected.getAccessGranted()))
            .when()
            .get(URL_USER_CARS_ACCESS);

        deleteUser(user);
        recognizerRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve retornar uri para fazer download do documento de um determinado carro")
    @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository")
    void shouldReturnUriDownloadDocumentCar() {
        var user = createAdmin();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);
        var id = user.getCar().get(0).getId();

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .pathParam("id", id)
            .contentType(JSON)
            .expect()
            .statusCode(OK.value())
            .body("uri", notNullValue())
            .body("ttl", is(5))
            .when()
            .get(URL_USER_DOCUMENT);

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve retorna not found quanto buscar documento do carro e usuário não existe")
    @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository")
    void shouldReturnNotFoundUserWhenCallDocumentCar() {
        var user = createAdmin();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var id = Math.abs(new Random().nextLong());
        var message = messageSource.getMessage(
            MessagesMapper.RESOURCE_NOT_FOUND.getCode(),
            new String[]{"carro", "%d".formatted(id)},
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
            .body("path", is("/users/%d/prepare-download/document".formatted(id)))
            .when()
            .get(URL_USER_DOCUMENT);

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve retornar uma página de carros que estão esperando decisão do cadastro do carro")
    @ResourceLocks(value = {
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.AccessCardRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.CarRepository")
    })
    void shouldReturnPageAllCarsWaitingDecision() {
        var user = createAdmin();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var carOne = new CarEntity();
        carOne.setCarStatus(CarStatus.WAITING);
        carOne.setPlate("plate");
        carOne.setAuthorisedAccess(Boolean.TRUE);
        carOne.setState("WAITING_DECISION");
        carOne.setDocument("document");
        carOne.setReasonBlock("reason block");
        carOne.setModel("model car");
        carOne.setLastAccess(LocalDateTime.now());
        carOne.setNumberAccess(10);

        carRepository.saveAndFlush(carOne);

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .contentType(JSON)
            .expect()
            .statusCode(OK.value())
            .body("size", is(5))
            .body("page", is(0))
            .body("sorted", is("createdAt"))
            .body("direction", is(DESC.name()))
            .body("totalElements", is(1))
            .body("totalPage", is(1))
            .body("items[0].id", is(carOne.getId().intValue()))
            .body("items[0].modelCar", is(carOne.getModel()))
            .body("items[0].plateCar", is(carOne.getPlate()))
            .body("items[0].lastAcess", notNullValue())
            .body("items[0].numberAccess", is(carOne.getNumberAccess()))
            .body("items[0].authorisedAccess", is(carOne.getAuthorisedAccess()))
            .body("items[0].status", is(carOne.getCarStatus().name()))
            .body("items[0].reasonLock", is(carOne.getReasonBlock()))
            .body("items[0].document", is(carOne.getDocument()))
            .when()
            .get(URL_USER_CARS_WAITING_DECISION);

        deleteUser(user);
        carRepository.deleteAll();
    }

    private void setupDatabase() {
        var faker = Faker.instance();
        var roles = roleRepository.findAll();

        var speakers = IntStream.range(0, 5).boxed()
            .map(index -> getUserEntity(faker, roles, TypeUser.SPEAKER))
            .collect(Collectors.toList());

        var students = IntStream.range(0, 5).boxed()
            .map(index -> getUserEntity(faker, roles, TypeUser.STUDENTS))
            .collect(Collectors.toList());

        var service = IntStream.range(0, 5).boxed()
            .map(index -> getUserEntity(faker, roles, TypeUser.SERVICE))
            .collect(Collectors.toList());

        userRepository.saveAllAndFlush(speakers);
        userRepository.saveAllAndFlush(students);
        userRepository.saveAllAndFlush(service);
    }

    private void clearDatabase() {
        carRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        userRepository.flush();
    }

    private UserEntity getUserEntity(Faker faker, List<RoleEntity> roles, TypeUser speaker) {
        var user = new UserEntity();
        user.setName(faker.name().fullName());
        user.setAuthorisedAccess(TRUE);
        user.setAvatar("url");
        user.setTypeUser(speaker.name());

        var accessCard = new AccessCardEntity();
        accessCard.setUser(user);
        accessCard.setEnabled(TRUE);
        accessCard.setUsername(faker.internet().emailAddress());
        accessCard.setPassword("pass");
        accessCard.setRoleEntities(roles);

        user.setAccessCard(accessCard);

        return user;
    }

    private void assertionForbidden(Locale locale, String message, InputUserDto inputNewUser) {
        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Accept-Language", locale.toLanguageTag())
            .contentType(JSON)
            .body(inputNewUser, JACKSON_2)
            .expect()
            .statusCode(FORBIDDEN.value())
            .body("message", is(message))
            .body("statusCode", is(403))
            .when()
            .post(URL_USER);
    }
}
