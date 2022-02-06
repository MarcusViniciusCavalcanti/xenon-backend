package br.edu.utfpr.tsi.xenon.structure.repository;

import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserTypeSummary;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository
    extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {

    Optional<UserEntity> findByAccessCard(AccessCardEntity accessCard);

    Optional<UserEntity> findByAccessCardUsername(String username);

    Boolean existsByName(String name);

    @Query("""
        SELECT
            SUM(CASE WHEN u.typeUser = 'STUDENTS' then 1 else 0 end) as students,
            SUM(CASE WHEN u.typeUser = 'SPEAKER' then 1 else 0 end) as speakers,
            SUM(CASE WHEN u.typeUser = 'SERVICE' then 1 else 0 end) as services
        FROM UserEntity u
        """)
    UserTypeSummary getUserSummary();
}
