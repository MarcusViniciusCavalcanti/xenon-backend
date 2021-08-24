package br.edu.utfpr.tsi.xenon.domain.security.service;

import br.edu.utfpr.tsi.xenon.structure.repository.AccessCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AccessCardRepository accessCardRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        log.info("Buscando usuário de e-mail: '{}'", username);

        return accessCardRepository.findByUsername(username)
            .orElseThrow(() -> {
                log.error("Usuário com e-mail {} não encontrado", username);
                return new UsernameNotFoundException(
                    """
                        O e-mail: %s informado não corresponde a nenhum usuário"""
                        .formatted(username));
            });
    }
}
