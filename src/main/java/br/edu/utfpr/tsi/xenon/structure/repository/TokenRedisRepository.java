package br.edu.utfpr.tsi.xenon.structure.repository;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public void saveToken(String key, String token, Long timeExpired, TimeUnit timeUnit) {
        log.info("salvando token com chave: {}", key);
        redisTemplate.opsForValue().set(key, token, timeExpired, timeUnit);
    }

    public String findTokenByKey(String key) {
        log.info("recuperando token com chave: {}", key);
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        log.info("deletando token com chave: {}", key);
        redisTemplate.delete(key);
    }
}
