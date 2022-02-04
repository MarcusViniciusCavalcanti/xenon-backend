package br.edu.utfpr.tsi.xenon.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.utfpr.tsi.xenon.application.dto.InputRecognizerDto;
import br.edu.utfpr.tsi.xenon.application.dto.PlatesDto;
import br.edu.utfpr.tsi.xenon.domain.recognize.entity.ErrorRecognizerEntity;
import br.edu.utfpr.tsi.xenon.domain.recognize.entity.RecognizeEntity;
import br.edu.utfpr.tsi.xenon.domain.recognize.entity.RecognizerSummaryWeek;
import br.edu.utfpr.tsi.xenon.domain.recognize.service.ExecutorRecognizerService;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.structure.DirectionEnum;
import br.edu.utfpr.tsi.xenon.structure.ParamsQuerySearchRecognizeDto;
import br.edu.utfpr.tsi.xenon.structure.ParamsQuerySearchRecognizeDto.SortedRecognizePropertyEnum;
import br.edu.utfpr.tsi.xenon.structure.exception.ResourceNotFoundException;
import br.edu.utfpr.tsi.xenon.structure.repository.BasicSpecification;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.ErrorRecognizerRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.RecognizerRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - RecognizeServiceApplication")
class RecognizeServiceApplicationTest {

    private static final String CREATED_AT = "createdAt";

    @Mock
    private ExecutorRecognizerService executorRecognizerService;

    @Mock
    private BasicSpecification<RecognizeEntity, ParamsQuerySearchRecognizeDto> specification;

    @Mock
    private RecognizerRepository recognizerRepository;

    @Mock
    private ErrorRecognizerRepository errorRecognizerRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private Specification<RecognizeEntity> recognizeEntitySpecification;

    @InjectMocks
    private RecognizeServiceApplication recognizeServiceApplication;

    @Test
    @DisplayName("Deve receber uma reconhecimento com sucesso")
    void shouldExecuteReceiveRecognizer() {
        var input = new InputRecognizerDto()
            .addRecognizersItem(new PlatesDto());
        var key = "key";
        var ip = "ip";

        recognizeServiceApplication.receive(input, key, ip);

        verify(executorRecognizerService, timeout(1000)).accept(input, key, ip);
    }

    @Test
    @DisplayName("Deve retonar uma página com uma lista de reconhecimentos")
    void shouldReturnPageRecognizer(@Mock Page<RecognizeEntity> entityPage) {
        var params = ParamsQuerySearchRecognizeDto.builder()
            .direction(DirectionEnum.DESC)
            .sorted(SortedRecognizePropertyEnum.CREATED)
            .size(1)
            .page(0)
            .build();
        var recognizerWithoutError = new RecognizeEntity();
        var recognizerWithError = new RecognizeEntity();
        recognizerWithError.setErrorRecognizer(new ErrorRecognizerEntity());

        when(specification.filterBy(params)).thenReturn(recognizeEntitySpecification);

        when(recognizerRepository.findAll(eq(recognizeEntitySpecification), any(Pageable.class)))
            .thenReturn(entityPage);
        when(entityPage.getContent()).thenReturn(
            List.of(recognizerWithError, recognizerWithoutError));
        when(entityPage.getTotalElements()).thenReturn(1L);
        when(entityPage.getNumber()).thenReturn(10);
        when(entityPage.getSize()).thenReturn(10);
        when(entityPage.getTotalPages()).thenReturn(1);

        recognizeServiceApplication.getAll(params);

        verify(entityPage).getContent();
        verify(entityPage).getTotalElements();
        verify(entityPage).getNumber();
        verify(entityPage, times(2)).getSize();
        verify(entityPage).getTotalPages();
        verify(recognizerRepository).findAll(eq(recognizeEntitySpecification), any(Pageable.class));
        verify(specification).filterBy(params);
    }

