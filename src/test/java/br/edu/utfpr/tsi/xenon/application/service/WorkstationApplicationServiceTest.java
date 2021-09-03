package br.edu.utfpr.tsi.xenon.application.service;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.utfpr.tsi.xenon.application.dto.InputWorkstationDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputWorkstationDto.ModeEnum;
import br.edu.utfpr.tsi.xenon.domain.workstations.entity.WorkstationEntity;
import br.edu.utfpr.tsi.xenon.domain.workstations.service.WorkstationService;
import br.edu.utfpr.tsi.xenon.structure.MessagesMapper;
import br.edu.utfpr.tsi.xenon.structure.exception.WorkStationException;
import br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository;
import com.github.javafaker.Faker;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - WorkstationCreatorService")
class WorkstationApplicationServiceTest {

    @Mock
    private WorkstationRepository workstationRepository;

    @Mock
    private WorkstationService workstationService;

    @InjectMocks
    private WorkstationApplicationService workstationApplicationService;

    @Test
    @DisplayName("Deve criar uma estação com sucesso")
    void shouldCreateNewWorkStation() {
        var faker = Faker.instance();
        var mode = ModeEnum.AUTOMATIC;
        var ip = faker.internet().ipV4Address();
        var name = faker.rockBand().name();

        var workstation = new WorkstationEntity();
        workstation.setIp(ip);
        workstation.setName(name);
        workstation.setMode(mode.name());

        var input = new InputWorkstationDto()
            .ip(ip)
            .mode(mode)
            .name(name);

        when(workstationService.create(ip, name, mode.name())).thenReturn(workstation);
        when(workstationRepository.existsByName(input.getName())).thenReturn(FALSE);
        when(workstationRepository.existsByIp(input.getIp())).thenReturn(FALSE);
        when(workstationRepository.saveAndFlush(workstation)).thenReturn(workstation);

        workstationApplicationService.createNew(input);

        verify(workstationService).create(ip, name, mode.name());
        verify(workstationRepository).existsByName(input.getName());
        verify(workstationRepository).existsByIp(input.getIp());
        verify(workstationRepository).saveAndFlush(workstation);
    }

    @Test
    @DisplayName("Deve lançar Exception quando dados não passou na validação")
    void shouldThrowsExceptionWhenNameExist() {
        var faker = Faker.instance();
        var workstation = new WorkstationEntity();
        var ip = faker.internet().ipV6Address();
        var mode = ModeEnum.AUTOMATIC;
        var name = faker.rockBand().name();

        workstation.setIp(ip);
        workstation.setName(name);

        var input = new InputWorkstationDto()
            .ip(workstation.getIp())
            .mode(mode)
            .name(workstation.getName());

        when(workstationService.create(ip, name, mode.name())).thenReturn(workstation);
        when(workstationRepository.existsByName(input.getName())).thenReturn(TRUE);

        var exception = assertThrows(WorkStationException.class,
            () -> workstationApplicationService.createNew(input));

        assertEquals(MessagesMapper.NAME_WORKSTATION_EXIST.getCode(), exception.getCode());
        assertEquals(name, exception.getValue());

        verify(workstationRepository).existsByName(name);
        verify(workstationRepository, never()).saveAndFlush(any());
        verify(workstationRepository, never()).existsByIp(any());
    }

    @Test
    @DisplayName("Deve lançar Exception quando dados não passou na validação")
    void shouldThrowsExceptionWhenIpExist() {
        var faker = Faker.instance();
        var workstation = new WorkstationEntity();
        var ip = faker.internet().ipV6Address();
        var mode = ModeEnum.AUTOMATIC;
        var name = faker.rockBand().name();

        workstation.setIp(ip);
        workstation.setName(name);

        var input = new InputWorkstationDto()
            .ip(workstation.getIp())
            .mode(mode)
            .name(workstation.getName());

        when(workstationService.create(ip, name, mode.name())).thenReturn(workstation);
        when(workstationRepository.existsByName(input.getName())).thenReturn(FALSE);
        when(workstationRepository.existsByIp(input.getIp())).thenReturn(TRUE);

        var exception = assertThrows(WorkStationException.class,
            () -> workstationApplicationService.createNew(input));

        assertEquals(MessagesMapper.IP_WORKSTATION_EXIST.getCode(), exception.getCode());
        assertEquals(ip, exception.getValue());

        verify(workstationRepository).existsByName(name);
        verify(workstationRepository).existsByIp(ip);
        verify(workstationRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Deve atualizar com sucesso")
    void shouldHaveUpdateWorkstation() {
        var faker = Faker.instance();
        var mode = ModeEnum.AUTOMATIC;
        var ip = faker.internet().ipV4Address();
        var name = faker.rockBand().name();

        var workstation = new WorkstationEntity();
        workstation.setId(1L);
        workstation.setIp(faker.internet().ipV6Address());
        workstation.setName(faker.rockBand().name());
        workstation.setMode(mode.name());

        var input = new InputWorkstationDto()
            .ip(ip)
            .mode(mode)
            .name(name);

        when(workstationRepository.findById(workstation.getId())).thenReturn(Optional.of(workstation));
        when(workstationService.replaceData(workstation, name,  mode.name(), ip)).thenReturn(workstation);
        when(workstationRepository.existsByName(name)).thenReturn(FALSE);
        when(workstationRepository.existsByIp(ip)).thenReturn(FALSE);
        when(workstationRepository.saveAndFlush(workstation)).thenReturn(workstation);

        workstationApplicationService.update(input, workstation.getId());

        verify(workstationRepository).findById(workstation.getId());
        verify(workstationService).replaceData(workstation, name, mode.name(), ip);
        verify(workstationRepository).existsByName(name);
        verify(workstationRepository).existsByIp(ip);
        verify(workstationRepository).saveAndFlush(workstation);
    }

    @Test
    @DisplayName("Deve deletar com sucesso")
    void shouldHaveDeleteWorkstation() {
        var faker = Faker.instance();

        var workstation = new WorkstationEntity();
        workstation.setId(1L);
        workstation.setIp(faker.internet().ipV6Address());
        workstation.setName(faker.rockBand().name());

        doNothing()
            .when(workstationRepository)
            .delete(workstation);
        when(workstationRepository.findById(workstation.getId())).thenReturn(Optional.of(workstation));

        workstationApplicationService.delete(workstation.getId());

        verify(workstationRepository).findById(workstation.getId());
        verify(workstationRepository).delete(workstation);
    }
}
