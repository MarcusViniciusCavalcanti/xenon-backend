package br.edu.utfpr.tsi.xenon.structure.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@DisplayName("Test - Unidade - TokenRedisRepository")
@ExtendWith(MockitoExtension.class)
class TokenRedisRepositoryTest {
    @Mock
    private ValueOperations<String, String> valueOperation;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @InjectMocks
    private TokenRedisRepository tokenRedisRepository;

    @Test
    @DisplayName("Deve salvar token")
    void shouldHaveSaveToken() {
        var token = "token";
        var key ="key";

        when(redisTemplate.opsForValue()).thenReturn(valueOperation);
        doNothing()
            .when(valueOperation)
            .set(key, token, 5L, TimeUnit.MINUTES);

        tokenRedisRepository.saveToken(key, token, 5L, TimeUnit.MINUTES);

        verify(redisTemplate).opsForValue();
        verify(valueOperation).set(key, token, 5L, TimeUnit.MINUTES);
    }

    @Test
    @DisplayName("Deve recuprear token")
    void shouldReturnToken() {
        var email = "email@email.com";
        var tokenValue = "token";

        when(redisTemplate.opsForValue()).thenReturn(valueOperation);
        when(valueOperation.get(any())).thenReturn(tokenValue);

        var token = tokenRedisRepository.findTokenByKey(email);

        assertEquals(tokenValue, token);
        verify(valueOperation).get(email);
    }

    @Test
    @DisplayName("Deve deletar token")
    void shouldHaveDeleteToken() {
        var email = "email@email.com";

        when(redisTemplate.delete(email)).thenReturn(Boolean.TRUE);

        tokenRedisRepository.delete(email);

        verify(redisTemplate).delete(email);
    }
}
