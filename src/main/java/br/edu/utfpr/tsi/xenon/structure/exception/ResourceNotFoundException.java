package br.edu.utfpr.tsi.xenon.structure.exception;

import lombok.Getter;

public class ResourceNotFoundException extends RuntimeException {

    private static final String MESSAGE_PATTERN =
        "Recurso %s com o parâmetro [%s] não foi encontrado";

    @Getter
    private final String resourceName;

    @Getter
    private final String argumentSearch;

    public ResourceNotFoundException(String resourceName, String argumentSearch) {
        super(String.format(MESSAGE_PATTERN, resourceName, argumentSearch));
        this.resourceName = resourceName;
        this.argumentSearch = argumentSearch;
    }
}
