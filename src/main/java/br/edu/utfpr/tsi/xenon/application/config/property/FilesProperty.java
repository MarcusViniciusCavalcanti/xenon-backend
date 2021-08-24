package br.edu.utfpr.tsi.xenon.application.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("xenon.configurations.file")
public class FilesProperty {

    private String avatarUrl;
    private String docUrl;
    private String secret;
    private String key;
    private String name;
}
