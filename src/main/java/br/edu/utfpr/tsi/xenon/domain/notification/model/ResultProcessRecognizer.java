package br.edu.utfpr.tsi.xenon.domain.notification.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public final class ResultProcessRecognizer {
    private final Long recognizerId;
    private final String driverName;
    private final Boolean identifier;
    private final Boolean authorize;
    private final Float confidence;
    private final String userAvatar;
    private final String plate;
    private final String modelCar;
}
