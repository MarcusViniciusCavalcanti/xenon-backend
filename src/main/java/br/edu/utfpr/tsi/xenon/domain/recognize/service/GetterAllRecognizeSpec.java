package br.edu.utfpr.tsi.xenon.domain.recognize.service;

import br.edu.utfpr.tsi.xenon.domain.recognize.entity.RecognizeEntity;
import br.edu.utfpr.tsi.xenon.structure.ParamsQuerySearchRecognizeDto;
import br.edu.utfpr.tsi.xenon.structure.repository.BasicSpecification;
import javax.validation.constraints.NotNull;
import org.springframework.data.jpa.domain.Specification;

public class GetterAllRecognizeSpec implements
    BasicSpecification<RecognizeEntity, ParamsQuerySearchRecognizeDto> {

    @Override
    public Specification<RecognizeEntity> filterBy(
        @NotNull final ParamsQuerySearchRecognizeDto params) {
        return (root, query, builder) -> {
            query.distinct(true);
            return Specification.where(
                RecognizerSpecifications.nameDriver(params.getDriverName()))
                .and(RecognizerSpecifications.onlyError(params.getOnlyError()))
                .toPredicate(root, query, builder);
        };
    }
}
