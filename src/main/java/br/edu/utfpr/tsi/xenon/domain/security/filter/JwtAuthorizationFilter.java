package br.edu.utfpr.tsi.xenon.domain.security.filter;

import br.edu.utfpr.tsi.xenon.application.config.property.SecurityProperty;
import br.edu.utfpr.tsi.xenon.domain.security.service.SecurityContextUserService;
import java.io.IOException;
import java.util.Objects;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Slf4j
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private final SecurityProperty securityProperty;

    private final SecurityContextUserService securityContextUserService;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager,
        SecurityProperty securityProperty,
        SecurityContextUserService securityContextUserService) {
        super(authenticationManager);
        this.securityProperty = securityProperty;
        this.securityContextUserService = securityContextUserService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse, FilterChain filterChain)
        throws IOException, ServletException {
        log.info(
            "Solicitação em execução do endereço de IP: '{}'",
            httpServletRequest.getRemoteAddr());
        var header = httpServletRequest.getHeader(securityProperty.getHeader().getName());

        if (Objects.isNull(header)) {
            log.info("Solicitação em execução não autenticada. . .");
        } else {
            log.info("Extraia para token do cabeçalho da solicitação. . .");
            var token = header
                .replace(securityProperty.getHeader().getPrefix(), "")
                .trim();

            log.info("Token descriptografado e sinal de validação. . .");

            securityContextUserService.receiveTokenToSecurityHolder(token);
            log.info("Token validado autenticado com sucesso!");
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

}
