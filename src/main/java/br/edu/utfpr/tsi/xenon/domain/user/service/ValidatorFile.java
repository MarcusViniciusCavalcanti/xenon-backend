package br.edu.utfpr.tsi.xenon.domain.user.service;

import static java.lang.Boolean.FALSE;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidatorFile {

    private static final Predicate<String> isNullType = Objects::nonNull;
    private static final Predicate<String> isPng = extension -> extension.endsWith("png");
    private static final Predicate<String> isJpg = extension -> extension.endsWith("jpg");
    private static final Predicate<String> isJpeg = extension -> extension.endsWith("jpeg");

    private final Tika tika;

    public Boolean validateAvatarFile(File file) {
        try {
            var extension = tika.detect(file);
            return isNullType.and(isPng.or(isJpg).or(isJpeg)).test(extension);
        } catch (IOException e) {
            log.debug("error executar validação causa: {}", e.getMessage());
        }

        return FALSE;
    }
}
