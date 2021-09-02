package br.edu.utfpr.tsi.xenon.domain.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Stream;
import org.apache.tika.Tika;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - ValidatorrFile")
class ValidatorFileTest {

    @Spy
    private Tika tika;

    @InjectMocks
    private ValidatorFile validatorFile;

    private static Stream<Arguments> providerFiletToTest() throws IOException, URISyntaxException {
        var folderValid = Paths.get(Objects
            .requireNonNull(ValidatorFileTest.class.getResource("/test-file/extention-test/valid/"))
            .toURI());
        var folderInvalid = Paths.get(Objects.requireNonNull(
            ValidatorFileTest.class.getResource("/test-file/extention-test/invalid/")).toURI());
        var valid = Files.walk(folderValid)
            .filter(path -> !Files.isDirectory(path))
            .map(path -> Arguments.arguments(path.toFile(), Boolean.TRUE));

        var invalids = Files.walk(folderInvalid)
            .map(path -> Arguments.arguments(path.toFile(), Boolean.FALSE));

        return Stream.concat(valid, invalids);
    }

    @ParameterizedTest
    @MethodSource("providerFiletToTest")
    @DisplayName("Deve validar se aquivo tem extensão de image permitidas [png, jpg, jpeg]")
    void shouldVerifyExtensionFile(File file, Boolean expected) {
        var result = validatorFile.validateAvatarFile(file);
        assertEquals(result, expected);
    }
}
