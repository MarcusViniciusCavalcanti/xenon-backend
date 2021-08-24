package br.edu.utfpr.tsi.xenon.domain.security.entity;

import static java.lang.Boolean.TRUE;

import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "access_card")
@Data
public class AccessCardEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "username", length = 200, nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "account_non_expired")
    private boolean accountNonExpired;

    @Column(name = "account_non_locked")
    private boolean accountNonLocked;

    @Column(name = "credentials_non_expired")
    private boolean credentialsNonExpired;

    @Column(name = "enabled")
    private boolean enabled;

    @ToString.Exclude
    @OneToOne(mappedBy = "accessCard", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private UserEntity user;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "access_card_has_roles",
        joinColumns = {@JoinColumn(name = "access_card_id")},
        inverseJoinColumns = {@JoinColumn(name = "roles_id")})
    private List<RoleEntity> roleEntities = Collections.emptyList();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roleEntities.stream()
            .flatMap(roleEntity -> Stream.of(roleEntity.getName()))
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }

    @PrePersist
    private void newAccessCard() {
        accountNonExpired = TRUE;
        accountNonLocked = TRUE;
        credentialsNonExpired = TRUE;

        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    private void updateAccessCard() {
        updatedAt = LocalDateTime.now();
    }
}
