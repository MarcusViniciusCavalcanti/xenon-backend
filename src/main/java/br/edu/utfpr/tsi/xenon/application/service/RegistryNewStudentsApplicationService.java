package br.edu.utfpr.tsi.xenon.application.service;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.nio.charset.StandardCharsets.UTF_8;

import br.edu.utfpr.tsi.xenon.application.config.property.ApplicationDomainProperty;
import br.edu.utfpr.tsi.xenon.application.dto.InputRegistryStudentDto;
import br.edu.utfpr.tsi.xenon.application.dto.ProcessResultDto;
import br.edu.utfpr.tsi.xenon.application.dto.UserDto;
import br.edu.utfpr.tsi.xenon.domain.notification.model.MessageRegistryTemplate;
import br.edu.utfpr.tsi.xenon.domain.notification.service.SenderAdapter;
import br.edu.utfpr.tsi.xenon.domain.user.factory.UserFactory;
import br.edu.utfpr.tsi.xenon.domain.user.service.UserCreatorService;
import br.edu.utfpr.tsi.xenon.domain.user.service.ValidatorEmail;
import br.edu.utfpr.tsi.xenon.structure.MessagesMapper;
import br.edu.utfpr.tsi.xenon.structure.exception.ResourceNotFoundException;
import br.edu.utfpr.tsi.xenon.structure.repository.UserRepository;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistryNewStudentsApplicationService implements UserServiceApplication {

    private static final String PARAMETERS_URL = "%s/activate-registry?params=%s";

    private final UserRepository userRepository;
    private final ValidatorEmail validatorEmail;
    private final UserCreatorService userCreatorService;
    private final SenderAdapter senderAdapter;
    private final ApplicationDomainProperty applicationDomainProperty;

    @Transactional
    public UserDto registryNewStudents(InputRegistryStudentDto input) {
        log.info("Processo de registro de novo estudante: {}", input.getEmail());
        log.debug("registro de estudante com os campos: {}", input);

        checkNameExist(input.getName());
        checkIsEmail(input.getEmail());
        checkEmailIsInstitutional(input.getEmail());
        checkExistEmail(input.getEmail());

        var entity = userCreatorService.createNewStudents(input);
        entity.getAccessCard().setEnabled(FALSE);
        entity.setAuthorisedAccess(FALSE);
        sendWelcomeEmail(input.getEmail(), input.getName());

        userRepository.saveAndFlush(entity);
        return UserFactory.getInstance().buildUserDto(entity);
    }

    @Transactional
    public ProcessResultDto activateAccount(String params) {
        var email = new String(
            Base64.getDecoder().decode(params.getBytes(UTF_8)),
            UTF_8
        );

        var user = userRepository.findByAccessCardUsername(email)
            .orElseThrow(() -> new ResourceNotFoundException("usuário", email));

        user.setAuthorisedAccess(TRUE);
        user.getAccessCard().setEnabled(TRUE);

        userRepository.saveAndFlush(user);

        return new ProcessResultDto().result(MessagesMapper.ACTIVATE_ACCOUNT.getCode());
    }

    @Override
    public ValidatorEmail getValidator() {
        return validatorEmail;
    }

    @Override
    public UserRepository getUserRepository() {
        return userRepository;
    }

    private void sendWelcomeEmail(String email, String name) {
        CompletableFuture.runAsync(() -> {
            log.debug("Criando url de solicitação de nova senha para url.");
            var parameterUrl = new String(
                Base64.getEncoder().encode(email.getBytes(UTF_8)),
                UTF_8);
            var url = PARAMETERS_URL
                .formatted(applicationDomainProperty.getDomain(), parameterUrl);
            var template = new MessageRegistryTemplate(email, url, name);
            senderAdapter.sendEmail(template);
        });
    }
}
