package br.edu.utfpr.tsi.xenon.domain.security.service;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.KeyGenerator;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Component
public class KeyService {

    public String createKey() throws NoSuchAlgorithmException {
        var rand = new SecureRandom();
        var generator = KeyGenerator.getInstance("AES");

        generator.init(256, rand);
        var encoded = Base64.getEncoder().encode(generator.generateKey().getEncoded());

        var chars = new String(encoded, StandardCharsets.UTF_8).toCharArray();
        return RandomStringUtils.random(
            25,
            0,
            chars.length,
            true,
            false,
            chars
        );
    }
}
