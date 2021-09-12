package br.edu.utfpr.tsi.xenon.domain.user.aggregator;

import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.PLATE_ALREADY;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.PLATE_INVALID;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.utfpr.tsi.xenon.application.dto.InputNewCarDto;
import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.domain.security.entity.RoleEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.factory.TypeUser;
import br.edu.utfpr.tsi.xenon.structure.MessagesMapper;
import br.edu.utfpr.tsi.xenon.structure.exception.BusinessException;
import br.edu.utfpr.tsi.xenon.structure.exception.PlateException;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - CarsAggregator")
class CarsAggregatorTest {

    @Mock
    private CarRepository repository;

    @InjectMocks
    private CarsAggregator carsAggregator;

    @ParameterizedTest
    @MethodSource("providerArgsToInvalidPlateOrModel")
    @DisplayName("Não deve incluir carro quando placa ou modelo do carro estão vazio ou nulo")
    void shouldNotHaveIncludeCar(String model, String plate) {
        var user = new UserEntity();
        carsAggregator.includeNewCar(user, model, plate);

        assertTrue(user.getCar().isEmpty());

        verify(repository, never()).exists(any());
    }

    @ParameterizedTest(name = "testando placa [{0}]")
    @ValueSource(strings = {
        "ab1",
        "abb123",
        "abbb1234",
        "abb123b",
        "abb_123b",
        "abb-1b3b",
        "abb.1234",
        "abb.1c34",
        "abb12333",
    })
    @DisplayName("Não lançar PlateException quando a placa for inválida")
    void shouldThrowsPlateException(String plate) {
        var user = new UserEntity();

        var exception = assertThrows(PlateException.class,
            () -> carsAggregator.includeNewCar(user, "model car", plate));

        assertEquals(PLATE_INVALID.getCode(), exception.getCode());
        assertEquals(plate, exception.getPlate());
    }

    @Test
    @DisplayName("Não lançar PlateException quando a placa já foi cadastrada")
    void shouldThrowsPlateExceptionWhenPlateAlready() {
        var plateCar = "ABC-1234";
        var user = new UserEntity();

        when(repository.exists(any())).thenReturn(TRUE);

        var exception = assertThrows(PlateException.class,
            () -> carsAggregator.includeNewCar(user, "model car", plateCar));

        assertEquals(PLATE_ALREADY.getCode(), exception.getCode());
        assertEquals(plateCar, exception.getPlate());
    }

    @ParameterizedTest
    @MethodSource("providerArgsToFormatterPlate")
    @DisplayName("Deve formatar do formato padrão da placa")
    void shouldHaveFormatterPlate(String actual, String expected) {
        var user = new UserEntity();
        carsAggregator.includeNewCar(user, "Model car", actual);

        assertFalse(user.getCar().isEmpty());
        assertEquals(expected, user.getCar().stream().toList().get(0).getPlate());
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando tentar incluir carro mas, atingiu o limite de 5")
    void shouldThrowsBusinessExceptionCarsExceededLimit() {
        var user = new UserEntity();

        var cars = IntStream.rangeClosed(0, 5).boxed()
            .map(index -> {
                var car = new CarEntity();
                car.setId(index.longValue());
                car.setModel("model");
                car.setPlate("plate");

                return car;
            }).collect(Collectors.toCollection(LinkedList::new));
        user.setCar(cars);

        var exception = assertThrows(BusinessException.class,
            () -> carsAggregator.includeNewCar(user, "model", "plate"));

        assertEquals(MessagesMapper.LIMIT_EXCEEDED_CAR.getCode(), exception.getCode());
    }

    private static Stream<Arguments> providerArgsToInvalidPlateOrModel() {
        return Stream.of(
            Arguments.of(" ", "abc1234"),
            Arguments.of("Model car", ""),
            Arguments.of(null, "abc1234"),
            Arguments.of("Model car", null)
        );
    }

    private static Stream<Arguments> providerArgsToFormatterPlate() {
        return Stream.of(
            Arguments.of("abc1234", "ABC-1234"),
            Arguments.of("abc1b34", "ABC-1B34"),
            Arguments.of("Abc1B34", "ABC-1B34"),
            Arguments.of("ABC1234", "ABC-1234"),
            Arguments.of("ABC-1234", "ABC-1234"),
            Arguments.of("ABC_1234", "ABC-1234")
        );
    }
}
