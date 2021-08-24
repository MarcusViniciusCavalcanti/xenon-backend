package br.edu.utfpr.tsi.xenon.application.config.property;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("xenon.configurations.security")
public class SecurityProperty {

    private TokenConfiguration token;
    private Header header = new Header();

    public LocalDateTime expirationTimeDate() {
        return LocalDateTime.now().plusMinutes(token.expiration * 1000L);
    }

    @Data
    public static class TokenConfiguration {

        private int expiration = 3600;
        private String secretKey = "qxBEEQv7E8aviX1KUcdOiF5ve5COUPAr";
    }

    @Getter
    public static class Header {

        private final String name;

        private final String prefix;

        public Header() {
            this.name = "Authorization";
            this.prefix = "Bearer ";
        }
    }
}
