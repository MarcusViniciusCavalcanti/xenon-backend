package br.edu.utfpr.tsi.xenon;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;

@Slf4j
@TestConfiguration
public class TestApplicationConfiguration {

    public static final PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:11.1")
            .withReuse(Boolean.TRUE)
            .withDatabaseName("xenon-test-database")
            .withPassword("xenon-password-test")
            .withUsername("xenon-username-test");
    private static final Integer PORT_SMTP = 1025;
    public static final GenericContainer<?> mailhog =
        new GenericContainer<>("mailhog/mailhog")
            .withReuse(Boolean.TRUE)
            .withExposedPorts(PORT_SMTP);
    private static final Integer PORT_REDIS = 6379;
    public static final GenericContainer<?> redis =
        new GenericContainer<>(DockerImageName.parse("library/redis:alpine"))
            .withReuse(Boolean.TRUE)
            .withExposedPorts(PORT_REDIS);

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    public static class InitiliazerContext
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            redis.start();
            postgres.start();
            mailhog.start();

            if (redis.isRunning()) {
                log.info("======================================================");
                log.info("        redis running in host: {}            ",
                    redis.getContainerIpAddress());
                log.info("======================================================");
            }

            if (postgres.isRunning()) {
                log.info("======================================================");
                log.info("        postgres running in hots: {}            ", postgres.getJdbcUrl());
                log.info("======================================================");
            }

            if (mailhog.isRunning()) {
                log.info("======================================================");
                log.info("        mailhog running in host: {}            ",
                    mailhog.getContainerIpAddress());
                log.info("======================================================");
            }

            var properties = TestPropertyValues.of(
                "xenon.configurations.redis.port=" + redis.getMappedPort(PORT_REDIS),
                "xenon.configurations.redis.host=", redis.getContainerIpAddress(),
                "spring.datasource.url=" + postgres.getJdbcUrl(),
                "spring.datasource.username=" + postgres.getUsername(),
                "spring.datasource.password=" + postgres.getPassword(),
                "xenon.configurations.email.host=" + mailhog.getContainerIpAddress(),
                "xenon.configurations.email.port=" + mailhog.getMappedPort(PORT_SMTP)
            );

            properties.applyTo(applicationContext);
        }
    }
}
