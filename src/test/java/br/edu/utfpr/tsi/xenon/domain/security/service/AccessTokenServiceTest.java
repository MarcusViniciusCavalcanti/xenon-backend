package br.edu.utfpr.tsi.xenon.domain.security.service;

import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.utfpr.tsi.xenon.application.config.property.SecurityProperty;
import br.edu.utfpr.tsi.xenon.application.dto.InputLoginDto;
import br.edu.utfpr.tsi.xenon.application.dto.TokenDto;
import br.edu.utfpr.tsi.xenon.application.dto.UserDto;
import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.domain.security.entity.RoleEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.factory.TypeUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - AccessTokenCreator")
class AccessTokenServiceTest {

    @Spy
    private ObjectMapper objectMapper;

    @Mock
    private SecurityProperty securityProperty;

    @Mock
    private SecurityProperty.TokenConfiguration tokenConfiguration;

    @InjectMocks
    private AccessTokenService accessTokenService;

    @Test
    @DisplayName("Deve criar um token")
    void shouldHaveCreateToken() {
        var faker = Faker.instance();
        var accessCard = new AccessCardEntity();
        var role = new RoleEntity();
        var user = new UserEntity();

        user.setId(1L);
        user.setName(faker.name().fullName());
        user.setTypeUser(TypeUser.SPEAKER.name());
        user.setAvatar(faker.internet().avatar());
        user.setAuthorisedAccess(TRUE);

        role.setId(1L);
        role.setName("roler_name");
        role.setDescription("description role");

        var expirationTime = LocalDateTime.now().plusMinutes(1L);
        when(securityProperty.expirationTimeDate()).thenReturn(expirationTime);
        when(securityProperty.getToken()).thenReturn(tokenConfiguration);
        when(tokenConfiguration.getSecretKey()).thenReturn("secrete");

        accessCard.setUsername(faker.internet().emailAddress());
        accessCard.setRoleEntities(List.of(role));
        accessCard.setUser(user);
        user.setAccessCard(accessCard);

        var token = accessTokenService.create(accessCard);

        var tokenPartis = token.getData().getToken().split("\\.");
        var partOne =
            new String(Base64.getDecoder().decode(tokenPartis[0].getBytes(StandardCharsets.UTF_8)));
        var partTwo =
            new String(Base64.getDecoder().decode(tokenPartis[1].getBytes(StandardCharsets.UTF_8)));

        assertEquals("""
            {"alg":"HS512"}""", partOne);

        try {
            var objectMapper = new ObjectMapper();
            var nodes = objectMapper.readTree(partTwo);
            var userDto = objectMapper.readValue(nodes.get("user").asText(), UserDto.class);

            assertEquals(user.getTypeUser(), userDto.getType().name());
            assertEquals(user.getAvatar(), userDto.getAvatar());
            assertEquals(user.getId(), userDto.getId());
            assertEquals(user.getName(), userDto.getName());
            assertEquals(user.getAuthorisedAccess(), userDto.getAuthorisedAccess());
            assertEquals(user.getAccessCard().isEnabled(), userDto.getEnabled());
            assertEquals(user.getAccessCard().getUsername(), userDto.getEmail());

            assertNull(userDto.getDisableReason());
            assertNull(userDto.getCars());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }

    @Test
    @DisplayName("Deve retonar que token esta invalido quando username está nulo")
    void shouldReturnInvalidTokenWhenUsernameIsNull() {
        var now = LocalDateTime.now().toInstant(ZoneOffset.UTC);
        var security = "security";
        var token = Jwts.builder()
            .setExpiration(Date.from(now))
            .signWith(SignatureAlgorithm.HS512, security)
            .compact();

        assertFalse(accessTokenService.isValid(token));
    }

    @Test
    @DisplayName("Deve retonar que token esta invalido quando data de expiração está nulo")
    void shouldReturnInvalidTokenWhenExpirationIsNull() {
        var security = "security";
        var token = Jwts.builder()
            .setSubject(Faker.instance().internet().emailAddress())
            .signWith(SignatureAlgorithm.HS512, security)
            .compact();

        assertFalse(accessTokenService.isValid(token));
    }

    @Test
    @DisplayName("Deve retonar que token esta invalido quando data está invalida")
    void shouldReturnInvalidTokenWhenExpirationIsBeforeDate() {
        var security = "security";
        var beforeOneDate = LocalDateTime.now().plusMinutes(1L).toInstant(ZoneOffset.UTC);
        var token = Jwts.builder()
            .setSubject(Faker.instance().internet().emailAddress())
            .signWith(SignatureAlgorithm.HS512, security)
            .setExpiration(Date.from(beforeOneDate))
            .compact();

        assertFalse(accessTokenService.isValid(token));
    }

    @Test
    @DisplayName("Deve retonar que token está valido")
    void shouldReturnValidToken() {
        var security = "security";
        var beforeOneDate = LocalDateTime.now().plusDays(1L).toInstant(ZoneOffset.UTC);
        var token = Jwts.builder()
            .setSubject(Faker.instance().internet().emailAddress())
            .signWith(SignatureAlgorithm.HS512, security)
            .setExpiration(Date.from(beforeOneDate))
            .compact();

        when(securityProperty.getToken()).thenReturn(tokenConfiguration);
        when(tokenConfiguration.getSecretKey()).thenReturn(security);

        assertTrue(accessTokenService.isValid(token));
    }

    @Test
    @DisplayName("Deve retonar que token está valido")
    void shouldReturnEmail() {
        var security = "security";
        var beforeOneDate = LocalDateTime.now().plusDays(1L).toInstant(ZoneOffset.UTC);
        var email = Faker.instance().internet().emailAddress();
        var token = Jwts.builder()
            .setSubject(email)
            .signWith(SignatureAlgorithm.HS512, security)
            .setExpiration(Date.from(beforeOneDate))
            .compact();

        when(securityProperty.getToken()).thenReturn(tokenConfiguration);
        when(tokenConfiguration.getSecretKey()).thenReturn(security);

        //noinspection OptionalGetWithoutIsPresent
        var result = accessTokenService.getEmail(token).get();
        assertEquals(email, result);
    }

    @Test
    @DisplayName("Deve lançar JsonProcessingException quando lançado pelo objectMapper")
    void shouldThrowsJsonProcessingException() throws JsonProcessingException {
        var faker = Faker.instance();
        var accessCard = new AccessCardEntity();
        var role = new RoleEntity();
        var user = new UserEntity();

        user.setId(1L);
        user.setName(faker.name().fullName());
        user.setTypeUser(TypeUser.SPEAKER.name());
        user.setAvatar(faker.internet().avatar());
        user.setAuthorisedAccess(TRUE);

        role.setId(1L);
        role.setName("roler_name");
        role.setDescription("description role");

        var expirationTime = LocalDateTime.now().plusMinutes(1L);
        when(securityProperty.expirationTimeDate()).thenReturn(expirationTime);
        when(securityProperty.getToken()).thenReturn(tokenConfiguration);
        when(tokenConfiguration.getSecretKey()).thenReturn("secrete");

        accessCard.setUsername(faker.internet().emailAddress());
        accessCard.setRoleEntities(List.of(role));
        accessCard.setUser(user);
        user.setAccessCard(accessCard);

        when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        assertThrows(JsonProcessingException.class, () -> accessTokenService.create(accessCard));
    }
}
