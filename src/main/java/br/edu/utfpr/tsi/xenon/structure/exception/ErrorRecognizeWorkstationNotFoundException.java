package br.edu.utfpr.tsi.xenon.structure.exception;

import br.edu.utfpr.tsi.xenon.application.dto.InputRecognizerDto;

public final class ErrorRecognizeWorkstationNotFoundException extends RecognizerError {

    private final String workstationName;
    private final InputRecognizerDto input;

    public ErrorRecognizeWorkstationNotFoundException(String key, InputRecognizerDto input) {
        super("Estação de trabalho para key %s não encontrada".formatted(key));
        this.workstationName = "Não encontrada";
        this.input = input;
    }

    @Override
    public String getWorkstationName() {
        return workstationName;
    }

    @Override
    public InputRecognizerDto getInput() {
        return input;
    }
}
