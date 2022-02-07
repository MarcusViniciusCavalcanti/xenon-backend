package br.edu.utfpr.tsi.xenon.application.controller;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.mapper.ObjectMapperType.JACKSON_2;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import br.edu.utfpr.tsi.xenon.AbstractContextTest;
import br.edu.utfpr.tsi.xenon.application.dto.InputLoginDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputRenewPasswordDto;
import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.factory.TypeUser;
import br.edu.utfpr.tsi.xenon.structure.repository.RoleRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.UserRepository;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@DisplayName("Test - Integration - Funcionalidade login")
class SecurityEndpointTest extends AbstractContextTest {

    private static final String URI_LOGIN = "/api/login";
    private static final String REQUEST_RENEW_PASS = "/api/request-renew-pass";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("Deve retornar erro quando usuário não encontrado")
    void shouldReturnError() {
        var locale = Locale.US;
        var message = messageSource.getMessage("ERROR-011", null, locale);
        var input = new InputLoginDto()
            .password("abc123")
            .email("email@email.com");

        given()
            .port(port)
            .accept(APPLICATION_JSON_VALUE)
            .header("Accept-Language", locale.toLanguageTag())
            .contentType(JSON)
            .body(input, JACKSON_2)
            .expect()
            .statusCode(UNAUTHORIZED.value())
            .body("message", is(message))
            .body("statusCode", is(401))
            .when()
            .post(URI_LOGIN);
    }

    @Test
    @DisplayName("Deve retornar erro quando corpo da requisição está vázio")
    void shouldReturnErrorBadRequest() {
        var locale = Locale.US;
        var message = messageSource.getMessage("ERROR-011", null, locale);
        var input = new InputLoginDto()
            .password("")
            .email("");

        given()
            .port(port)
            .accept(APPLICATION_JSON_VALUE)
            .header("Accept-Language", locale.toLanguageTag())
            .contentType(JSON)
            .body(input, JACKSON_2)
            .expect()
            .statusCode(UNAUTHORIZED.value())
            .body("message", is(message))
            .body("statusCode", is(401))
            .when()
            .post(URI_LOGIN);
    }

    @Test
    @DisplayName("Deve retornar erro quando corpo da requisição está vázio")
    void shouldReturnErrorBadRequestInputNull() {
        var locale = Locale.US;
        var message = messageSource.getMessage("ERROR-001", null, locale);

        given()
            .port(port)
            .accept(APPLICATION_JSON_VALUE)
            .header("Accept-Language", locale.toLanguageTag())
            .contentType(JSON)
            .body("{}")
            .expect()
            .statusCode(BAD_REQUEST.value())
            .body("message", is(message))
            .body("statusCode", is(400))
            .body("path", is("/login"))
            .body("details[].fields", everyItem(
                hasItems("email", "password")
            ))
            .body("details[].descriptionError", everyItem(
                hasItems("não deve ser nulo", "não deve ser nulo")
            ))
            .when()
            .post(URI_LOGIN);
    }

    @Test
    @DisplayName("Deve logar com sucesso")
    void shouldHaveLoginSuccess() {
        var locale = Locale.forLanguageTag("en-US");
        var email = faker.internet().emailAddress();
        var user = new UserEntity();
        user.setName(faker.name().fullName());
        user.setTypeUser(TypeUser.SERVICE.name());

        var accessCard = new AccessCardEntity();
        accessCard.setPassword(new BCryptPasswordEncoder().encode("1234567"));
        accessCard.setUsername(email);

        var roles = roleRepository.findAllById(List.of(1L, 2L, 3L));
        accessCard.setRoleEntities(roles);
        accessCard.setEnabled(Boolean.TRUE);
        accessCard.setUser(user);

        user.setAccessCard(accessCard);
        userRepository.saveAndFlush(user);

        var input = new InputLoginDto()
            .password("1234567")
            .email(accessCard.getUsername());

        given()
            .port(port)
            .accept(APPLICATION_JSON_VALUE)
            .header("Accept-Language", locale.getLanguage())
            .contentType(JSON)
            .body(input, JACKSON_2)
            .expect()
            .statusCode(OK.value())
            .body("data.token", notNullValue())
            .body("data.expiration", notNullValue())
            .when()
            .post(URI_LOGIN);
    }

    @Test
    @DisplayName("Deve retornar sucesso de pedido de processo de renovação de senha")
    void shouldHaveStartRenewPassword() {
        var locale = Locale.forLanguageTag("en-US");
        var input = new InputRenewPasswordDto()
            .email(faker.internet().emailAddress());

        given()
            .port(port)
            .accept(APPLICATION_JSON_VALUE)
            .header("Accept-Language", locale.getLanguage())
            .contentType(JSON)
            .body(input, JACKSON_2)
            .expect()
            .statusCode(ACCEPTED.value())
            .when()
            .post(REQUEST_RENEW_PASS);
    }

    @Test
    @DisplayName("Deve retornar sucesso de pedido de processo de renovação de senha")
    void shouldHaveCompleteRenewPassword() {
        var locale = Locale.forLanguageTag("en-US");
        var input = new InputRenewPasswordDto()
            .email(faker.internet().emailAddress());

        given()
            .queryParam("params",
                "ZW1haWxfbmV3X3Bhc3N3b3JkQGVtYWlsLmNvbS1zYXVsdDo4Mjk3ZWVmYS1kMGZhLTRkMjYtYTUwNi1iMGU1YzBiYjk3OTQ=")
            .port(port)
            .accept(APPLICATION_JSON_VALUE)
            .header("Accept-Language", locale.getLanguage())
            .contentType(JSON)
            .body(input, JACKSON_2)
            .expect()
            .statusCode(ACCEPTED.value())
            .when()
            .get(REQUEST_RENEW_PASS);
    }
}
