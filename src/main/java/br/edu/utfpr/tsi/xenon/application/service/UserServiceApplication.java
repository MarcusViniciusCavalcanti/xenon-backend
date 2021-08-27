package br.edu.utfpr.tsi.xenon.application.service;

import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.NAME_CHANGED_SUCCESSFULLY;

import br.edu.utfpr.tsi.xenon.application.dto.InputNameUserDto;
import br.edu.utfpr.tsi.xenon.application.dto.ProcessResultDto;
import br.edu.utfpr.tsi.xenon.application.dto.UserDto;
import br.edu.utfpr.tsi.xenon.domain.security.service.SecurityContextUserService;
import br.edu.utfpr.tsi.xenon.domain.user.factory.UserFactory;
import br.edu.utfpr.tsi.xenon.domain.user.service.UserCreatorService;
import br.edu.utfpr.tsi.xenon.domain.user.service.ValidatorEmail;
import br.edu.utfpr.tsi.xenon.structure.repository.AccessCardRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceApplication implements UserServiceRegistryApplication {

    private final UserCreatorService userCreatorService;
    private final AccessCardRepository accessCardService;
    private final ValidatorEmail validatorEmail;
    private final UserRepository userRepository;
    private final SecurityContextUserService securityContextUserService;

    @Transactional
    public UserDto getUserByToken(String authorization) {
        log.info("Executando recuperação de usuário por token");
        log.debug("Recuperando usuário dono do token {}", authorization);
        try {
            return securityContextUserService.getUserByContextSecurity(authorization)
                .map(userEntity -> UserFactory.getInstance().buildUserDto(userEntity))
                .orElse(new UserDto());
        } catch (Exception exception) {
            log.debug(exception.getMessage());
            log.warn(
                """ 
                            
                    +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                    | Contexto de segurança vazio usando o token:                                 |
                    |==============================================================================
                                    
                      {}
                                    
                    |==============================================================================
                    | Requisição de pedido de token sem está autenticado.                         |
                    +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                            
                    """, authorization);
            return new UserDto();
        }
    }

    @Transactional
    public ProcessResultDto changeName(InputNameUserDto input, String authorization) {
        return securityContextUserService.getUserByContextSecurity(authorization)
            .map(userEntity -> {
                checkNameExist(input.getName());
                userEntity.setName(input.getName());

                userRepository.saveAndFlush(userEntity);

                return new ProcessResultDto().result(NAME_CHANGED_SUCCESSFULLY.getCode());
            }).orElse(new ProcessResultDto().result(NAME_CHANGED_SUCCESSFULLY.getCode()));
    }

//    @Transactional
//    public UserDto createNewUser(InputUserDto input) {
//        log.info("Iniciando processo de registro de novo usuário");
//        log.info("Iniciando processo de registro de novo usuário, {}", input);
//
//        checkNameExist(input.getName());
//        checkIsEmail(input.getEmail());
//        checkExistEmail(input.getEmail());
//
//        if (TypeUserEnum.STUDENTS == input.getTypeUser()) {
//            checkEmailIsInstitutional(input.getEmail());
//        }
//
//        var pass = CreatorPasswordService.newInstance(bCryptPasswordEncoder).createPass();
//        var newUser = userCreatorService.createNewUser(input, pass.pass());
//
//        notificationNewPassword.sendNewPass(pass.pass(), input.getEmail());
//        return newUser;

//    }
//    @Transactional
//    public UserDto updateUser(Long id, InputUserDto input) {
//        var user = userRepository.findById(id)
//            .orElseThrow(
//                () -> new ResourceNotFoundException("usuário", "userId: {%d}".formatted(id)));
//
//        checkNameExist(input.getName());
//        checkIsEmail(input.getEmail());
//        checkExistEmail(input.getEmail());
//
//        if (TypeUserEnum.STUDENTS == input.getTypeUser()) {
//            checkEmailIsInstitutional(input.getEmail());
//        }
//
//        user.setTypeUser(input.getTypeUser().name());
//        user.setName(input.getName());
//        user.getAccessCard().setUsername(input.getEmail());
//
//        userRepository.saveAndFlush(user);
//        return UserFactory.getInstance().buildUserDto(user);
    //    }

    @Override
    public ValidatorEmail getValidator() {
        return validatorEmail;
    }

    @Override
    public UserRepository getUserRepository() {
        return userRepository;
    }
}
