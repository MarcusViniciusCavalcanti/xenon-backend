package br.edu.utfpr.tsi.xenon.application.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("xenon.configurations.application")
public class ApplicationDomainProperty {
    private String domain;
}
