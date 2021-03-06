package br.edu.utfpr.tsi.xenon.application.service;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.awaitility.Durations.TWO_HUNDRED_MILLISECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.utfpr.tsi.xenon.application.config.property.ApplicationDomainProperty;
import br.edu.utfpr.tsi.xenon.application.dto.InputRegistryStudentDto;
import br.edu.utfpr.tsi.xenon.domain.notification.model.EmailTemplate;
import br.edu.utfpr.tsi.xenon.domain.notification.service.SenderEmailService;
import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.domain.security.entity.RoleEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.factory.TypeUser;
import br.edu.utfpr.tsi.xenon.domain.user.service.UserCreatorService;
import br.edu.utfpr.tsi.xenon.domain.user.service.ValidatorEmail;
import br.edu.utfpr.tsi.xenon.structure.exception.ResourceNotFoundException;
import br.edu.utfpr.tsi.xenon.structure.repository.UserRepository;
import com.github.javafaker.Faker;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - RegistryNewStudentsApplicationService")
class RegistryNewStudentsApplicationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ValidatorEmail validatorEmail;

    @Mock
    private UserCreatorService userCreatorService;

    @Mock
    private SenderEmailService senderEmailService;

    @Mock
    private ApplicationDomainProperty applicationDomainProperty;

    @InjectMocks
    private RegistryNewStudentsApplicationService registryNewStudentsApplicationService;

    @Test
    @DisplayName("Deve cadastrar com sucesso")
    void shouldHaveRegistrySuccessfully() {
        var faker = Faker.instance();
        var input = new InputRegistryStudentDto()
            .name(faker.name().fullName())
            .password("1234567")
            .confirmPassword("1234567")
            .email(faker.bothify("########@alunos.utfpr.edu.br"))
            .plateCar(faker.bothify("???####"))
            .modelCar(faker.rockBand().name());

        var role = new RoleEntity();
        var accessCard = new AccessCardEntity();
        accessCard.setRoleEntities(List.of(role));
        var user = new UserEntity();
        user.setAccessCard(accessCard);
        user.setTypeUser(TypeUser.STUDENTS.name());

        when(validatorEmail.isEmail(input.getEmail())).thenReturn(TRUE);
        when(validatorEmail.validateEmailStudents(input.getEmail())).thenReturn(TRUE);
        when(validatorEmail.isExistEmail(input.getEmail())).thenReturn(FALSE);
        when(userCreatorService.createNewStudents(input)).thenReturn(user);
        when(applicationDomainProperty.getDomain()).thenReturn("domain");
        doNothing()
            .when(senderEmailService)
            .sendEmail(any(EmailTemplate.class));

        registryNewStudentsApplicationService.registryNewStudents(input);

        verify(validatorEmail).isEmail(input.getEmail());
        verify(validatorEmail).validateEmailStudents(input.getEmail());
        verify(validatorEmail).isExistEmail(input.getEmail());
        verify(userCreatorService).createNewStudents(input);
        verify(senderEmailService, timeout(TWO_HUNDRED_MILLISECONDS.toMillis()))
            .sendEmail(any(EmailTemplate.class));
        verify(userRepository).saveAndFlush(user);
    }

    @Test
    @DisplayName("Deve ativar conta do usu??rio")
    void shouldHaveActivateAccount() {
        var email = Faker.instance().internet().emailAddress();
        var params = new String(
            Base64.getEncoder().encode(email.getBytes(StandardCharsets.UTF_8)),
            StandardCharsets.UTF_8
        );

        var role = new RoleEntity();
        var accessCard = new AccessCardEntity();
        accessCard.setRoleEntities(List.of(role));
        var user = new UserEntity();
        user.setAccessCard(accessCard);
        user.setTypeUser(TypeUser.STUDENTS.name());

        when(userRepository.findByAccessCardUsername(email)).thenReturn(Optional.of(user));
        when(userRepository.saveAndFlush(user)).thenReturn(user);

        registryNewStudentsApplicationService.activateAccount(params);

        verify(userRepository).findByAccessCardUsername(email);
        verify(userRepository).saveAndFlush(user);
    }

    @Test
    @DisplayName("Deve lan??ar ResourceNotFoundException quando usu??rio n??o existe")
    void shouldThrowsResourceNotFoundExceptionWhenUserNotFound() {
        var email = Faker.instance().internet().emailAddress();
        var params = new String(
            Base64.getEncoder().encode(email.getBytes(StandardCharsets.UTF_8)),
            StandardCharsets.UTF_8
        );

        when(userRepository.findByAccessCardUsername(email)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
            () -> registryNewStudentsApplicationService.activateAccount(params));
    }

    @Test
    @DisplayName("Deve retonar uma istancia do reposit??rio")
    void shouldReturnRepository() {
        var repository = registryNewStudentsApplicationService.getUserRepository();

        assertEquals(userRepository, repository);
    }
}
