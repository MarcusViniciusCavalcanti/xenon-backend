package br.edu.utfpr.tsi.xenon.domain.workstations.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "workstation", indexes = @Index(columnList = "ip", name = "Index_ip_workstation"))
@Data
public class WorkstationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ip", unique = true, nullable = false)
    private String ip;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "mode", nullable = false)
    private String mode;
}
