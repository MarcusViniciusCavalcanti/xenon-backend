package br.edu.utfpr.tsi.xenon.domain.user.aggregator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.utfpr.tsi.xenon.application.config.property.FilesProperty;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import com.cloudinary.Cloudinary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - AvatarAggregator")
class AvatarAggregatorTest {

    @Spy
    private Cloudinary cloudinary;

    @Mock
    private FilesProperty filesProperty;

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
}
