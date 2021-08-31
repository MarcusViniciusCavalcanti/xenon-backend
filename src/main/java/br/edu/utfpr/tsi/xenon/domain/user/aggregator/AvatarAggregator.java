package br.edu.utfpr.tsi.xenon.domain.user.aggregator;

import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.FILE_ALLOWED;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import br.edu.utfpr.tsi.xenon.application.config.property.FilesProperty;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.service.ValidatorFile;
import br.edu.utfpr.tsi.xenon.structure.exception.BusinessException;
import com.cloudinary.Cloudinary;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AvatarAggregator {

    private final Cloudinary cloudinary;
    private final FilesProperty property;
    private final ValidatorFile validatorFile;

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

    public void includeAvatar(File avatar, UserEntity userEntity) throws IOException {
        var options = Map.of(
            "resource_type", "image",
            "public_id", "%d".formatted(userEntity.getId()),
            "folder", property.getAvatarUrl(),
            "filename_override", TRUE,
            "use_filename", TRUE,
            "overwrite", TRUE
        );

        if (FALSE.equals(validatorFile.validateAvatarFile(avatar))) {
            throw new BusinessException(400, FILE_ALLOWED.getCode(), "png, jpeg, jpg");
        }

        var url = (String) cloudinary.uploader().upload(avatar, options).get("url");
        userEntity.setAvatar(url);
    }

}
