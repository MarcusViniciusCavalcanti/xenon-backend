package br.edu.utfpr.tsi.xenon.structure.repository;

import br.edu.utfpr.tsi.xenon.domain.workstations.entity.WorkstationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkstationRepository extends JpaRepository<WorkstationEntity, Long> {

    Boolean existsByIp(String ip);

    Boolean existsByName(String name);
}
