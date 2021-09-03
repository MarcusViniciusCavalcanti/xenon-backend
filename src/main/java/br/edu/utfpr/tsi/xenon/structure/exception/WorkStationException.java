package br.edu.utfpr.tsi.xenon.structure.exception;

import lombok.Getter;

public class WorkStationException extends RuntimeException {
    @Getter
    private final String code;

    @Getter
    private final String value;

    public WorkStationException(String code, String value) {
        super("Error na manipular workstation: code: [%s] value error: [%s]"
            .formatted(code, value));
        this.code = code;
        this.value = value;
    }
}
