package br.edu.utfpr.tsi.xenon.domain.security.service;


import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.structure.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityContextUserService {

    private static final String TOKEN_IS_INVALID = "Token é invalido";

    private final AccessTokenService tokenCreator;
    private final UserDetailsService accessCardRepository;
    private final UserRepository userRepository;

    public void receiveTokenToSecurityHolder(String token) {
        if (tokenCreator.isValid(token)) {
            tokenCreator.getEmail(token)
                .map(accessCardRepository::loadUserByUsername)
                .map(userDetails -> new UsernamePasswordAuthenticationToken(userDetails, null,
                    userDetails.getAuthorities()))
                .ifPresentOrElse(
                    auth -> SecurityContextHolder.getContext().setAuthentication(auth),
                    () -> {
                        SecurityContextHolder.clearContext();
                        throw new BadCredentialsException(TOKEN_IS_INVALID);
                    }
                );
            return;
        }

        throw new AuthorizationServiceException("token está invalido");
    }

    public Optional<UserEntity> getUserByContextSecurity(String authorization) {
        log.debug("Recuperando access card do token: {}", authorization);
        var principal = (AccessCardEntity) SecurityContextHolder.getContext()
            .getAuthentication()
            .getPrincipal();

        return userRepository.findByAccessCard(principal);
    }
}
