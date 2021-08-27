package br.edu.utfpr.tsi.xenon.structure.exception;

import lombok.Getter;

public class BusinessException extends RuntimeException {

    @Getter
    private final Integer status;

    @Getter
    private final String code;

    @Getter
    private final String[] args;

    public BusinessException(Integer status, String code, String ...args) {
        super("Erro de negocio código: %s".formatted(code));
        this.status = status;
        this.code = code;
        this.args = args;
    }
}
