package br.edu.utfpr.tsi.xenon.application.service;

import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.EMAIL_EXIST;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.EMAIL_INVALID;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.EMAIL_NOT_INSTITUTIONAL;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import br.edu.utfpr.tsi.xenon.domain.user.service.ValidatorEmail;
import br.edu.utfpr.tsi.xenon.structure.MessagesMapper;
import br.edu.utfpr.tsi.xenon.structure.exception.EmailErrorException;
import br.edu.utfpr.tsi.xenon.structure.exception.RegistryUserException;
import br.edu.utfpr.tsi.xenon.structure.repository.UserRepository;

public interface UserServiceRegistryApplication {

    ValidatorEmail getValidator();
    UserRepository getUserRepository();

    default void checkIsEmail(String email) {
        if (FALSE.equals(getValidator().isEmail(email))) {
            throw new EmailErrorException(email, EMAIL_INVALID.getCode());
        }
    }

    default void checkEmailIsInstitutional(String email) {
        if (FALSE.equals(getValidator().validateEmailStudents(email))) {
            throw new EmailErrorException(email, EMAIL_NOT_INSTITUTIONAL.getCode());
        }
    }

    default void checkExistEmail(String email) {
        if (TRUE.equals(getValidator().isExistEmail(email))) {
            throw new EmailErrorException(email, EMAIL_EXIST.getCode());
        }
    }

    default void checkNameExist(String name) {
        if (TRUE.equals(getUserRepository().existsByName(name))) {
            throw new RegistryUserException(MessagesMapper.NAME_EXIST.getCode(), 409);
        }
    }
}
