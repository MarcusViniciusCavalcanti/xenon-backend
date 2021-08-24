package br.edu.utfpr.tsi.xenon.structure.repository;

import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessCardRepository extends JpaRepository<AccessCardEntity, Long> {

    Optional<AccessCardEntity> findByUsername(String username);

    Boolean existsByUsername(String username);

}
