package br.edu.utfpr.tsi.xenon.structure.repository;

import br.edu.utfpr.tsi.xenon.domain.recognize.entity.ErrorRecognizerEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErrorRecognizerRepository
    extends JpaRepository<ErrorRecognizerEntity, Long> {

    Optional<ErrorRecognizerEntity> findErrorRecognizerEntityByRecognizeId(Long id);
}
