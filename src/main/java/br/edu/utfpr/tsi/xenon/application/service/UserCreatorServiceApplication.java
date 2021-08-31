package br.edu.utfpr.tsi.xenon.application.service;

import br.edu.utfpr.tsi.xenon.application.dto.InputUserDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputUserDto.TypeUserEnum;
import br.edu.utfpr.tsi.xenon.application.dto.UserDto;
import br.edu.utfpr.tsi.xenon.domain.notification.model.MessageWelcomeTemplate;
import br.edu.utfpr.tsi.xenon.domain.notification.service.SenderAdapter;
import br.edu.utfpr.tsi.xenon.domain.security.service.CreatorPasswordService;
import br.edu.utfpr.tsi.xenon.domain.user.factory.UserFactory;
import br.edu.utfpr.tsi.xenon.domain.user.service.UserCreatorService;
import br.edu.utfpr.tsi.xenon.domain.user.service.ValidatorEmail;
import br.edu.utfpr.tsi.xenon.structure.repository.UserRepository;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCreatorServiceApplication implements UserServiceApplication {

    private final UserCreatorService userCreatorService;
    private final ValidatorEmail validatorEmail;
    private final UserRepository userRepository;

    private final BCryptPasswordEncoder cryptPasswordEncoder;
    private final SenderAdapter senderAdapter;

    @Transactional
    public UserDto createNewUser(InputUserDto input) {
        log.info("Iniciando processo de registro de novo usuário");
        log.info("Iniciando processo de registro de novo usuário, {}", input);

        checkNameExist(input.getName());
        checkIsEmail(input.getEmail());
        checkExistEmail(input.getEmail());

        if (TypeUserEnum.STUDENTS == input.getTypeUser()) {
            checkEmailIsInstitutional(input.getEmail());
        }

        var pass = CreatorPasswordService.newInstance(cryptPasswordEncoder).createPass();
        var newUser = userCreatorService.createNewUser(input, pass.pass());

        sendWelcome(input.getEmail(), newUser.getName(), pass.pass());

        newUser = userRepository.saveAndFlush(newUser);
        return UserFactory.getInstance().buildUserDto(newUser);

    }

    @Override
    public ValidatorEmail getValidator() {
        return validatorEmail;
    }

    @Override
    public UserRepository getUserRepository() {
        return userRepository;
    }

    @Async
    public void sendWelcome(String email, String name, String pass) {
        CompletableFuture.runAsync(() -> {
            var template = new MessageWelcomeTemplate(email, name, pass);
            senderAdapter.sendEmail(template);
        });
    }

}
