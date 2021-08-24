package br.edu.utfpr.tsi.xenon.domain.user.service;

import static java.util.regex.Pattern.compile;

import br.edu.utfpr.tsi.xenon.structure.repository.AccessCardRepository;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValidatorEmail {

    private static final Predicate<String> IS_INSTITUTIONAL =
        compile("^[A-Za-z0-9._%+-]+@alunos.utfpr.edu.br$").asPredicate();

    private static final Predicate<String> IS_EMAIL =
        compile(
            "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$")
            .asPredicate();

    private final AccessCardRepository accessCardRepository;

    public Boolean validateEmailStudents(String email) {
        var isEmail = isEmail(email);
        var isInstitutional = IS_INSTITUTIONAL.test(email);

        return isEmail && isInstitutional;
    }

    public Boolean isExistEmail(String email) {
        return accessCardRepository.existsByUsername(email);
    }

    public Boolean isEmail(String email) {
        return IS_EMAIL.test(email);
    }
}
