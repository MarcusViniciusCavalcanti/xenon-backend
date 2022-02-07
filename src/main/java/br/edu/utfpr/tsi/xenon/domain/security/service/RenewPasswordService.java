package br.edu.utfpr.tsi.xenon.domain.security.service;

import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.CHANGE_PASS_SUCCESSFULLY;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.PASS_ACTUAL_NOT_MATCH;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.PASS_AND_CONFIRM_NOT_MATCH;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.nio.charset.StandardCharsets.UTF_8;

import br.edu.utfpr.tsi.xenon.application.config.property.ApplicationDomainProperty;
import br.edu.utfpr.tsi.xenon.application.dto.InputChangePasswordDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputRenewPasswordDto;
import br.edu.utfpr.tsi.xenon.application.dto.ProcessResultDto;
import br.edu.utfpr.tsi.xenon.domain.notification.model.MessageChangePasswordTemplate;
import br.edu.utfpr.tsi.xenon.domain.notification.model.MessageRenewPassTemplate;
import br.edu.utfpr.tsi.xenon.domain.notification.model.MessageRequestRenewPassTemplate;
import br.edu.utfpr.tsi.xenon.domain.notification.model.TokenApplication;
import br.edu.utfpr.tsi.xenon.domain.notification.service.SenderEmailService;
import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.structure.exception.BusinessException;
import br.edu.utfpr.tsi.xenon.structure.repository.AccessCardRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.TokenRedisRepository;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RenewPasswordService {

    private static final String TEMPLATE_KEY_REQUEST_RENEW_PASS = "%s-%d-%s-renew-password";
    private static final String TEMPLATE_PARAMETER_URL_REQUEST_RENEW_PASS = "%s:%s";
    private static final String TEMPLATE_URL_REQUEST_RENEW_PASS = "%s/request-renew-pass?params=%s";

    private final SenderEmailService senderEmailService;
    private final BCryptPasswordEncoder cryptPasswordEncoder;
    private final AccessCardRepository accessCardRepository;
    private final TokenRedisRepository tokenRedisRepository;
    private final ApplicationDomainProperty applicationDomainProperty;

    @Async
    public void checkSolicitation(InputRenewPasswordDto input) {
        log.info("Executando processo de solicitação de nova senha para: {}", input.getEmail());
        CompletableFuture.runAsync(() ->
                accessCardRepository.findByUsername(input.getEmail())
                    .ifPresent(accessCardEntity -> {
                        var token = createToken(input.getEmail());
                        var key = createKey(input.getEmail(), accessCardEntity.getId());
                        var url = createUrl(token, key);

                        saveToken(token, key);
                        log.debug("Enviando e-mail para notificação solicitação de senha.");
                        var template = new MessageRequestRenewPassTemplate(input.getEmail(), url);
                        senderEmailService.sendEmail(template);
                    }))
            .handleAsync((result, throwable) ->
                catchError(result, throwable, "Erro na solicitação de pedido de senha {}"));
    }

    @Async
    public void renewPassword(String params) {
        CompletableFuture.runAsync(() -> {
            log.info("Executando renovação de senha.");
            var decodeParams = decodeAndGetKeyToken(params);

            log.debug("Recuperando token.");
            var expectedToken = tokenRedisRepository.findTokenByKey(decodeParams.key);

            log.debug("Validando toke da chave: {}", decodeParams.key());
            if (TRUE.equals(TokenApplication.newInstance()
                .validateToken(decodeParams.token(), expectedToken))) {
                log.debug("recuperando access card.");
                var email = getEmail(decodeParams);
                accessCardRepository.findByUsername(email)
                    .ifPresentOrElse(
                        createPass(decodeParams, email),
                        () -> log.debug("access card não foi encontrado.")
                    );
            } else {
                log.info("Token não encontrado ou está invalido.");
            }
        }).handleAsync((result, throwable) ->
            catchError(result, throwable, "Erro na confirmação de pedido de senha {}"));
    }

    private Consumer<AccessCardEntity> createPass(KeyToken decodeParams,
        String email) {
        return accessCardEntity -> {
            log.info("Criando nova senha.");
            var crytorService = CreatorPasswordService
                .newInstance(cryptPasswordEncoder);
            var pass = crytorService.createPass();

            log.debug("Senha encryptado com {}", crytorService.cryptPasswordEncoder());
            log.info("enviando email.");
            var template = new MessageRenewPassTemplate(pass.pass(), email);
            senderEmailService.sendEmail(template);
            accessCardEntity.setPassword(pass.encoderPass());

            log.info("Salvando nova senha.");
            tokenRedisRepository.delete(decodeParams.key);
            accessCardRepository.saveAndFlush(accessCardEntity);
        };
    }

    public ProcessResultDto changePassword(AccessCardEntity accessCardEntity,
        InputChangePasswordDto input) {
        log.info("Executando processo para troca de senha");

        checkActualPassword(accessCardEntity, input.getActualPassword());
        checkPasswordAndConfirmPassword(input);

        log.debug("iniciando processo de criptografia de nova senha");
        var newPassword = cryptPasswordEncoder.encode(input.getPassword());
        accessCardEntity.setPassword(newPassword);

        log.debug("salvando nova senha no cartão de acesso");
        accessCardRepository.saveAndFlush(accessCardEntity);

        log.debug("Enviando e-mail para notificação solicitação de senha.");
        CompletableFuture.runAsync(() -> {
            var template = new MessageChangePasswordTemplate(accessCardEntity.getUsername());
            senderEmailService.sendEmail(template);
        });

        log.info("Processo concluído com sucesso.");
        return new ProcessResultDto().result(CHANGE_PASS_SUCCESSFULLY.getCode());
    }

    private String getEmail(KeyToken decodeParameter) {
        return decodeParameter.key.split("-")[0];
    }

    private KeyToken decodeAndGetKeyToken(String parameter) {
        log.debug("De codificando parâmetros.");
        var decodeParameter = new String(Base64.getDecoder().decode(
            parameter.getBytes(UTF_8)),
            UTF_8);
        var arrayParameter = decodeParameter.split(":");
        var key = arrayParameter[0];
        var token = arrayParameter[1];

        return new KeyToken(key, token);
    }

    private void saveToken(TokenApplication token, String key) {
        log.debug("Salvando token com tempo de 5 horas");
        tokenRedisRepository.saveToken(key, token.getToken(), 5L, TimeUnit.HOURS);
    }

    private String createUrl(TokenApplication token, String key) {
        log.debug("Criando url de solicitação de nova senha para url.");
        var partOneUrl = TEMPLATE_PARAMETER_URL_REQUEST_RENEW_PASS
            .formatted(key, token.getToken());
        var parameterUrl = new String(
            Base64.getEncoder().encode(partOneUrl.getBytes(UTF_8)),
            UTF_8);
        return TEMPLATE_URL_REQUEST_RENEW_PASS
            .formatted(applicationDomainProperty.getDomain(), parameterUrl);
    }

    private String createKey(String email, Long id) {
        log.debug("Criando key de solicitação de nova senha.");
        var randString = RandomStringUtils.random(10, TRUE, TRUE);
        return TEMPLATE_KEY_REQUEST_RENEW_PASS.formatted(email, id, randString);
    }

    private TokenApplication createToken(String email) {
        log.debug("Criando token de solicitação de nova senha.");
        var token = TokenApplication.newInstance(email);
        token.generateNewToken();
        return token;
    }

    private Void catchError(Void result, Throwable throwable, String s) {
        if (Objects.nonNull(throwable)) {
            log.error(s, throwable.getMessage());
        }
        return result;
    }

    private void checkPasswordAndConfirmPassword(InputChangePasswordDto input) {
        log.debug("Validando senha e confirmação de senha");
        if (FALSE.equals(input.getPassword().equals(input.getConfirmPassword()))) {
            throw new BusinessException(422, PASS_AND_CONFIRM_NOT_MATCH.getCode());
        }
    }

    private void checkActualPassword(AccessCardEntity accessCardEntity, String actual) {
        log.debug("Validando senha atual");
        if (FALSE.equals(cryptPasswordEncoder.matches(actual, accessCardEntity.getPassword()))) {
            throw new BusinessException(422, PASS_ACTUAL_NOT_MATCH.getCode());
        }
    }

    record KeyToken(String key, String token) {

    }
}