    @Test
    @DisplayName("Deve retornar um erro de reconhecimento pelo id do reconhecimento")
    void shouldReturnErrorRecognizerById() {
        var erroId = 1L;

        var error = new ErrorRecognizerEntity();
        error.setErrorMessage("message error");
        error.setDate(LocalDateTime.now());
        error.setTrace("trace");
        error.setOriginIp("ip");
        error.setWorkstationName("worstation");

        when(errorRecognizerRepository.findErrorRecognizerEntityByRecognizeId(erroId)).thenReturn(Optional.of(error));

        var errorDto = recognizeServiceApplication.getErroById(erroId);

        assertEquals(errorDto.getErrorMessage(), error.getErrorMessage());
        assertEquals(errorDto.getDate(), error.getDate());
        assertEquals(errorDto.getOriginIp(), error.getOriginIp());
        assertEquals(errorDto.getTrace(), error.getTrace());
        assertEquals(errorDto.getWorkstationName(), error.getWorkstationName());
        assertEquals(errorDto.getOriginIp(), error.getOriginIp());

        verify(errorRecognizerRepository).findErrorRecognizerEntityByRecognizeId(erroId);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando não encontrar error")
    void shoudlThrowsResourceNotFoundExceptionWhenNotFoundError() {
        when(errorRecognizerRepository.findErrorRecognizerEntityByRecognizeId(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> recognizeServiceApplication.getErroById(1L));

        verify(errorRecognizerRepository).findErrorRecognizerEntityByRecognizeId(1L);
    }

    @Test
    @DisplayName("Deve retornar uma página de accesso com base no id do carro")
    void shouldReturnPageUserCarAccessDtoByIdUser(@Mock Page<RecognizeEntity> entityPage) {
        var idUser = 1L;
        var carOne = new CarEntity();
        carOne.setPlate("plate-01");

        var carTwo = new CarEntity();
        carTwo.setPlate("plate-02");

        var listCar = List.of(carOne, carTwo);

        when(carRepository.findByUserId(idUser)).thenReturn(listCar);

        var recognizersToCarOne = LongStream.range(0, 5)
            .mapToObj(index -> {
                var recognizer = new RecognizeEntity();
                recognizer.setId(index);
                recognizer.setPlate(carOne.getPlate());
                recognizer.setEpochTime(LocalDateTime.now().plusMinutes(index));
                recognizer.setConfidence(100.0F);
                recognizer.setAccessGranted(Boolean.TRUE);

                return recognizer;
            });

        var recognizersToCarTwo = LongStream.range(0, 3)
            .mapToObj(index -> {
                var recognizer = new RecognizeEntity();
                recognizer.setId(index);
                recognizer.setPlate(carTwo.getPlate());
                recognizer.setEpochTime(LocalDateTime.now().plusMinutes(index));
                recognizer.setConfidence(100.0F);
                recognizer.setAccessGranted(Boolean.TRUE);

                return recognizer;
            });

        var allRecognizers = Stream.concat(recognizersToCarOne, recognizersToCarTwo).toList();

        when(carRepository.findByUserId(idUser)).thenReturn(listCar);
        when(recognizerRepository.findByAccessGrantedTrueAndPlateIn(
            eq(List.of(carOne.getPlate(), carTwo.getPlate())),
            any(Pageable.class)))
            .thenReturn(entityPage);

        when(entityPage.getContent()).thenReturn(allRecognizers);
        when(entityPage.getSize()).thenReturn(allRecognizers.size());
        when(entityPage.getTotalPages()).thenReturn(1);
        when(entityPage.getNumber()).thenReturn(10);
        when(entityPage.getTotalElements()).thenReturn(10000L);

        var allAccessCarUser = recognizeServiceApplication.getAllAccessCarUser(idUser, 10, 0);

        assertEquals(allAccessCarUser.getAmountCars(), listCar.size());
        assertEquals(allAccessCarUser.getDirection(), Direction.DESC.name());
        assertEquals(CREATED_AT, allAccessCarUser.getSorted());
        assertEquals(allAccessCarUser.getSize(), allRecognizers.size());

        assertThat(allRecognizers)
            .hasSize(8)
            .extracting(RecognizeEntity::getPlate)
            .containsAnyOf(
                allRecognizers.stream().map(RecognizeEntity::getPlate).toArray(String[]::new));
    }

    @Test
    @DisplayName("Deve retornar um sumario de reconhecimentos da semana")
    void shouldReturnSummaryWeek() {
        var recognizerSummaryWeek = new RecognizerSummaryWeek() {
            @Override
            public Integer getSevenDay() {
                return 10;
            }

            @Override
            public Integer getSixDay() {
                return 10;
            }

            @Override
            public Integer getFiveDay() {
                return 10;
            }

            @Override
            public Integer getFourDay() {
                return 10;
            }

            @Override
            public Integer getThreeDay() {
                return 10;
            }

            @Override
            public Integer getTwoDay() {
                return 10;
            }

            @Override
            public Integer getOneDay() {
                return 10;
            }

            @Override
            public Integer getNow() {
                return 10;
            }
        };

        when(recognizerRepository.getRecognizerSummaryWeek()).thenReturn(recognizerSummaryWeek);

        var summary = recognizeServiceApplication.getRecognizerSummary();

        assertEquals(recognizerSummaryWeek.getNow(), summary.getNow());
        assertEquals(recognizerSummaryWeek.getOneDay(), summary.getFirstDayBefore());
        assertEquals(recognizerSummaryWeek.getTwoDay(), summary.getSecondDayBefore());
        assertEquals(recognizerSummaryWeek.getThreeDay(), summary.getThirdhDayBefore());
        assertEquals(recognizerSummaryWeek.getFourDay(), summary.getFourthDayBefore());
        assertEquals(recognizerSummaryWeek.getFiveDay(), summary.getFifthDayBefore());
        assertEquals(recognizerSummaryWeek.getSixDay(), summary.getSixthDayBefore());
    }

}
