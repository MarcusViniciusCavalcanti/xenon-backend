package br.edu.utfpr.tsi.xenon.domain.notification.model;

import static java.lang.Boolean.FALSE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.util.Assert;

@RequiredArgsConstructor
public final class TokenApplication {

    @Getter
    private final String key;

    @Getter
    private String token;

    public void generateNewToken() {
        Assert.state(isNotBlank(key), "key não pode ser nulo");
        var params = "%s%s".formatted(key, RandomStringUtils.randomAlphabetic(10, 20));
        token = UUID.nameUUIDFromBytes(params.getBytes(StandardCharsets.UTF_8)).toString();
    }

    public Boolean validateToken(String actual, String expected) {
        if (isBlank(expected)) {
            return FALSE;
        }

        if (isBlank(actual)) {
            return FALSE;
        }

        if (expected.length() > 36) {
            return FALSE;
        }

        if (actual.length() <= 36) {
            var uuidActual = UUID.fromString(actual.trim());
            var uuidExpected = UUID.fromString(expected.trim());

            return uuidActual.compareTo(uuidExpected) == 0;
        }

        return FALSE;
    }

    public static TokenApplication newInstance(String email) {
        return new TokenApplication(email);
    }

    public static TokenApplication newInstance() {
        return new TokenApplication(null);
    }

}
