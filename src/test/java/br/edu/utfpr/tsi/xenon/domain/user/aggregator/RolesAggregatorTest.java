package br.edu.utfpr.tsi.xenon.domain.user.aggregator;

import static org.mockito.Mockito.verify;

import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.domain.user.factory.TypeUser;
import br.edu.utfpr.tsi.xenon.structure.repository.RoleRepository;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - RolesAggregator")
class RolesAggregatorTest {

    @Mock
    private RoleRepository repository;

    @InjectMocks
    private RolesAggregator rolesAggregator;

    private static Stream<Arguments> providerArgsToIncludeRoles() {
        return Stream.of(
            Arguments.of(TypeUser.STUDENTS, List.of(1L), List.of(1L)),
            Arguments.of(TypeUser.STUDENTS, List.of(1L, 2L), List.of(1L)),
            Arguments.of(TypeUser.STUDENTS, List.of(1L, 2L, 3L), List.of(1L)),

            Arguments.of(TypeUser.SPEAKER, List.of(1L), List.of(1L)),
            Arguments.of(TypeUser.SPEAKER, List.of(1L, 2L), List.of(1L)),
            Arguments.of(TypeUser.SPEAKER, List.of(1L, 3L), List.of(1L)),

            Arguments.of(TypeUser.SERVICE, List.of(1L, 3L), List.of(1L, 3L)),
            Arguments.of(TypeUser.SERVICE, List.of(1L, 2L), List.of(1L, 2L)),
            Arguments.of(TypeUser.SERVICE, List.of(2L, 3L), List.of(2L, 3L))
        );
    }

    @ParameterizedTest(name = "tipo de estudande {0} deve conter as roles de id {2}")
    @MethodSource("providerArgsToIncludeRoles")
    @DisplayName("Deve retonar apenas as roles permitidas por tipos de usuários")
    void shouldReturnRoesAllows(TypeUser type, List<Long> roles, List<Long> expected) {
        rolesAggregator.includeRoles(new AccessCardEntity(), type, roles);
        verify(repository).findAllById(expected);
    }
}
