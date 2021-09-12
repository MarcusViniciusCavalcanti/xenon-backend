package br.edu.utfpr.tsi.xenon.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.utfpr.tsi.xenon.application.dto.InputAccessUserDto;
import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.domain.security.service.SecurityContextUserService;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.structure.MessagesMapper;
import br.edu.utfpr.tsi.xenon.structure.exception.ResourceNotFoundException;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.UserRepository;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - UserDeleterApplicationService")
class UserDeleterApplicationServiceTest {

    @Mock
    private SecurityContextUserService securityContextUserService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private UserDeleterApplicationService userDeleterApplicationService;

    @Test
    @DisplayName("Deve desativar conta do usuário e remover todos os carros")
    void shouldHaveDisabledAccountAndRemoverAllCars() {
        var user = new UserEntity();
        var accessCard = new AccessCardEntity();
        user.setAccessCard(accessCard);
        var car = new CarEntity();
        var cars = new LinkedList<CarEntity>();
        cars.add(car);
        user.setCar(cars);

        when(securityContextUserService.getUserByContextSecurity("token"))
            .thenReturn(Optional.of(user));
        when(userRepository.saveAndFlush(user)).thenReturn(user);
        doNothing()
            .when(carRepository)
            .deleteByUser(user);

        userDeleterApplicationService.disableAccount("token", "reson");

        verify(securityContextUserService).getUserByContextSecurity("token");
        verify(carRepository).deleteByUser(user);
    }

    @Test
    @DisplayName("Deve o administrador desativar a conta do usuário e remover todos os carros")
    void shouldHaveAdminDisabledAccountAndRemoverAllCars() {
        var id = 1L;
        var user = new UserEntity();
        var accessCard = new AccessCardEntity();
        user.setAccessCard(accessCard);
        var car = new CarEntity();
        var cars = new LinkedList<CarEntity>();
        cars.add(car);
        user.setCar(cars);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.saveAndFlush(user)).thenReturn(user);
        doNothing()
            .when(carRepository)
            .deleteByUser(user);

        var input = new InputAccessUserDto().userId(id).reason("reason");
        var result = userDeleterApplicationService.disableAccount(input);

        assertEquals(MessagesMapper.USER_ACCOUNT_DEACTIVATED.getCode(), result.getResult());
        verify(userRepository).findById(id);
        verify(carRepository).deleteByUser(user);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException")
    void shouldThrowsResourceNotFoundExceptionInUserNotFoundWhenDisableAccount() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> userDeleterApplicationService.disableAccount(new InputAccessUserDto()));

        verify(userRepository).findById(any());
        verify(userRepository, never()).saveAndFlush(any());
        verify(carRepository, never()).deleteByUser(any());
    }
}
