package br.edu.utfpr.tsi.xenon.structure;

import lombok.Getter;

public enum MessagesMapper {
    KNOWN("ERROR-000"),
    ARGUMENT_INVALID("ERROR-001"),
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
    LIMIT_EXCEEDED_CAR("ERROR-015"),
    PASS_AND_CONFIRM_NOT_MATCH("ERROR-016"),
    PASS_ACTUAL_NOT_MATCH("ERROR-017"),
    FILE_ALLOWED("ERROR-018"),
    REASON_IS_EMPTY("ERROR-019"),

    SEND_TOKEN("SUCCESS-001"),
    SEND_CONFIRM_NEW_PASSWORD("SUCCESS-002"),
    NAME_CHANGED_SUCCESSFULLY("SUCCESS-003"),
    SEND_NEW_PASSWORD("SUCCESS-004"),
    ACTIVATE_ACCOUNT("SUCCESS-005"),
    CHANGE_PASS_SUCCESSFULLY("SUCCESS-006"),
    REMOVE_AUTHORIZATION_ACCESS("SUCCESS-007"),
    USER_ACCOUNT_DEACTIVATED("SUCCESS-008"),
    ADD_AUTHORIZATION_ACCESS("SUCCESS-009");

    @Getter
    private final String code;

    MessagesMapper(String code) {
        this.code = code;
    }
}
