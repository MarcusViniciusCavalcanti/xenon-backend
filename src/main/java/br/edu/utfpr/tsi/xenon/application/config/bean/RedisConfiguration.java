package br.edu.utfpr.tsi.xenon.application.config.bean;

import static java.lang.Boolean.TRUE;

import br.edu.utfpr.tsi.xenon.application.config.property.RedisProperty;
import br.edu.utfpr.tsi.xenon.application.dto.CarDto;
import br.edu.utfpr.tsi.xenon.application.dto.PageUserDto;
import br.edu.utfpr.tsi.xenon.application.dto.UserDto;
import br.edu.utfpr.tsi.xenon.application.dto.WorkstationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
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

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(
        ObjectMapper objectMapper) {
        return builder -> {
            var pageUser = new Jackson2JsonRedisSerializer<>(PageUserDto.class);
            pageUser.setObjectMapper(objectMapper);
            var workstationListType = objectMapper.getTypeFactory()
                .constructCollectionType(ArrayList.class, WorkstationDto.class);
            var workstation = new Jackson2JsonRedisSerializer<>(workstationListType);
            workstation.setObjectMapper(objectMapper);

            var user = new Jackson2JsonRedisSerializer<>(UserDto.class);
            user.setObjectMapper(objectMapper);

            var car = new Jackson2JsonRedisSerializer<>(CarDto.class);
            car.setObjectMapper(objectMapper);

            builder
                .withCacheConfiguration("User",
                    RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(10))
                        .serializeValuesWith(SerializationPair.fromSerializer(user)))
                .withCacheConfiguration("UserPage",
                    RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(1))
                        .serializeValuesWith(SerializationPair.fromSerializer(pageUser)))
                .withCacheConfiguration("Workstation",
                    RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(10))
                        .serializeValuesWith(SerializationPair.fromSerializer(workstation)))
                .withCacheConfiguration("Car",
                    RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(10))
                        .serializeValuesWith(SerializationPair.fromSerializer(car)));
        };
    }
}
