package br.edu.utfpr.tsi.xenon.domain.user.aggregator;

import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.PASSWORD_INVALID;
import static java.lang.Boolean.FALSE;

import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.structure.exception.RegistryUserException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessCardAggregator {

    private final BCryptPasswordEncoder cryptPasswordEncoder;

    public void includeAccessCard(
        UserEntity user,
        String email,
        String password,
        String confirmPass) {
        var accessCard = new AccessCardEntity();
        accessCard.setUser(user);

        accessCard.setUsername(email);
        accessCard.setPassword(checkPasswordAndEncoderPass(password, confirmPass));
        accessCard.setEnabled(Boolean.TRUE);

        user.setAccessCard(accessCard);
    }

    private String checkPasswordAndEncoderPass(String password, String confirmPassword) {
        if (FALSE.equals(password.equals(confirmPassword))) {
            throw new RegistryUserException(PASSWORD_INVALID.getCode());
        }

        return cryptPasswordEncoder.encode(password);
    }
}
