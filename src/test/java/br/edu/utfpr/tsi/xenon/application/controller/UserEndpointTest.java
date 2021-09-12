package br.edu.utfpr.tsi.xenon.application.controller;

import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.ACCESS_DENIED;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.ADD_AUTHORIZATION_ACCESS;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.ARGUMENT_INVALID;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.REMOVE_AUTHORIZATION_ACCESS;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.USER_ACCOUNT_DEACTIVATED;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.mapper.ObjectMapperType.JACKSON_2;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;

import br.edu.utfpr.tsi.xenon.AbstractSecurityContext;
import br.edu.utfpr.tsi.xenon.application.dto.InputAccessUserDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputLoginDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputUpdateUserDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputUserDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputUserDto.TypeUserEnum;
import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.domain.security.entity.RoleEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.factory.TypeUser;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import com.github.javafaker.Faker;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

@DisplayName("Test - Integration - Funcionalidade de Usuários")
class UserEndpointTest extends AbstractSecurityContext {

    private static final String URL_USER = "/api/users";
    private static final String URL_USER_WITH_ID = "/api/users/{id}";
    private static final String URL_USER_ALL = "/api/users/all";
    private static final String URL_USER_REMOVE_AUTHORIZATION = "/api/users/disabled/access";
    private static final String URL_USER_ADD_AUTHORIZATION = "/api/users/enabled/access";

    @Autowired
    private CarRepository carRepository;

    @Test
    @DisplayName("Deve retornar Unauthorized quando usuário tiver papel de motorista")
    void shouldReturnUnauthorizedWhenUserIsDriveRole() {
        var locale = Locale.forLanguageTag("pt_BR");
        var message = messageSource.getMessage(ACCESS_DENIED.getCode(), null, locale);

        var user = createDriver();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var inputNewUser = new InputUserDto()
            .typeUser(TypeUserEnum.SPEAKER)
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
    void shouldReturnUnauthorizedWhenUserIsOperatorRole() {
        var locale = Locale.forLanguageTag("pt_BR");
        var message = messageSource.getMessage(ACCESS_DENIED.getCode(), null, locale);

        var user = createOperator();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var inputNewUser = new InputUserDto()
            .typeUser(TypeUserEnum.SERVICE)
            .name(faker.name().fullName())
            .addRolesItem(1L)
            .email(faker.internet().emailAddress())
            .enabled(TRUE)
            .authorisedAccess(TRUE);

        assertionForbidden(locale, message, inputNewUser);
        deleteUser(user);
    }

    @Test
    @DisplayName("Deve retornar erro de bad request para criar quando campos estão inválidos")
    void shouldReturnBadRequestInCreateWhenFieldsInvalid() {
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
            .body(new InputUserDto(), JACKSON_2)
            .expect()
            .statusCode(BAD_REQUEST.value())
            .body("message", is(message))
            .body("statusCode", is(400))
            .body("path", is("/users"))
            .body("details.findAll { it }.field", hasItems(
                "email", "name", "typeUser", "roles")
            )
            .body("details.findAll { it }.descriptionError", hasItems(
                "não deve ser nulo", "não deve ser nulo", "tamanho deve ser entre 1 e 3",
                "não deve ser nulo")
            )
            .when()
            .post(URL_USER);

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve criar usuário com sucesso")
    void shouldHaveCreateUserSuccessfully() {
        var locale = Locale.forLanguageTag("pt_BR");

        var user = createAdmin();
        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var inputNewUser = new InputUserDto()
            .typeUser(TypeUserEnum.SERVICE)
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
    void shouldReturnBadRequestInUpdateWhenFieldsInvalid() {
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
                "não deve ser nulo", "não deve ser nulo", "tamanho deve ser entre 1 e 3",
                "não deve ser nulo")
            )
            .when()
            .put(URL_USER_WITH_ID);

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve atualizar usuário com sucesso")
    void shouldHaveUpdateUserSuccessfully() {
        var locale = Locale.forLanguageTag("pt_BR");
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
    void shouldReturnPageUser() {
        var locale = Locale.forLanguageTag("pt_BR");
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
    void shouldReturnErroSize() {
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
            .body("details.findAll { it }.descriptionError", hasItems("deve ser menor que ou igual à 100"))
            .when()
            .get(URL_USER_ALL);

        deleteUser(user);
    }

    @Test
    @DisplayName("Deve retornar usuário por id")
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
    void shouldHaveDisableAccountUser() {
        var locale = Locale.forLanguageTag("pt_BR");
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
            .header("Accept-Language", locale)
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
    @DisplayName("Deve remover autorização do usuário")
    void shouldHaveRemoveAuthorizationUser() {
        var locale = Locale.forLanguageTag("pt_BR");
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
            .header("Accept-Language", locale)
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
    @DisplayName("Deve remover autorização do usuário")
    void shouldHaveAddAuthorizationUser() {
        var locale = Locale.forLanguageTag("pt_BR");
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
            .header("Accept-Language", locale)
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
            .header("Accept-Language", locale)
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
