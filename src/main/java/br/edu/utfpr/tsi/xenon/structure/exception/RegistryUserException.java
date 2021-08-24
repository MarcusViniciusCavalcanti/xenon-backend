package br.edu.utfpr.tsi.xenon.structure.exception;

import lombok.Getter;

public class RegistryUserException extends RuntimeException {

    @Getter
    private final String code;

    @Getter
    private final int statusCode;

    public RegistryUserException(String code) {
        super("Error no registro com code %s".formatted(code));
        this.code = code;
        this.statusCode = 422;
    }

    public RegistryUserException(String code, int statusCode) {
        super("Error no registro com code %s".formatted(code));
        this.code = code;
        this.statusCode = statusCode;
    }

}
