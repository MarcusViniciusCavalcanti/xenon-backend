package br.edu.utfpr.tsi.xenon.domain.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import br.edu.utfpr.tsi.xenon.application.config.property.SecurityProperty;
import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.domain.security.entity.RoleEntity;
import com.github.javafaker.Faker;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - AccessTokenCreator")
class AccessTokenServiceTest {

    @Mock
    private SecurityProperty securityProperty;

    @Mock
    private SecurityProperty.TokenConfiguration tokenConfiguration;

    @InjectMocks
    private AccessTokenService accessTokenService;

    @Test
    @DisplayName("Deve criar um token")
    void shouldHaveCreateToken() {
        var accessCard = new AccessCardEntity();
        var role = new RoleEntity();
        role.setId(1L);
        role.setName("roler_name");
        role.setDescription("description role");

        var expirationTime = LocalDateTime.now().plusMinutes(1L);
        when(securityProperty.expirationTimeDate()).thenReturn(expirationTime);
        when(securityProperty.getToken()).thenReturn(tokenConfiguration);
        when(tokenConfiguration.getSecretKey()).thenReturn("secrete");

        accessCard.setUsername(Faker.instance().internet().emailAddress());
        accessCard.setRoleEntities(List.of(role));

        var token = accessTokenService.create(accessCard);

        var tokenPartis = token.getData().getToken().split("\\.");
        var partOne =
            new String(Base64.getDecoder().decode(tokenPartis[0].getBytes(StandardCharsets.UTF_8)));
        var partTwo =
            new String(Base64.getDecoder().decode(tokenPartis[1].getBytes(StandardCharsets.UTF_8)));

        assertEquals("""
            {"alg":"HS512"}""", partOne);
        assertEquals("""
                {"sub":"%s","exp":%s,"roles":[{"id":%d,"name":"%s","description":"%s"}]}"""
                .formatted(
                    accessCard.getUsername(),
                    expirationTime.toInstant(ZoneOffset.UTC).toEpochMilli() / 1000L,
                    role.getId(),
                    role.getName(),
                    role.getDescription()),
            partTwo);
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

        var result = accessTokenService.getEmail(token).get();
        assertEquals(email, result);
    }
}
