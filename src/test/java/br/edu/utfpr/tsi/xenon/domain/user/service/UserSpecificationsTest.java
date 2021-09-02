package br.edu.utfpr.tsi.xenon.domain.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.factory.TypeUser;
import java.util.Locale;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("Test - Unidade - UserSpecification")
@ExtendWith(MockitoExtension.class)
class UserSpecificationsTest {

    private static final String WILD_CARD = "%";

    @Mock
    private CriteriaBuilder builder;

    @SuppressWarnings("rawtypes")
    @Mock
    private CriteriaQuery query;

    @Mock
    private Root<UserEntity> root;

    @Test
    @DisplayName("Deve retornar Specification de nomes ou username null quando não enviado o argumento")
    void shouldReturnSpecificationNullWhenValueIsNull() {
        var predicate = UserSpecifications.nameOrUsernameContains(null);
        assertNull(predicate);
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", ""})
    @DisplayName("Deve retonar Specification de nomes ou username null quando enviado o argumento é invalido")
    void shouldReturnSpecificationNullWhenValueIsInvalid(String value) {
        var predicate = UserSpecifications.nameOrUsernameContains(value);
        assertNull(predicate);
    }

    @Test
    @DisplayName("Deve retornar Specification com nome ou username")
    void shouldReturnSpecificationUserOrName() {
        var pathAccessCard = mock(Path.class);
        var pathUsername = mock(Path.class);
        var pathUser = mock(Path.class);

        //noinspection unchecked
        when(root.get("name")).thenReturn(pathUser);
        //noinspection unchecked
        when(root.get("accessCard")).thenReturn(pathAccessCard);
        when(pathAccessCard.get("username")).thenReturn(pathUsername);

        var value = "name";
        //noinspection ConstantConditions
        UserSpecifications.nameOrUsernameContains(value).toPredicate(root, query, builder);

        verify(builder).lower(root.get("name"));
        verify(builder).lower(root.get("accessCard").get("username"));
        verify(builder, times(2)).lower(builder.literal(concatenateKeyValueWithWildCard(value)));
        verify(builder).like(builder.lower(root.get("name")),
            builder.lower(builder.literal(concatenateKeyValueWithWildCard(value))));
        verify(builder).like(builder.lower(root.get("accessCard").get("username")),
            builder.lower(builder.literal(concatenateKeyValueWithWildCard(value))));
        verify(builder).or(builder.like(builder.lower(root.get("name")),
            builder.lower(builder.literal(concatenateKeyValueWithWildCard(value)))),
            builder.like(builder.lower(root.get("accessCard").get("username")),
                builder.lower(builder.literal(concatenateKeyValueWithWildCard(value)))
            ));
    }

    @Test
    @DisplayName("Deve retornar Specification de tipos de usuários null quando não enviado o argumento")
    void shouldReturnSpecificationTypeUserNullWhenValueIsNull() {
        var predicate = UserSpecifications.type(null);
        assertNull(predicate);
    }

    @Test
    @DisplayName("Deve retonar Specification de tipos de usuários")
    void shouldReturnSpecificationType() {
        var pathTypeUser = mock(Path.class);

        var typeUser = TypeUser.SERVICE.name();
        //noinspection unchecked
        when(root.get("typeUser")).thenReturn(pathTypeUser);

        //noinspection ConstantConditions
        UserSpecifications.type(typeUser).toPredicate(root, query, builder);

        verify(builder).equal(root.get("typeUser"), typeUser);
    }

    private String concatenateKeyValueWithWildCard(String value) {
        return WILD_CARD + value.toLowerCase(Locale.getDefault()) + WILD_CARD;
    }
}
