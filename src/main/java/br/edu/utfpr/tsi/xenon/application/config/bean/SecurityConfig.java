package br.edu.utfpr.tsi.xenon.application.config.bean;


import br.edu.utfpr.tsi.xenon.application.config.property.SecurityProperty;
import br.edu.utfpr.tsi.xenon.application.handler.AccessDeniedFailureHandler;
import br.edu.utfpr.tsi.xenon.application.handler.AuthenticationFailureHandlerImpl;
import br.edu.utfpr.tsi.xenon.domain.security.filter.JwtAuthorizationFilter;
import br.edu.utfpr.tsi.xenon.domain.security.service.SecurityContextUserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String[] POST_URL_PUBLIC = new String[] {
        "/new-students/**",
        "/login/**",
        "/request-renew-pass/**",
        "/new-students/registry/**",
    };
    private static final String[] GET_URL_PUBLIC = new String[] {
        "/request-renew-pass/**",
        "/activate-registry/**",
        "/swagger-ui/**",
        "/v3/api-docs/swagger-config/**",
        "/api-doc.yml",
    };

    private final UserDetailsService userDetailsService;
    private final MessageSource messageSource;
    private final SecurityContextUserService securityContextUserService;
    private final SecurityProperty securityProperty;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        configureCorsAndCsrf(http);
        configureSessionManagements(http);
        configureHandlers(http);
        configureRequestAuthorizations(http);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        var source = new UrlBasedCorsConfigurationSource();
        var config = new CorsConfiguration().applyPermitDefaultValues();
        config.setAllowedMethods(
            List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "TRACE", "CONNECT"));

        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private void configureRequestAuthorizations(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .antMatchers(HttpMethod.POST, POST_URL_PUBLIC).permitAll()
            .antMatchers(HttpMethod.GET, GET_URL_PUBLIC).permitAll()
            .anyRequest().authenticated();
    }

    private void configureHandlers(HttpSecurity http) throws Exception {
        http.exceptionHandling()
            .accessDeniedHandler(new AccessDeniedFailureHandler(messageSource))
            .authenticationEntryPoint(new AuthenticationFailureHandlerImpl(messageSource))
            .and()
            .addFilterBefore(
                new JwtAuthorizationFilter(
                    authenticationManager(),
                    securityProperty,
                    securityContextUserService), BasicAuthenticationFilter.class);
    }

    private void configureSessionManagements(HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    private void configureCorsAndCsrf(HttpSecurity http) throws Exception {
        http.csrf().disable().cors();
    }
}
