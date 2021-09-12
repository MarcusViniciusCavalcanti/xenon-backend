package br.edu.utfpr.tsi.xenon.domain.security.service;

import br.edu.utfpr.tsi.xenon.application.config.property.SecurityProperty;
import br.edu.utfpr.tsi.xenon.application.dto.TokenDataDto;
import br.edu.utfpr.tsi.xenon.application.dto.TokenDto;
import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.domain.user.factory.UserFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessTokenService {

    private final SecurityProperty securityProperty;
    private final ObjectMapper objectMapper;

    private final Predicate<Claims> containsUsername =
        (claims -> StringUtils.isNotBlank(claims.getSubject()));
    private final Predicate<Claims> containsExpiration =
        (claims -> Objects.nonNull(claims.getExpiration()));

    @SneakyThrows
    public TokenDto create(AccessCardEntity accessCardEntity) {
        var email = accessCardEntity.getUsername();
        var now = securityProperty.expirationTimeDate().toInstant(ZoneOffset.UTC);

        var user = UserFactory.getInstance().buildUserDto(accessCardEntity.getUser());
        var token = Jwts.builder()
            .setSubject(email)
            .setExpiration(Date.from(now))
            .signWith(SignatureAlgorithm.HS512, securityProperty.getToken().getSecretKey())
            .claim("user", objectMapper.writeValueAsString(user))
            .compact();

        return new TokenDto()
            .data(new TokenDataDto().token(token)
                .expiration(now.toEpochMilli()));

    }

    boolean isValid(String token) {
        return getClaims(token)
            .filter(claims -> containsUsername.and(containsExpiration).test(claims))
            .isPresent();
    }

    Optional<String> getEmail(String token) {
        return getClaims(token).map(Claims::getSubject);
    }

    private Optional<Claims> getClaims(String token) {
        try {
            var body = Jwts.parser()
                .setSigningKey(securityProperty.getToken().getSecretKey())
                .parseClaimsJws(token)
                .getBody();
            return Optional.of(body);
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
