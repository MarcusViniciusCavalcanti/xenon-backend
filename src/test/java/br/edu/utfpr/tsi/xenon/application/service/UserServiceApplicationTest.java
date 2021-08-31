package br.edu.utfpr.tsi.xenon.application.service;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.utfpr.tsi.xenon.domain.user.service.ValidatorEmail;
import br.edu.utfpr.tsi.xenon.structure.MessagesMapper;
import br.edu.utfpr.tsi.xenon.structure.exception.EmailErrorException;
import br.edu.utfpr.tsi.xenon.structure.exception.RegistryUserException;
import br.edu.utfpr.tsi.xenon.structure.repository.UserRepository;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - UserServiceRegistryApplication")
class UserServiceApplicationTest {

    @Mock
    private ValidatorEmail validatorEmail;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("Deve lançar EmailErrorException quando e-mail já cadastrado")
    void shouldThrowsEmailErrorExceptionWhenEmailExist() {
        var userServiceRegistryApplication = new UserServiceTest();

        var email = Faker.instance().internet().emailAddress();
        when(validatorEmail.isExistEmail(email)).thenReturn(TRUE);

        var exception = assertThrows(EmailErrorException.class,
            () -> userServiceRegistryApplication.checkExistEmail(email));

        assertEquals(MessagesMapper.EMAIL_EXIST.getCode(), exception.getCode());
        assertEquals(email, exception.getEmail());
        verify(validatorEmail).isExistEmail(email);
    }

    @Test
    @DisplayName("Deve lançar EmailErrorException quando e-mail não é institucional")
    void shouldThrowsEmailErrorExceptionWhenEmailNotInstitutional() {
        var email = Faker.instance().internet().emailAddress();
        var userServiceRegistryApplication = new UserServiceTest();

        when(validatorEmail.validateEmailStudents(email)).thenReturn(FALSE);

        var exception = assertThrows(EmailErrorException.class,
            () -> userServiceRegistryApplication.checkEmailIsInstitutional(email));

        assertEquals(MessagesMapper.EMAIL_NOT_INSTITUTIONAL.getCode(), exception.getCode());
        assertEquals(email, exception.getEmail());

        verify(validatorEmail).validateEmailStudents(email);
    }

    @Test
    @DisplayName("Deve lançar EmailErrorException quando e-mail não está no padrão de e-mail")
    void shouldThrowsEmailErrorExceptionWhenEmailInvalid() {
        var value = "email";
        var userServiceRegistryApplication = new UserServiceTest();

        when(validatorEmail.isEmail(value)).thenReturn(FALSE);

        var exception = assertThrows(EmailErrorException.class,
            () -> userServiceRegistryApplication.checkIsEmail(value));

        assertEquals(MessagesMapper.EMAIL_INVALID.getCode(), exception.getCode());
        assertEquals(value, exception.getEmail());

        verify(validatorEmail).isEmail(value);
    }

    @Test
    @DisplayName("Deve lançar RegistryUserException quando nome já cadastrado")
    void shouldThrowsRegistryUserExceptionWhenNameExist() {
        var name = Faker.instance().name().fullName();
        var userServiceRegistryApplication = new UserServiceTest();

        when(userRepository.existsByName(name)).thenReturn(TRUE);

        var exception = assertThrows(RegistryUserException.class,
            () -> userServiceRegistryApplication.checkNameExist(name));

        assertEquals(MessagesMapper.NAME_EXIST.getCode(), exception.getCode());
        verify(userRepository).existsByName(name);
    }

    class UserServiceTest implements UserServiceApplication {

        @Override
        public ValidatorEmail getValidator() {
            return validatorEmail ;
        }

        @Override
        public UserRepository getUserRepository() {
            return userRepository;
        }
    }
}
