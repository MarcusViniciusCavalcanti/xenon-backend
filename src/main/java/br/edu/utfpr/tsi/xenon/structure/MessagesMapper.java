package br.edu.utfpr.tsi.xenon.structure;

import lombok.Getter;

public enum MessagesMapper {
    KNOWN("ERROR-000"),
    EMAIL_EXIST("ERROR-002"),
    EMAIL_INVALID("ERROR-003"),
    TOKEN_NOT_MATCH("ERROR-004"),
    EMAIL_NOT_INSTITUTIONAL("ERROR-005"),
    ROLES_NOT_ALLOWS("ERROR-006"),
    PLATE_ALREADY("ERROR-007"),
    PASSWORD_INVALID("ERROR-008"),
    PLATE_INVALID("ERROR-009"),
    ACCESS_DENIED("ERROR-010"),
    UNAUTHORIZED("ERROR-011"),
    RESOURCE_NOT_FOUND("ERROR-012"),
    NAME_EXIST("ERROR-013"),
    NAME_CHANGE_ERROR("ERROR-014"),

    SEND_TOKEN("SUCCESS-001"),
    SEND_CONFIRM_NEW_PASSWORD("SUCCESS-002"),
    NAME_CHANGED_SUCCESSFULLY("SUCCESS-003"),
    SEND_NEW_PASSWORD("SUCCESS-004");

    @Getter
    private final String code;

    MessagesMapper(String code) {
        this.code = code;
    }
}
