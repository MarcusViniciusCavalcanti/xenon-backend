package br.edu.utfpr.tsi.xenon.domain.recognize.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "error_recognizer")
@Data
public class ErrorRecognizerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "origin_ip")
    private String originIp;

    @Column(name = "workstation_name")
    private String workstationName;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "input")
    private String input;

    @Lob
    private String trace;
}
