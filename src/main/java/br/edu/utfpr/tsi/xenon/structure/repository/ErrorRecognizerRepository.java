package br.edu.utfpr.tsi.xenon.structure.repository;

import br.edu.utfpr.tsi.xenon.domain.recognize.entity.ErrorRecognizerEntity;
import java.util.Optional;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ErrorRecognizerRepository
    extends PagingAndSortingRepository<ErrorRecognizerEntity, Long> {

    Optional<ErrorRecognizerEntity> findErrorRecognizerEntityByRecognizeId(Long id);
}
