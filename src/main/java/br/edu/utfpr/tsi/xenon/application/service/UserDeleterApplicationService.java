package br.edu.utfpr.tsi.xenon.application.service;

import static java.lang.Boolean.FALSE;

import br.edu.utfpr.tsi.xenon.application.dto.InputAccessUserDto;
import br.edu.utfpr.tsi.xenon.application.dto.ProcessResultDto;
import br.edu.utfpr.tsi.xenon.domain.security.service.SecurityContextUserService;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.structure.MessagesMapper;
import br.edu.utfpr.tsi.xenon.structure.exception.ResourceNotFoundException;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.UserRepository;
import java.util.LinkedList;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDeleterApplicationService {

    private final SecurityContextUserService securityContextUserService;
    private final UserRepository userRepository;
    private final CarRepository carRepository;

    @Transactional
    @CacheEvict(cacheNames = {"User", "UserPage"}, allEntries = true)
    public void disableAccount(String authorization, String reason) {
        securityContextUserService.getUserByContextSecurity(authorization)
            .ifPresent(disableAccount(reason));
    }

    @Transactional
    @CacheEvict(cacheNames = {"User", "UserPage"}, allEntries = true)
    public ProcessResultDto disableAccount(InputAccessUserDto input) {
        var id = input.getUserId();
        userRepository.findById(id)
            .ifPresentOrElse(
                disableAccount(input.getReason()),
                () -> {
                    throw new ResourceNotFoundException("usuário", "id");
                });

        return new ProcessResultDto().result(MessagesMapper.USER_ACCOUNT_DEACTIVATED.getCode());
    }

    private Consumer<UserEntity> disableAccount(String reason) {
        return userEntity -> {
            userEntity.setAuthorisedAccess(FALSE);
            userEntity.getAccessCard().setEnabled(FALSE);
            userEntity.setDisableReason(reason);
            userEntity.setCar(new LinkedList<>());

            carRepository.deleteByUser(userEntity);
            userRepository.saveAndFlush(userEntity);
        };
    }
}
