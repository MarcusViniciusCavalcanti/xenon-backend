package br.edu.utfpr.tsi.xenon.domain.user.factory;

import static org.junit.jupiter.api.Assertions.*;

import br.edu.utfpr.tsi.xenon.application.dto.InputRegistryStudentDto;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class UserFactoryTest {

//    @ParameterizedTest(name = "deve criar usuário do tipo [{0}]")
//    @MethodSource("providerArgsUserCreated")
//    @DisplayName("Deve criar um usuário")
//    void shouldReturnUserEntity(InputUserDto input) {
//        var user = UserFactory.getInstance().create(input);
//        assertEquals(user.getTypeUser(), input.getTypeUser().name());
//    }

    @Test
    @DisplayName("Deve criar usuário tipo estudante")
    void shouldReturnUserEntityTypeStudent() {
        var user = UserFactory.getInstance().create(new InputRegistryStudentDto());

        assertEquals(user.getTypeUser(), TypeUser.STUDENTS.name());
    }

//    private static Stream<Arguments> providerArgsUserCreated() {
//        return Stream.of(
//            Arguments.of(new InputUserDto()
//                .typeUser(TypeUserEnum.fromValue(TypeUserEnum.SERVICE.name()))),
//            Arguments.of(new InputUserDto()
//                .typeUser(TypeUserEnum.fromValue(TypeUserEnum.STUDENTS.name()))),
//            Arguments.of(new InputUserDto()
//                .typeUser(TypeUserEnum.fromValue(TypeUserEnum.SPEAKER.name())))
//        );
//    }
}
