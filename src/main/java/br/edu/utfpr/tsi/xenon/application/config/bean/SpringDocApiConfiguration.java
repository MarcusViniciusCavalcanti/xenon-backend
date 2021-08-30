package br.edu.utfpr.tsi.xenon.application.config.bean;

import org.springdoc.core.SpringDocConfigProperties;
import org.springdoc.core.SpringDocConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocApiConfiguration {

    @Bean
    SpringDocConfiguration springDocConfiguration() {
        return new SpringDocConfiguration();
    }

    @Bean
    public SpringDocConfigProperties springDocConfigProperties() {
        return new SpringDocConfigProperties();
    }
}
