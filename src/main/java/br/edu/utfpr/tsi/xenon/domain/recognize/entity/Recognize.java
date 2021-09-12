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
public class Recognize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "origin")
    private String origin;

    @Column(name = "epoch_time")
    private LocalDateTime epochTime;

    @Column(name = "plate", nullable = false, length = 10)
    private String plate;

    @Column(name = "confidence")
    private Float confidence;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @PrePersist
    private void newRecognize() {
        this.createdAt = LocalDate.now();
    }
}
