package br.edu.utfpr.tsi.xenon.structure.exception;

import lombok.Getter;

public class PlateException extends RuntimeException {

    @Getter
    private final String code;

    @Getter
    private final String plate;

    public PlateException(String plate, String code) {
        super("Plate %s exist".formatted(plate));
        this.code = code;
        this.plate = plate;
    }
}
