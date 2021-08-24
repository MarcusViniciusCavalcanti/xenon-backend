package br.edu.utfpr.tsi.xenon.domain.user.aggregator;

import static java.lang.Boolean.FALSE;

import br.edu.utfpr.tsi.xenon.application.config.property.FilesProperty;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AvatarAggregator {

    private final Cloudinary cloudinary;
    private final FilesProperty property;

    public void includeDefaultAvatarUrl(UserEntity userEntity) {
        var url = cloudinary.url()
            .cloudName(property.getName())
            .version("1629230914")
            .publicId("%s/defaultUser".formatted(property.getAvatarUrl().trim()))
            .format("png")
            .resourceType("image")
            .secure(FALSE)
            .generate();

        userEntity.setAvatar(url);
    }
}
