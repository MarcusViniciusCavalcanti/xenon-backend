package br.edu.utfpr.tsi.xenon.application.service;


import static java.lang.Boolean.FALSE;

import br.edu.utfpr.tsi.xenon.application.dto.InputChangePasswordDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputLoginDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputRenewPasswordDto;
import br.edu.utfpr.tsi.xenon.application.dto.ProcessResultDto;
import br.edu.utfpr.tsi.xenon.application.dto.TokenDto;
import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.domain.security.service.AccessTokenService;
import br.edu.utfpr.tsi.xenon.domain.security.service.RenewPasswordService;
import br.edu.utfpr.tsi.xenon.domain.security.service.SecurityContextUserService;
import br.edu.utfpr.tsi.xenon.structure.MessagesMapper;
import br.edu.utfpr.tsi.xenon.structure.exception.ResourceNotFoundException;
import br.edu.utfpr.tsi.xenon.structure.repository.UserRepository;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityApplicationService {

    private final AuthenticationManager authenticationManager;
    private final AccessTokenService accessTokenService;
    private final RenewPasswordService renewPasswordService;
    private final SecurityContextUserService securityContextUserService;
    private final UserRepository userRepository;

    public TokenDto processSignIn(InputLoginDto input) {
        log.info(
            "Criando o objeto de autenticação para o cartão de acesso: '{}'",
            input.getEmail());

        var authenticationToken = new UsernamePasswordAuthenticationToken(
            input.getEmail(),
            input.getPassword(),
            Collections.emptyList());

        var accessCard = (AccessCardEntity) authenticationManager.authenticate(authenticationToken)
            .getPrincipal();

        return accessTokenService.create(accessCard);
    }

    @Transactional
    public ProcessResultDto receiveRequestRenewPass(InputRenewPasswordDto input) {
        log.info("Iniciando processo de solicitação de nova senha para: {}", input.getEmail());
        renewPasswordService.checkSolicitation(input);
        return new ProcessResultDto().result(MessagesMapper.SEND_CONFIRM_NEW_PASSWORD.getCode());
    }

    @Transactional
    public ProcessResultDto confirmRequestRenewPass(String params) {
        log.info("Iniciado processo de confirmação de solicitação de nova senha.");
        renewPasswordService.renewPassword(params);
        return new ProcessResultDto().result(MessagesMapper.SEND_NEW_PASSWORD.getCode());
    }

    @Transactional
    public ProcessResultDto changePass(InputChangePasswordDto input, String authorization) {
        log.info("Iniciando processo para alterar senha");
        var accessCard = securityContextUserService.getUserByContextSecurity(authorization)
            .orElseThrow(() -> new ResourceNotFoundException("usuário", "token"));
        return renewPasswordService.changePassword(accessCard.getAccessCard(), input);
    }

    @Transactional
    public void disableAccount(String authorization) {
        securityContextUserService.getUserByContextSecurity(authorization)
            .ifPresent(userEntity -> {
                userEntity.setAuthorisedAccess(FALSE);
                userEntity.getAccessCard().setEnabled(FALSE);

                userRepository.saveAndFlush(userEntity);
            });
    }
}
