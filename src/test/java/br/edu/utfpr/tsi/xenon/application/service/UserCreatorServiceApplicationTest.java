package br.edu.utfpr.tsi.xenon.application.service;

import static br.edu.utfpr.tsi.xenon.application.dto.InputUserDto.TypeUserEnum.SERVICE;
import static br.edu.utfpr.tsi.xenon.application.dto.InputUserDto.TypeUserEnum.SPEAKER;
import static br.edu.utfpr.tsi.xenon.application.dto.InputUserDto.TypeUserEnum.STUDENTS;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.awaitility.Durations.TWO_HUNDRED_MILLISECONDS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.utfpr.tsi.xenon.application.dto.InputUserDto;
import br.edu.utfpr.tsi.xenon.domain.notification.model.EmailTemplate;
import br.edu.utfpr.tsi.xenon.domain.notification.service.SenderEmailService;
import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.domain.security.entity.RoleEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.factory.TypeUser;
import br.edu.utfpr.tsi.xenon.domain.user.service.UserCreatorService;
import br.edu.utfpr.tsi.xenon.domain.user.service.ValidatorEmail;
import br.edu.utfpr.tsi.xenon.structure.repository.UserRepository;
import com.github.javafaker.Faker;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - UserServiceApplication")
class UserCreatorServiceApplicationTest {

    @Mock
    private ValidatorEmail validatorEmail;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder cryptPasswordEncoder;

    @Mock
    private UserCreatorService userCreatorService;

    @Mock
    private SenderEmailService senderEmailService;

    @InjectMocks
    private UserCreatorServiceApplication userCreatorServiceApplication;

    @ParameterizedTest
    @MethodSource("providerUser")
    @DisplayName("Deve criar usuário estudante com sucesso")
    void shouldCreateUserSuccessfully(InputUserDto input) {
        var role = new RoleEntity();
        var accessCard = new AccessCardEntity();
        accessCard.setRoleEntities(List.of(role));
        var user = new UserEntity();
        user.setAccessCard(accessCard);
        user.setTypeUser(TypeUser.STUDENTS.name());

        when(userRepository.existsByName(input.getName())).thenReturn(FALSE);
        when(validatorEmail.isEmail(input.getEmail())).thenReturn(TRUE);

        if (input.getTypeUser() == STUDENTS) {
            when(validatorEmail.validateEmailStudents(input.getEmail())).thenReturn(TRUE);
        }

        when(cryptPasswordEncoder.encode(any())).thenReturn("crypted");
        when(userCreatorService.createNewUser(eq(input), anyString())).thenReturn(user);
        when(userRepository.saveAndFlush(user)).thenReturn(user);
        doNothing()
            .when(senderEmailService)
            .sendEmail(any(EmailTemplate.class));

        userCreatorServiceApplication.createNewUser(input);
        verify(userRepository).existsByName(input.getName());
        verify(validatorEmail).isEmail(input.getEmail());

        if (input.getTypeUser() == STUDENTS) {
            verify(validatorEmail).validateEmailStudents(input.getEmail());
        } else {
            verify(validatorEmail, never()).validateEmailStudents(input.getEmail());
        }

        verify(validatorEmail).isExistEmail(input.getEmail());
        verify(userCreatorService).createNewUser(eq(input), anyString());
        verify(senderEmailService, timeout(TWO_HUNDRED_MILLISECONDS.toMillis()))
            .sendEmail(any(EmailTemplate.class));
        verify(userRepository).saveAndFlush(user);
    }

    private static Stream<Arguments> providerUser() {
        var faker = Faker.instance();

        var students= new InputUserDto()
            .name(faker.name().fullName())
            .email(faker.internet().emailAddress())
            .addRolesItem(1L)
            .typeUser(STUDENTS)
            .enabled(TRUE)
            .authorisedAccess(TRUE);

        var speaker= new InputUserDto()
            .name(faker.name().fullName())
            .email(faker.internet().emailAddress())
            .addRolesItem(1L)
            .typeUser(SPEAKER)
            .enabled(TRUE)
            .authorisedAccess(TRUE);

        var service= new InputUserDto()
            .name(faker.name().fullName())
            .email(faker.internet().emailAddress())
            .addRolesItem(1L)
            .addRolesItem(2L)
            .addRolesItem(3L)
            .typeUser(SERVICE)
            .enabled(TRUE)
            .authorisedAccess(TRUE);

        return Stream.of(
            Arguments.of(students),
            Arguments.of(speaker),
            Arguments.of(service)
        );
    }
}

