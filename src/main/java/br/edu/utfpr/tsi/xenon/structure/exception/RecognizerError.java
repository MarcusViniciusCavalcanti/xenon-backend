package br.edu.utfpr.tsi.xenon.structure.exception;

import br.edu.utfpr.tsi.xenon.application.dto.InputRecognizerDto;

public abstract class RecognizerError extends RuntimeException {

    protected RecognizerError(String msg) {
        super(msg);
    }

    public abstract String getWorkstationName();

    public abstract InputRecognizerDto getInput();

}
