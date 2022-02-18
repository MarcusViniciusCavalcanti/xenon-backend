package br.edu.utfpr.tsi.xenon.structure.repository;

import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarStateSummary;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CarRepository extends JpaRepository<CarEntity, Long> {

    void deleteByUser(UserEntity user);

    void deleteByUserAndPlate(UserEntity user, String plate);

    List<CarEntity> findByUser(UserEntity userDisabled);

    List<CarEntity> findAllByPlateIn(List<String> plates);

    List<CarEntity> findByUserId(Long id);

    @Query("""
        select
            SUM(CASE WHEN c.carStatus = br.edu.utfpr.tsi.xenon.domain.user.entity.CarStatus.WAITING then 1 else 0 end) as waiting,
            SUM(CASE WHEN c.carStatus = br.edu.utfpr.tsi.xenon.domain.user.entity.CarStatus.APPROVED then 1 else 0 end) as approved,
            SUM(CASE WHEN c.carStatus = br.edu.utfpr.tsi.xenon.domain.user.entity.CarStatus.REPROVED then 1 else 0 end) as reproved,
            SUM(CASE WHEN c.carStatus = br.edu.utfpr.tsi.xenon.domain.user.entity.CarStatus.BLOCK then 1 else 0 end) as block
        from CarEntity c""")
    CarStateSummary getCarsSummary();

    Page<CarEntity> findAllByState(String state, Pageable pageable);

    Boolean existsAllByState(String stats);
}
