package br.edu.utfpr.tsi.xenon.application.config.bean;

import static java.lang.Boolean.TRUE;

import br.edu.utfpr.tsi.xenon.application.config.property.FilesProperty;
import com.cloudinary.Cloudinary;
import java.util.Map;
import org.apache.tika.Tika;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConditionalOnProperty(value = {
    "xenon.configurations.file.secret",
    "xenon.configurations.file.key",
    "xenon.configurations.file.avatarUrl",
    "xenon.configurations.file.docUrl",
}, matchIfMissing = true)
public class CloudFileStorageConfiguration {

    @Bean
    public Cloudinary cloudinary(FilesProperty property) {
        return new Cloudinary(Map.of(
            "cloud_name", property.getName(),
            "api_key", property.getKey(),
            "api_secret", property.getSecret(),
            "secure", TRUE
        ));
    }

    @Bean
    public Tika tika() {
        return new Tika();
    }
}
