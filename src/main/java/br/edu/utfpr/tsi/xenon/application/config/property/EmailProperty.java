package br.edu.utfpr.tsi.xenon.application.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Data
@ConfigurationProperties("xenon.configurations.email")
public class EmailProperty {

    private String host;
    private Integer port;
    private String protocol;
    private String username;
    private String password;
    private Boolean auth;

    @NestedConfigurationProperty
    private TlSConfiguration tls;

    @Data
    public static class TlSConfiguration {

        private Boolean enable;
        private Boolean required;
    }
}
