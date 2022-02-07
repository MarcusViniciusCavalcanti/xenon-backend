package br.edu.utfpr.tsi.xenon.domain.user.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "cars", indexes = @Index(columnList = "plate", name = "Index_car_plate"))
@Data
public class CarEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "plate", nullable = false, length = 10, unique = true)
    private String plate;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "document")
    private String document;

    @ManyToOne
    @EqualsAndHashCode.Exclude
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "last_access")
    private LocalDateTime lastAccess;

    @Column(name = "number_access")
    private Integer numberAccess;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "authorised_access")
    private Boolean authorisedAccess = Boolean.FALSE;

    @Column(name = "state")
    private String state;

    @Column(name = "reason_block")
    private String reasonBlock;

    @Enumerated(EnumType.STRING)
    private CarStatus carStatus;

    @PrePersist
    private void newCar() {
        createdAt = LocalDateTime.now();
    }
}
