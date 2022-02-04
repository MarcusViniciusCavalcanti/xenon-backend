package br.edu.utfpr.tsi.xenon.domain.user.entity;

import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import lombok.EqualsAndHashCode.Exclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "users")
@Getter
@Setter
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @OneToOne
    @MapsId
    @Exclude
    @JoinColumn(name = "access_card_id")
    private AccessCardEntity accessCard;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "type")
    private String typeUser;

    @Column(name = "authorised_access")
    private Boolean authorisedAccess = Boolean.TRUE;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "disable_account_reason")
    private String disableReason;

    @ToString.Exclude
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<CarEntity> car = new ArrayList<>();

    @PrePersist
    private void newAccessCard() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    public CarEntity lastCar() {
        return car.stream()
            .reduce((first, second) -> second)
            .orElse(new CarEntity());
    }

    public void includeLastCar(CarEntity carEntity) {
        if (car.isEmpty()) {
            car.add(0, carEntity);
        } else {
            car.add(car.size(), carEntity);
        }
    }

    public CarEntity firstCar() {
        return car.get(0);
    }

    @PreUpdate
    private void updateAccessCard() {
        updatedAt = LocalDateTime.now();
    }

}
