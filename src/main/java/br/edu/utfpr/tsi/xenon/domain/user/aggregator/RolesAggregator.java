package br.edu.utfpr.tsi.xenon.domain.user.aggregator;

import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.domain.user.factory.TypeUser;
import br.edu.utfpr.tsi.xenon.structure.repository.RoleRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RolesAggregator {

    private final RoleRepository repository;

    public void includeRoles(AccessCardEntity accessCard, TypeUser typeUser, List<Long> ids) {
        enum Types {
            STUDENTS(List.of(1L)),
            SPEAKER(List.of(1L)),
            SERVICE(List.of(1L, 2L, 3L));

            private static final Map<String, Types> values = Map.of(
                "STUDENTS", STUDENTS,
                "SPEAKER", SPEAKER,
                "SERVICE", SERVICE
            );

            private final List<Long> allowedRoles;

            Types(List<Long> allowedRoles) {
                this.allowedRoles = allowedRoles;
            }

            public static Types getOrDefaultType(String name) {
                return values.getOrDefault(name, STUDENTS);
            }
        }

        var allowedRoles = Types.getOrDefaultType(typeUser.name()).allowedRoles;
        var rolesIds = ids.stream()
            .filter(allowedRoles::contains)
            .toList();

        var roles = repository.findAllById(rolesIds);
        accessCard.setRoleEntities(roles);
    }
}
