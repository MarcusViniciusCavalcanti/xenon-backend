package br.edu.utfpr.tsi.xenon.application.config.bean;

import static java.lang.Boolean.TRUE;

import br.edu.utfpr.tsi.xenon.application.config.property.RedisProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@RequiredArgsConstructor
public class RedisConfiguration {

    private final RedisProperty xenonProperty;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        var redisStandalone = new RedisStandaloneConfiguration();
        redisStandalone.setHostName(xenonProperty.getHost());
        redisStandalone.setPort(xenonProperty.getPort());

        var jedisConfig = new JedisPoolConfig();
        jedisConfig.setMaxTotal(10);
        jedisConfig.setMaxIdle(10);
        jedisConfig.setTestWhileIdle(true);
        jedisConfig.setMinEvictableIdleTimeMillis(60000);
        jedisConfig.setTimeBetweenEvictionRunsMillis(30000);
        jedisConfig.setNumTestsPerEvictionRun(-1);

        return new JedisConnectionFactory(redisStandalone);
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        var template = new RedisTemplate<String, String>();
        var stringRedisSerializer = new StringRedisSerializer();

        template.setConnectionFactory(jedisConnectionFactory());
        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(stringRedisSerializer);
        template.setEnableTransactionSupport(TRUE);
        template.afterPropertiesSet();

        return template;
    }
}
