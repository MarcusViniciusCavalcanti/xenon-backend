package br.edu.utfpr.tsi.xenon.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.domain.security.service.SecurityContextUserService;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.factory.TypeUser;
import br.edu.utfpr.tsi.xenon.structure.ParamsQuerySearchUserDto;
import br.edu.utfpr.tsi.xenon.structure.ParamsQuerySearchUserDto.DirectionEnum;
import br.edu.utfpr.tsi.xenon.structure.ParamsQuerySearchUserDto.SortedEnum;
import br.edu.utfpr.tsi.xenon.structure.exception.ResourceNotFoundException;
import br.edu.utfpr.tsi.xenon.structure.repository.BasicSpecification;
import br.edu.utfpr.tsi.xenon.structure.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - UserGetterServiceApplication")
class UserGetterServiceApplicationTest {

    @Mock
    private SecurityContextUserService securityContextUserService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Specification<UserEntity> specification;

    @Mock
    private Page<UserEntity> entityPage;

    @Mock
    private BasicSpecification<UserEntity, ParamsQuerySearchUserDto> getterAllUserSpec;

    @InjectMocks
    private UserGetterServiceApplication userGetterServiceApplication;

    @Test
    @DisplayName("Deve tratar silenciosamente qualquer exception")
    void shouldCatchAnyException() {
        lenient()
            .doThrow(NullPointerException.class)
            .when(securityContextUserService)
            .getUserByContextSecurity(any());

        assertDoesNotThrow(() -> userGetterServiceApplication.getUserByToken("token"));
    }

    @Test
    @DisplayName("Deve retornar o usuário vazio quando usuário uma exception for lançada")
    void shouldReturnUserEmptyWhenThrowsException() {
        assertDoesNotThrow(() -> userGetterServiceApplication.getUserByToken("token"));
        verify(userRepository, never()).findByAccessCard(any());
    }

    @Test
    @DisplayName("Deve retornar um usuário por id")
    void shouldReturnUserWhenGetById() {
        var user = new UserEntity();
        user.setTypeUser(TypeUser.SPEAKER.name());
        var accessCard = new AccessCardEntity();
        user.setAccessCard(accessCard);
        var id = 1L;

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userGetterServiceApplication.getUserById(id);

        verify(userRepository).findById(id);
    }

    @Test
    @DisplayName("Deve lançar ResourceException quando usuário não foi encontrado")
    void shouldThrowsResourceExceptionWhenGetById() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> userGetterServiceApplication.getUserById(1L));
    }

    @Test
    @DisplayName("Deve retornar UserPage")
    void shouldReturnUserPageDto() {
        var user = new UserEntity();
        user.setTypeUser(TypeUser.SPEAKER.name());
        var accessCard = new AccessCardEntity();
        user.setAccessCard(accessCard);
        var params = ParamsQuerySearchUserDto.builder()
            .direction(DirectionEnum.ASC)
            .sorted(SortedEnum.CREATED)
            .size(1L)
            .page(10L)
            .build();

        when(getterAllUserSpec.filterBy(params)).thenReturn(specification);
        when(userRepository.findAll(eq(specification), any(Pageable.class))).thenReturn(entityPage);
        when(entityPage.getContent()).thenReturn(List.of(user));
        when(entityPage.getTotalElements()).thenReturn(1L);
        when(entityPage.getNumber()).thenReturn(10);
        when(entityPage.getSize()).thenReturn(10);
        when(entityPage.getTotalPages()).thenReturn(1);

        userGetterServiceApplication.getAllUser(params);

        verify(entityPage).getContent();
        verify(entityPage).getTotalElements();
        verify(entityPage).getNumber();
        verify(entityPage).getSize();
        verify(entityPage).getTotalPages();
        verify(userRepository).findAll(eq(specification), any(Pageable.class));
        verify(getterAllUserSpec).filterBy(params);
    }
}
