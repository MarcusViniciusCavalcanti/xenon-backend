package br.edu.utfpr.tsi.xenon.structure.repository;

import br.edu.utfpr.tsi.xenon.domain.recognize.entity.RecognizeEntity;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecognizerRepository extends JpaRepository<RecognizeEntity, Long> {

    Boolean existsByPlateAndEpochTimeBetween(String plate, LocalDateTime start, LocalDateTime end);
}
