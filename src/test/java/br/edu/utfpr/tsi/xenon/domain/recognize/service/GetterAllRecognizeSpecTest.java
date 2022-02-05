package br.edu.utfpr.tsi.xenon.domain.recognize.service;

import static org.assertj.core.api.Assertions.assertThat;

import br.edu.utfpr.tsi.xenon.TestApplicationConfiguration.InitiliazerContext;
import br.edu.utfpr.tsi.xenon.domain.recognize.entity.RecognizeEntity;
import br.edu.utfpr.tsi.xenon.structure.ParamsQuerySearchRecognizeDto;
import br.edu.utfpr.tsi.xenon.structure.repository.RecognizerRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.ResourceLocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers
@ContextConfiguration(initializers = InitiliazerContext.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GetterAllRecognizeSpecTest {

    @Autowired
    private RecognizerRepository recognizerRepository;

    @Test
    @DisplayName("Deve retornar reconhecimento com base no nome do motorista")
    @ResourceLock(value = "br.edu.utfpr.tsi.xenon.structure.repository.RecognizerRepository")
    void shouldReturnRecognizerBySpecificationByDriveName() {
        var driveName = "Driver Name";

        var recognizerExpected = buildRecognizer(driveName, Boolean.FALSE);
        configureDatabase(recognizerExpected);

        var params = ParamsQuerySearchRecognizeDto.builder()
            .driverName(driveName)
            .build();
        var filter = new GetterAllRecognizeSpec().filterBy(params);

        var result = recognizerRepository.findAll(filter);

        checkAssertionAndCleanDatabase(recognizerExpected, result);
    }

    @Test
    @DisplayName("Deve retornar reconhecimento com base no campo only error")
    @ResourceLock(value = "br.edu.utfpr.tsi.xenon.structure.repository")
    void shouldReturnRecognizerBySpecificationByOnlyError() {
        var driveName = "Name";

        var recognizerExpected = buildRecognizer(driveName, Boolean.TRUE);
        configureDatabase(recognizerExpected);

        var params = ParamsQuerySearchRecognizeDto.builder()
            .onlyError(recognizerExpected.getHasError())
            .build();
        var filter = new GetterAllRecognizeSpec().filterBy(params);

        var result = recognizerRepository.findAll(filter);

        checkAssertionAndCleanDatabase(recognizerExpected, result);
    }

    @Test
    @DisplayName("Deve retornar reconhecimento com base no nome do motorista e do campo only error")
    @ResourceLock(value = "br.edu.utfpr.tsi.xenon.structure.repository.RecognizerRepository")
    void shouldReturnRecognizerBySpecificationByOnlyErrorAndDriveName() {
        var driveName = "Driver Name";

        var recognizerExpected = buildRecognizer(driveName, Boolean.FALSE);
        configureDatabase(recognizerExpected);

        var params = ParamsQuerySearchRecognizeDto.builder()
            .driverName(driveName)
            .onlyError(recognizerExpected.getHasError())
            .build();
        var filter = new GetterAllRecognizeSpec().filterBy(params);

        var result = recognizerRepository.findAll(filter);

        checkAssertionAndCleanDatabase(recognizerExpected, result);
    }

    private RecognizeEntity buildRecognizer(String driveName, Boolean onlyError) {
        var recognizerExpected = new RecognizeEntity();
        recognizerExpected.setConfidence(99.9F);
        recognizerExpected.setEpochTime(LocalDateTime.now());
        recognizerExpected.setPlate("plate");
        recognizerExpected.setOriginIp("ip");
        recognizerExpected.setDriverName(driveName);
        recognizerExpected.setHasError(onlyError);

        return recognizerExpected;
    }

    private void configureDatabase(RecognizeEntity recognizerExpected) {
        var allRecognizer = IntStream.range(0, 10)
            .mapToObj(index -> {
                var recognizeEntity = new RecognizeEntity();
                recognizeEntity.setConfidence(99.9F);
                recognizeEntity.setEpochTime(LocalDateTime.now());
                recognizeEntity.setPlate("plate");
                recognizeEntity.setOriginIp("ip");
                recognizeEntity.setDriverName("name %d".formatted(index));
                recognizeEntity.setHasError(Boolean.FALSE);

                return recognizeEntity;
            }).collect(Collectors.toList());

        allRecognizer.add(recognizerExpected);

        recognizerRepository.saveAllAndFlush(allRecognizer);
    }

    private void checkAssertionAndCleanDatabase(RecognizeEntity recognizerExpected,
        List<RecognizeEntity> result) {
        var isEquals = new Condition<RecognizeEntity>(
            rec -> rec.equals(recognizerExpected),
            "Is equal entity expected"
        );

        assertThat(result)
            .hasSize(1)
            .haveExactly(1, isEquals);

        recognizerRepository.deleteAll();
    }
}
