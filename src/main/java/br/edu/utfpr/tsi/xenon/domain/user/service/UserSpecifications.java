package br.edu.utfpr.tsi.xenon.domain.user.service;

import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserSpecifications {

    private static final String WILD_CARD = "%";
    private static final String ACCESS_CARD = "accessCard";

    public static Specification<UserEntity> nameOrUsernameContains(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        return (root, criteriaQuery, criteriaBuilder) ->
            criteriaBuilder.or(
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    criteriaBuilder
                        .lower(criteriaBuilder.literal(concatenateKeyValueWithWildCard(value)))
                ),
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get(ACCESS_CARD).get("username")),
                    criteriaBuilder
                        .lower(criteriaBuilder.literal(concatenateKeyValueWithWildCard(value)))
                )
            );
    }

    public static Specification<UserEntity> type(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder
            .equal(root.get("typeUser"), value);
    }

    private static String concatenateKeyValueWithWildCard(String value) {
        return WILD_CARD + value.toLowerCase(Locale.getDefault()) + WILD_CARD;
    }
}
