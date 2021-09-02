package br.edu.utfpr.tsi.xenon.domain.user.service;

import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.structure.ParamsQuerySearchUserDto;
import br.edu.utfpr.tsi.xenon.structure.repository.BasicSpecification;
import javax.validation.constraints.NotNull;
import org.springframework.data.jpa.domain.Specification;

public class GetterAllUserSpec implements BasicSpecification<UserEntity, ParamsQuerySearchUserDto> {

    @Override
    public Specification<UserEntity> filterBy(@NotNull final ParamsQuerySearchUserDto spec) {
        return (root, query, builder) -> {
            query.distinct(true);
            return Specification.where(
                UserSpecifications.nameOrUsernameContains(spec.getNameOrEmail()))
                .and(UserSpecifications.type(spec.getType()))
                .toPredicate(root, query, builder);
        };
    }
}
