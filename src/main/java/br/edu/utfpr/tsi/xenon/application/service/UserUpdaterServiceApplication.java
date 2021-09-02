package br.edu.utfpr.tsi.xenon.application.service;

import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.ADD_AUTHORIZATION_ACCESS;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.FILE_ALLOWED;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.NAME_CHANGED_SUCCESSFULLY;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.REASON_IS_EMPTY;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.REMOVE_AUTHORIZATION_ACCESS;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import br.edu.utfpr.tsi.xenon.application.dto.InputAccessUserDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputNameUserDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputUpdateUserDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputUpdateUserDto.TypeUserEnum;
import br.edu.utfpr.tsi.xenon.application.dto.ProcessResultDto;
import br.edu.utfpr.tsi.xenon.application.dto.UserDto;
import br.edu.utfpr.tsi.xenon.domain.security.service.SecurityContextUserService;
import br.edu.utfpr.tsi.xenon.domain.user.aggregator.AvatarAggregator;
import br.edu.utfpr.tsi.xenon.domain.user.aggregator.RolesAggregator;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.factory.TypeUser;
import br.edu.utfpr.tsi.xenon.domain.user.factory.UserFactory;
import br.edu.utfpr.tsi.xenon.domain.user.service.ValidatorEmail;
import br.edu.utfpr.tsi.xenon.structure.exception.BusinessException;
import br.edu.utfpr.tsi.xenon.structure.exception.ResourceNotFoundException;
import br.edu.utfpr.tsi.xenon.structure.repository.UserRepository;
import java.io.IOException;
import java.nio.file.Files;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserUpdaterServiceApplication implements UserServiceApplication {

    private static final String RESOURCE_NAME = "usuário";
    private final UserRepository userRepository;
    private final RolesAggregator aggregator;
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
    @CacheEvict(cacheNames = {"UserPage", "User"}, allEntries = true)
    public ProcessResultDto changeName(InputNameUserDto input, String authorization) {
        log.info("Iniciando processo de alterar nome");
        log.debug("Iniciando processo de alterar nome, com token {}", authorization);
        return securityContextUserService.getUserByContextSecurity(authorization)
            .map(userEntity -> {
                updateName(input.getName(), userEntity);
                userRepository.saveAndFlush(userEntity);

                return new ProcessResultDto().result(NAME_CHANGED_SUCCESSFULLY.getCode());
            }).orElse(new ProcessResultDto().result(NAME_CHANGED_SUCCESSFULLY.getCode()));
    }

    @Transactional
    @CachePut(cacheNames = "User", key = "#id")
    public UserDto updateUser(InputUpdateUserDto input, Long id) {
        return userRepository.findById(id)
            .map(userEntity -> {
                updateAuthorizedAccess(
                    input.getDisableReason(),
                    input.getAuthorisedAccess(),
                    userEntity);
                updateEnabled(input.getDisableReason(), input.getEnabled(), userEntity);

                updateName(input.getName(), userEntity);
                updateEmail(input, userEntity);

                userEntity.setTypeUser(input.getTypeUser().name());
                userEntity.setName(input.getName());

                aggregator.includeRoles(
                    userEntity.getAccessCard(),
                    TypeUser.valueOf(input.getTypeUser().name()),
                    input.getRoles());

                var updateUser = userRepository.saveAndFlush(userEntity);
                return UserFactory.getInstance().buildUserDto(updateUser);
            })
            .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NAME, "id"));
    }

    @Transactional
    @CachePut(cacheNames = "User", key = "#input.userId")
    public ProcessResultDto unauthorizedAccess(InputAccessUserDto input) {
        var id = input.getUserId();
        log.info(
            "Iniciando processo para remover autorização de acesso usuário de id: {}, motivo: {}",
            id,
            input.getReason());

        return userRepository.findById(id)
            .map(userEntity -> {
                log.debug("Removendo autorização usuário.");
                userEntity.setAuthorisedAccess(FALSE);
                userEntity.setDisableReason(input.getReason());

                log.debug("Salvando usuário.");
                userRepository.saveAndFlush(userEntity);

                return new ProcessResultDto().result(REMOVE_AUTHORIZATION_ACCESS.getCode());
            })
            .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NAME, "id"));
    }

    @Transactional
    @CachePut(cacheNames = "User", key = "#input.userId")
    public ProcessResultDto authorizedAccess(InputAccessUserDto input) {
        log.info("Iniciando processo para autorizar acesso de usuário");
        return userRepository.findById(input.getUserId())
            .map(userEntity -> {
                log.debug("Autorizando usuário.");
                userEntity.setAuthorisedAccess(TRUE);
                userEntity.setDisableReason(null);

                log.debug("Salvando usuário.");
                userRepository.saveAndFlush(userEntity);

                return new ProcessResultDto().result(ADD_AUTHORIZATION_ACCESS.getCode());
            })
            .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NAME, "id"));
    }

    @Override
    public ValidatorEmail getValidator() {
        return validatorEmail;
    }

    @Override
    public UserRepository getUserRepository() {
        return userRepository;
    }

    private void updateName(String name, UserEntity userEntity) {
        if (FALSE.equals(userEntity.getName().equals(name))) {
            checkNameExist(name);
            userEntity.setName(name);
        }
    }

    private void updateEmail(InputUpdateUserDto input, UserEntity userEntity) {
        if (TRUE.equals(userEntity.getAccessCard().getUsername().equals(input.getEmail()))) {
            return;
        }

        checkIsEmail(input.getEmail());
        checkExistEmail(input.getEmail());

        if (input.getTypeUser() == TypeUserEnum.STUDENTS) {
            checkEmailIsInstitutional(input.getEmail());
        }

        userEntity.getAccessCard().setUsername(input.getEmail());
    }

    private void updateEnabled(String reason, Boolean value, UserEntity userEntity) {
        if (TRUE.equals(value)) {
            return;
        }

        if (StringUtils.isBlank(reason)) {
            throw new BusinessException(400, REASON_IS_EMPTY.getCode());
        }

        userEntity.setDisableReason(reason);
        userEntity.getAccessCard().setEnabled(value);
    }

    private void updateAuthorizedAccess(String reason, Boolean value, UserEntity userEntity) {
        if (TRUE.equals(value)) {
            return;
        }

        if (StringUtils.isBlank(reason)) {
            throw new BusinessException(400, REASON_IS_EMPTY.getCode());
        }

        userEntity.setDisableReason(reason);
        userEntity.setAuthorisedAccess(value);
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

}
