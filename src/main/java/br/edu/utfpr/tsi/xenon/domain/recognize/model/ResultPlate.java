package br.edu.utfpr.tsi.xenon.domain.recognize.model;

import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;

public record ResultPlate(
    CarEntity carEntity,
    Float confidence) { }
