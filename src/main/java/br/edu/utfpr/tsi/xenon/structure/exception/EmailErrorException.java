package br.edu.utfpr.tsi.xenon.structure.exception;

import lombok.Getter;

public class EmailErrorException extends RuntimeException {

    @Getter
    private final String email;

    @Getter
    private final String code;

    @Getter
    private final Integer httpStatusCode;

    public EmailErrorException(String email, String code, Integer httpStatusCode) {
        super("E-mail %s está inválido, code de error: %s".formatted(email, code));
        this.email = email;
        this.code = code;
        this.httpStatusCode = httpStatusCode;
    }
}
