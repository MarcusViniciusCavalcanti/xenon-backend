package br.edu.utfpr.tsi.xenon.application.config.bean;

import static java.lang.Boolean.TRUE;

import br.edu.utfpr.tsi.xenon.application.config.property.RedisProperty;
import br.edu.utfpr.tsi.xenon.application.dto.CarDto;
import br.edu.utfpr.tsi.xenon.application.dto.PageUserDto;
import br.edu.utfpr.tsi.xenon.application.dto.RecognizerSummaryDto;
import br.edu.utfpr.tsi.xenon.application.dto.UserCarsSummaryDto;
import br.edu.utfpr.tsi.xenon.application.dto.UserDto;
import br.edu.utfpr.tsi.xenon.application.dto.UsersRegistrySummaryDto;
import br.edu.utfpr.tsi.xenon.application.dto.WorkstationDto;
import br.edu.utfpr.tsi.xenon.application.dto.WorkstationSummaryDto;
import br.edu.utfpr.tsi.xenon.domain.workstations.entity.WorkstationEntity;
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
        return builder -> builder
            .withCacheConfiguration("User",
                getCacheConfigurationUser(objectMapper))
            .withCacheConfiguration("UserPage",
                getCacheConfigurationUserPage(objectMapper))
            .withCacheConfiguration("Workstations",
                getCacheConfigurationWorkstations(objectMapper))
            .withCacheConfiguration("Workstation",
                getCacheConfigurationWorkstation(objectMapper))
            .withCacheConfiguration("WorkstationDto",
                getCacheConfigurationWorkstationsDto(objectMapper))
            .withCacheConfiguration("Car",
                getCacheConfigurationCar(objectMapper))
            .withCacheConfiguration("WorkstationsSummary",
                getCacheConfigurationWorkstationsSummary(objectMapper))
            .withCacheConfiguration("UserCarSummary",
                getCacheConfigurationUserCarSummary(objectMapper))
            .withCacheConfiguration("RecognizerWeekSummary",
                getCacheConfigurationRecognizerWeekSummary(objectMapper))
            .withCacheConfiguration("UserTypeSummary",
                getCacheConfigurationUserTypeSummary(objectMapper));
    }

    private RedisCacheConfiguration getCacheConfigurationUserTypeSummary(
        ObjectMapper objectMapper
    ) {
        var userSummary = new Jackson2JsonRedisSerializer<>(UsersRegistrySummaryDto.class);
        userSummary.setObjectMapper(objectMapper);

        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5))
            .serializeValuesWith(SerializationPair.fromSerializer(userSummary));
    }

    private RedisCacheConfiguration getCacheConfigurationRecognizerWeekSummary(
        ObjectMapper objectMapper
    ) {
        var recognizeSummary = new Jackson2JsonRedisSerializer<>(RecognizerSummaryDto.class);
        recognizeSummary.setObjectMapper(objectMapper);

        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5))
            .serializeValuesWith(SerializationPair.fromSerializer(recognizeSummary));
    }

    private RedisCacheConfiguration getCacheConfigurationUserCarSummary(ObjectMapper objectMapper) {
        var carsSummary = new Jackson2JsonRedisSerializer<>(UserCarsSummaryDto.class);
        carsSummary.setObjectMapper(objectMapper);

        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5))
            .serializeValuesWith(SerializationPair.fromSerializer(carsSummary));
    }

    private RedisCacheConfiguration getCacheConfigurationWorkstationsSummary(
        ObjectMapper objectMapper
    ) {
        var workstationListSummary = objectMapper.getTypeFactory()
            .constructCollectionType(ArrayList.class, WorkstationSummaryDto.class);
        var workstationSummary = new Jackson2JsonRedisSerializer<>(workstationListSummary);
        workstationSummary.setObjectMapper(objectMapper);

        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5))
            .serializeValuesWith(SerializationPair.fromSerializer(workstationSummary));
    }

    private RedisCacheConfiguration getCacheConfigurationCar(ObjectMapper objectMapper) {
        var car = new Jackson2JsonRedisSerializer<>(CarDto.class);
        car.setObjectMapper(objectMapper);

        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeValuesWith(SerializationPair.fromSerializer(car));
    }

    private RedisCacheConfiguration getCacheConfigurationWorkstationsDto(
        ObjectMapper objectMapper
    ) {
        var workstationDto = new Jackson2JsonRedisSerializer<>(WorkstationDto.class);
        workstationDto.setObjectMapper(objectMapper);

        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .serializeValuesWith(SerializationPair.fromSerializer(workstationDto));
    }

    private RedisCacheConfiguration getCacheConfigurationWorkstation(ObjectMapper objectMapper) {
        var workstation = new Jackson2JsonRedisSerializer<>(WorkstationEntity.class);
        workstation.setObjectMapper(objectMapper);

        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .serializeValuesWith(SerializationPair.fromSerializer(workstation));
    }

    private RedisCacheConfiguration getCacheConfigurationWorkstations(ObjectMapper objectMapper) {
        var workstationListType = objectMapper.getTypeFactory()
            .constructCollectionType(ArrayList.class, WorkstationDto.class);

        var workstations = new Jackson2JsonRedisSerializer<>(workstationListType);
        workstations.setObjectMapper(objectMapper);

        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .serializeValuesWith(SerializationPair.fromSerializer(workstations));
    }

    private RedisCacheConfiguration getCacheConfigurationUserPage(ObjectMapper objectMapper) {
        var pageUser = new Jackson2JsonRedisSerializer<>(PageUserDto.class);
        pageUser.setObjectMapper(objectMapper);

        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(1))
            .serializeValuesWith(SerializationPair.fromSerializer(pageUser));
    }

    private RedisCacheConfiguration getCacheConfigurationUser(ObjectMapper objectMapper) {
        var user = new Jackson2JsonRedisSerializer<>(UserDto.class);
        user.setObjectMapper(objectMapper);

        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeValuesWith(SerializationPair.fromSerializer(user));
    }
}
