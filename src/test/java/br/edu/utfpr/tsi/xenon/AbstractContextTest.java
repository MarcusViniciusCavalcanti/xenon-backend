package br.edu.utfpr.tsi.xenon;

import br.edu.utfpr.tsi.xenon.TestApplicationConfiguration.InitiliazerContext;
import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

@Slf4j
@ActiveProfiles("test")
@Testcontainers
@ContextConfiguration(initializers = InitiliazerContext.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractContextTest {

    protected static Faker faker;

    @LocalServerPort
    protected int port;

    @Autowired
    protected MessageSource messageSource;

    @BeforeAll
    static void setup() {
        faker = Faker.instance();
    }

}
