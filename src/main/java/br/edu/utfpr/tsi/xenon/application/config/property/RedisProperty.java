package br.edu.utfpr.tsi.xenon.application.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("xenon.configurations.redis")
public class RedisProperty {

    private String host;
    private Integer port;
}
