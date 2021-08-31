package br.edu.utfpr.tsi.xenon.domain.user.aggregator;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.utfpr.tsi.xenon.application.config.property.FilesProperty;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.service.ValidatorFile;
import br.edu.utfpr.tsi.xenon.structure.MessagesMapper;
import br.edu.utfpr.tsi.xenon.structure.exception.BusinessException;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - AvatarAggregator")
class AvatarAggregatorTest {

    @Spy
    private Cloudinary cloudinary;

    @Mock
    private FilesProperty filesProperty;

    @Mock
    private ValidatorFile validatorFile;

    @InjectMocks
    private AvatarAggregator avatarAggregator;

    @Test
    @DisplayName("Deve retonar url de avatar padrão")
    void shouldReturnUrlAvatarDefault() {
        when(filesProperty.getAvatarUrl()).thenReturn("avatar");
        when(filesProperty.getName()).thenReturn("cname");

        var userEntity = new UserEntity();
        avatarAggregator.includeDefaultAvatarUrl(userEntity);

        assertNotNull(userEntity.getAvatar());

        verify(cloudinary).url();
    }

    @Test
    @DisplayName("Deve incluir avatar com sucesso")
    void shouldIncludeAvatarSuccessfully() throws IOException {
        var uploader = mock(Uploader.class);

        var userEntity = new UserEntity();
        var avatar = Files.createTempFile("test", ".png").toFile();
        var url = "url";

        when(filesProperty.getAvatarUrl()).thenReturn("avatar");
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(eq(avatar), any())).thenReturn(Map.of("url", url));
        when(validatorFile.validateAvatarFile(avatar)).thenReturn(TRUE);

        avatarAggregator.includeAvatar(avatar, userEntity);

        assertNotNull(userEntity.getAvatar());
        assertEquals(url, userEntity.getAvatar());

        verify(uploader).upload(eq(avatar), any());
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando arquivo não possou na validação")
    void shouldThrowBusinessExceptionWhenFileInvalid() throws IOException {
        var uploader = mock(Uploader.class);

        var userEntity = new UserEntity();
        var avatar = Files.createTempFile("test", ".png").toFile();

        when(filesProperty.getAvatarUrl()).thenReturn("avatar");
        when(validatorFile.validateAvatarFile(avatar)).thenReturn(FALSE);

        var exception = assertThrows(BusinessException.class,
            () -> avatarAggregator.includeAvatar(avatar, userEntity));

        assertNull(userEntity.getAvatar());
        assertEquals(MessagesMapper.FILE_ALLOWED.getCode(), exception.getCode());
        verify(uploader, never()).upload(eq(avatar), any());
    }
}
