package br.edu.utfpr.tsi.xenon.structure.exception;

import br.edu.utfpr.tsi.xenon.application.dto.InputRecognizerDto;

public final class ErrorRecognizeOriginIpNotAllowed extends RecognizerError {

    private final String workstationName;
    private final InputRecognizerDto input;

    public ErrorRecognizeOriginIpNotAllowed(
        String originIp,
        String workstationName,
        InputRecognizerDto input) {
        super("Ip de origem diferente do ip %s vinculado a estação".formatted(originIp));
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
