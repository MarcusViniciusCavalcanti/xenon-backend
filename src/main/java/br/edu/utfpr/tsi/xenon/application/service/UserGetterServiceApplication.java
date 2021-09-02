package br.edu.utfpr.tsi.xenon.application.service;

import br.edu.utfpr.tsi.xenon.application.config.bean.SpecificationConfiguration;
import br.edu.utfpr.tsi.xenon.application.dto.PageUserDto;
import br.edu.utfpr.tsi.xenon.application.dto.UserDto;
import br.edu.utfpr.tsi.xenon.domain.security.service.SecurityContextUserService;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.factory.UserFactory;
import br.edu.utfpr.tsi.xenon.structure.ParamsQuerySearchUserDto;
import br.edu.utfpr.tsi.xenon.structure.exception.ResourceNotFoundException;
import br.edu.utfpr.tsi.xenon.structure.repository.BasicSpecification;
import br.edu.utfpr.tsi.xenon.structure.repository.UserRepository;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserGetterServiceApplication {

    private final UserRepository userRepository;
    private final SecurityContextUserService securityContextUserService;
    private final BasicSpecification<UserEntity, ParamsQuerySearchUserDto> getterAllUserSpec;

    public UserGetterServiceApplication(
        UserRepository userRepository,
        SecurityContextUserService securityContextUserService,
        @Qualifier(SpecificationConfiguration.QUALIFIER_GET_ALL_SPEC)
            BasicSpecification<UserEntity, ParamsQuerySearchUserDto> getterAllUserSpec) {
        this.userRepository = userRepository;
        this.securityContextUserService = securityContextUserService;
        this.getterAllUserSpec = getterAllUserSpec;
    }

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
    @Cacheable(cacheNames = "User", key = "#id")
    public UserDto getUserById(Long id) {
        return userRepository.findById(id)
            .map(userEntity -> UserFactory.getInstance().buildUserDto(userEntity))
            .orElseThrow(() -> new ResourceNotFoundException("usuário", "id"));
    }

    @Cacheable(cacheNames = "UserPage", key = "#params.toString()")
    public PageUserDto getAllUser(ParamsQuerySearchUserDto params) {
        var sort = Sort.by(
            Sort.Direction.fromString(params.getDirection().name()), params.getSorted().getValue());
        var pageable = PageRequest.of(
            params.getPage().intValue(), params.getSize().intValue(), sort);
        var specification = getterAllUserSpec.filterBy(params);
        var page = userRepository.findAll(specification, pageable);

        var content = page.getContent().stream()
            .map(userEntity -> UserFactory.getInstance().buildUserDto(userEntity))
            .collect(Collectors.toList());

        var pageUser = new PageUserDto().items(content);
        pageUser.setDirection(params.getDirection().name());
        pageUser.setSorted(params.getSorted().name());
        pageUser.setTotalElements(page.getTotalElements());
        pageUser.setPage(page.getNumber());
        pageUser.setSize(page.getSize());
        pageUser.setTotalPage(page.getTotalPages());

        return pageUser;
    }
}
