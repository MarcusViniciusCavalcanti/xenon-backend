package br.edu.utfpr.tsi.xenon.domain.recognize.service;

import br.edu.utfpr.tsi.xenon.domain.recognize.entity.RecognizeEntity;
import java.util.Locale;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RecognizerSpecifications {

    private static final String WILD_CARD = "%";

    public static Specification<RecognizeEntity> nameDriver(String driverName) {
        if (StringUtils.isBlank(driverName)) {
            return null;
        }

        return (root, criteriaQuery, criteriaBuilder) ->
            criteriaBuilder.like(
                criteriaBuilder.lower(root.get("driverName")),
                criteriaBuilder
                    .lower(criteriaBuilder.literal(concatenateKeyValueWithWildCard(driverName)))
            );
    }

    public static Specification<RecognizeEntity> onlyError(Boolean onlyError) {
        if (Objects.isNull(onlyError)) {
            return null;
        }

        return (root, criteriaQuery, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("hasError"), onlyError);
    }

    private static String concatenateKeyValueWithWildCard(String value) {
        return WILD_CARD + value.toLowerCase(Locale.getDefault()) + WILD_CARD;
    }
}
