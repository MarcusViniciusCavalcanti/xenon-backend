package br.edu.utfpr.tsi.xenon.application.config.bean;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ResteTemplateCoonfiguration {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
