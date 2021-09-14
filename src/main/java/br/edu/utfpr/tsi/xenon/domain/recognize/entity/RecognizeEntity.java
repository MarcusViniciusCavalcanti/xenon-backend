package br.edu.utfpr.tsi.xenon.domain.recognize.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "recognizers")
@Data
public class RecognizeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "origin_ip")
    private String originIp;

    @Column(name = "epoch_time")
    private LocalDateTime epochTime;

    @Column(name = "plate", nullable = false, length = 10)
    private String plate;

    @Column(name = "confidence")
    private Float confidence;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "has_error")
    private Boolean hasError = Boolean.FALSE;

    @Column(name = "access_granted")
    private Boolean accessGranted;

    @Column(name = "driver_name")
    private String driverName;


    @PrePersist
    private void newRecognize() {
        this.createdAt = LocalDate.now();
    }
}
