package br.edu.utfpr.tsi.xenon.application.service;

import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.FILE_ALLOWED;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.NAME_CHANGED_SUCCESSFULLY;

import br.edu.utfpr.tsi.xenon.application.dto.InputNameUserDto;
import br.edu.utfpr.tsi.xenon.application.dto.ProcessResultDto;
import br.edu.utfpr.tsi.xenon.application.dto.UserDto;
import br.edu.utfpr.tsi.xenon.domain.security.service.SecurityContextUserService;
import br.edu.utfpr.tsi.xenon.domain.user.aggregator.AvatarAggregator;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.factory.UserFactory;
import br.edu.utfpr.tsi.xenon.domain.user.service.ValidatorEmail;
import br.edu.utfpr.tsi.xenon.structure.exception.BusinessException;
import br.edu.utfpr.tsi.xenon.structure.repository.UserRepository;
import java.io.IOException;
import java.nio.file.Files;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserUpdaterServiceApplication implements UserServiceApplication {

    private final UserRepository userRepository;
    private final AvatarAggregator avatarAggregator;
    private final SecurityContextUserService securityContextUserService;
    private final ValidatorEmail validatorEmail;

    @Transactional
    public UserDto changeAvatar(MultipartFile file, String authorization) {
        log.info("Iniciando processo de alterar avatar");
        log.debug("Iniciando processo de alterar avatar, com token {}", authorization);

        return securityContextUserService.getUserByContextSecurity(authorization)
            .map(userEntity -> includeAvatar(file, userEntity))
            .map(userEntity -> UserFactory.getInstance().buildUserDto(userEntity))
            .orElse(new UserDto());
    }

    @Transactional
    public ProcessResultDto changeName(InputNameUserDto input, String authorization) {
        log.info("Iniciando processo de alterar nome");
        log.debug("Iniciando processo de alterar nome, com token {}", authorization);
        return securityContextUserService.getUserByContextSecurity(authorization)
            .map(userEntity -> {
                checkNameExist(input.getName());
                userEntity.setName(input.getName());

                userRepository.saveAndFlush(userEntity);

                return new ProcessResultDto().result(NAME_CHANGED_SUCCESSFULLY.getCode());
            }).orElse(new ProcessResultDto().result(NAME_CHANGED_SUCCESSFULLY.getCode()));
    }

    private UserEntity includeAvatar(MultipartFile file, UserEntity userEntity) {
        try {
            log.debug("Criando arquivo temporário");
            var avatar = Files.createTempFile("%s".formatted(userEntity.getId()), ".png");

            log.debug("Transferindo dados do arquivo recebido para o temporário");
            file.transferTo(avatar);

            log.debug("Incluindo avatar no usuário");
            avatarAggregator.includeAvatar(avatar.toFile(), userEntity);

            log.debug("Salvando usuário");
            return userRepository.saveAndFlush(userEntity);
        } catch (IOException e) {
            log.error("Erro ao concluir avatar causa {}", e.getMessage());
            throw new BusinessException(422, FILE_ALLOWED.getCode(), "jpeg, jpg, png");
        }
    }

    @Override
    public ValidatorEmail getValidator() {
        return validatorEmail;
    }

    @Override
    public UserRepository getUserRepository() {
        return userRepository;
    }
}
