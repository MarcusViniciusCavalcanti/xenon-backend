package br.edu.utfpr.tsi.xenon.application.service;

import br.edu.utfpr.tsi.xenon.application.dto.UserDto;
import br.edu.utfpr.tsi.xenon.domain.security.service.SecurityContextUserService;
import br.edu.utfpr.tsi.xenon.domain.user.factory.UserFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserGetterServiceApplication {

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

}
