package br.edu.utfpr.tsi.xenon.domain.user.entity;

public interface CarStateSummary {
    Long getWaiting();

    Long getApproved();

    Long getReproved();

    Long getBlock();
}
