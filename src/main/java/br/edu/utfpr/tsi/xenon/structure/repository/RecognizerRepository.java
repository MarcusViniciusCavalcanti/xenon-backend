package br.edu.utfpr.tsi.xenon.structure.repository;

import br.edu.utfpr.tsi.xenon.domain.recognize.entity.RecognizeEntity;
import br.edu.utfpr.tsi.xenon.domain.recognize.entity.RecognizerSummaryWeek;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface RecognizerRepository extends
    JpaRepository<RecognizeEntity, Long>, JpaSpecificationExecutor<RecognizeEntity> {

    Boolean existsByPlateAndEpochTimeBetween(String plate, LocalDateTime start, LocalDateTime end);

    Page<RecognizeEntity> findByAccessGrantedTrueAndPlateIn(
        List<String> plate,
        Pageable pageRequest);

    @Query(nativeQuery = true, value = """
            SELECT
                SUM(CASE WHEN rc.created_at = CURRENT_DATE - INTERVAL '7 DAY' then 1 else 0 end) as sevenDay,
                SUM(CASE WHEN rc.created_at = CURRENT_DATE - INTERVAL '6 DAY' then 1 else 0 end) as sixDay,
                SUM(CASE WHEN rc.created_at = CURRENT_DATE - INTERVAL '5 DAY' then 1 else 0 end) as fiveDay,
                SUM(CASE WHEN rc.created_at = CURRENT_DATE - INTERVAL '4 DAY' then 1 else 0 end) as fourDay,
                SUM(CASE WHEN rc.created_at = CURRENT_DATE - INTERVAL '3 DAY' then 1 else 0 end) as threeDay,
                SUM(CASE WHEN rc.created_at = CURRENT_DATE - INTERVAL '2 DAY' then 1 else 0 end) as twoDay,
                SUM(CASE WHEN rc.created_at = CURRENT_DATE - INTERVAL '1 DAY' then 1 else 0 end) as oneDay,
                SUM(CASE WHEN rc.created_at = CURRENT_DATE then 1 else 0 end) as now
            FROM recognizers rc
        """)
    RecognizerSummaryWeek getRecognizerSummaryWeek();

    @Transactional
    @Modifying
    @Query("UPDATE RecognizeEntity rc SET rc.accessGranted = 'true' WHERE rc.id = :recognizerId")
    void updateAccessAuthorized(Long recognizerId);
}
