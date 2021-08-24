package br.edu.utfpr.tsi.xenon;

import br.edu.utfpr.tsi.xenon.application.config.property.ApplicationDomainProperty;
import br.edu.utfpr.tsi.xenon.application.config.property.EmailProperty;
import br.edu.utfpr.tsi.xenon.application.config.property.FilesProperty;
import br.edu.utfpr.tsi.xenon.application.config.property.RedisProperty;
import br.edu.utfpr.tsi.xenon.application.config.property.SecurityProperty;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(
    exclude = {RedisAutoConfiguration.class}
)
@EnableConfigurationProperties(value = {
    SecurityProperty.class,
    EmailProperty.class,
    RedisProperty.class,
    ApplicationDomainProperty.class,
    FilesProperty.class
})
public class XenonBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(XenonBackendApplication.class, args);
    }

}
