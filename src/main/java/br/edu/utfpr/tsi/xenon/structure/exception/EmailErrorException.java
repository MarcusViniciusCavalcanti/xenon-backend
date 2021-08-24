package br.edu.utfpr.tsi.xenon.structure.exception;

import lombok.Getter;

public class EmailErrorException extends RuntimeException {

    @Getter
    private final String email;

    @Getter
    private final String code;

    public EmailErrorException(String email, String code) {
        super("E-mail %s está inválido, code de error: %s".formatted(email, code));
        this.email = email;
        this.code = code;
    }
}
