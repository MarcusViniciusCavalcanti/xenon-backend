package br.edu.utfpr.tsi.xenon.domain.notification.model;

public interface EmailTemplate {

    String getTemplate();

    String subject();

    String to();
}
