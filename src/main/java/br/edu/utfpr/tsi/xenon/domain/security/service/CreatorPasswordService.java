package br.edu.utfpr.tsi.xenon.domain.security.service;

import static java.lang.Boolean.TRUE;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public record CreatorPasswordService(
    BCryptPasswordEncoder cryptPasswordEncoder) {

    public static CreatorPasswordService newInstance(BCryptPasswordEncoder passwordEncoder) {
        return new CreatorPasswordService(passwordEncoder);
    }

    public Pass createPass() {
        var newPassword = RandomStringUtils.random(8, TRUE, TRUE);
        return new Pass(newPassword, cryptPasswordEncoder.encode(newPassword));
    }

    public record Pass(String pass, String encoderPass) {

    }
}
