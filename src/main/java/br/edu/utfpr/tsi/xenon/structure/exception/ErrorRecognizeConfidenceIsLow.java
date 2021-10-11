package br.edu.utfpr.tsi.xenon.structure.exception;

import br.edu.utfpr.tsi.xenon.application.dto.InputRecognizerDto;

public final class ErrorRecognizeConfidenceIsLow extends RecognizerError {

    private final String workstationName;
    private final transient InputRecognizerDto input;

    public ErrorRecognizeConfidenceIsLow(
        String workstationName,
        InputRecognizerDto input) {
        super("Reconhecimentos abaixo da confidencialidade [valor < 75.00]");
        this.workstationName = workstationName;
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
