package br.edu.utfpr.tsi.xenon.structure.repository;

import br.edu.utfpr.tsi.xenon.domain.workstations.entity.WorkstationEntity;
import br.edu.utfpr.tsi.xenon.domain.workstations.entity.WorstationAndRecognizeSummary;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkstationRepository extends JpaRepository<WorkstationEntity, Long> {

    Boolean existsByIp(String ip);

    Boolean existsByName(String name);

    @Cacheable(cacheNames = "Workstation", key = "#key", unless = "#result == null")
    WorkstationEntity findIdByKey(String key);

    @Query("select w.id as id, w.name as name, (select count(rc.originIp) from RecognizeEntity rc where rc.originIp = w.ip) as recognizers from WorkstationEntity w")
    List<WorstationAndRecognizeSummary> getWorkstationSummary();
}
