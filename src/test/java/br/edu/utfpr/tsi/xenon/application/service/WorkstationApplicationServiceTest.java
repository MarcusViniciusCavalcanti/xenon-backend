package br.edu.utfpr.tsi.xenon.application.service;

import static br.edu.utfpr.tsi.xenon.domain.notification.model.TopicApplication.CHANGE_WORKSTATION;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.utfpr.tsi.xenon.application.dto.InputWorkstationDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputWorkstationDto.ModeEnum;
import br.edu.utfpr.tsi.xenon.domain.notification.model.UpdateWorkstationMessage;
import br.edu.utfpr.tsi.xenon.domain.notification.service.SenderMessageWebSocketService;
import br.edu.utfpr.tsi.xenon.domain.notification.service.SendingMessageService;
import br.edu.utfpr.tsi.xenon.domain.recognize.entity.RecognizeEntity;
import br.edu.utfpr.tsi.xenon.domain.workstations.entity.WorkstationEntity;
import br.edu.utfpr.tsi.xenon.domain.workstations.entity.WorstationAndRecognizeSummary;
import br.edu.utfpr.tsi.xenon.domain.workstations.service.WorkstationService;
import br.edu.utfpr.tsi.xenon.structure.MessagesMapper;
import br.edu.utfpr.tsi.xenon.structure.exception.ResourceNotFoundException;
import br.edu.utfpr.tsi.xenon.structure.exception.WorkStationException;
import br.edu.utfpr.tsi.xenon.structure.repository.RecognizerRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository;
import com.github.javafaker.Faker;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - WorkstationCreatorService")
class WorkstationApplicationServiceTest {

    @Mock
    private WorkstationRepository workstationRepository;

    @Mock
    private WorkstationService workstationService;

    @Mock
    private SendingMessageService senderMessageWebSocketService;

    @Mock
    private RecognizerRepository recognizerRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WorkstationApplicationService workstationApplicationService;

    @Test
    @DisplayName("Deve criar uma estação com sucesso")
    void shouldCreateNewWorkStation() {
        var faker = Faker.instance();
        var mode = ModeEnum.AUTOMATIC;
        var ip = faker.internet().ipV4Address();
        var name = faker.rockBand().name();
        var port = 9090;

        var workstation = new WorkstationEntity();
        workstation.setIp(ip);
        workstation.setName(name);
        workstation.setMode(mode.name());

        var input = new InputWorkstationDto()
            .ip(ip)
            .mode(mode)
            .name(name)
            .port(port);

        when(workstationService.formatterIp(input.getIp())).thenReturn(input.getIp());
        when(workstationService.create(ip, name, mode.name(), port)).thenReturn(workstation);
        when(workstationRepository.existsByName(input.getName())).thenReturn(FALSE);
        when(workstationRepository.existsByIp(input.getIp())).thenReturn(FALSE);
        when(workstationRepository.saveAndFlush(workstation)).thenReturn(workstation);

        workstationApplicationService.createNew(input);

        verify(workstationService).create(ip, name, mode.name(), port);
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
        var port = 9090;

        workstation.setIp(ip);
        workstation.setName(name);

        var input = new InputWorkstationDto()
            .ip(workstation.getIp())
            .mode(mode)
            .name(workstation.getName())
            .port(port);

        when(workstationService.formatterIp(ip)).thenReturn(ip);
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
        var port = 9090;

        workstation.setIp(ip);
        workstation.setName(name);

        var input = new InputWorkstationDto()
            .ip(workstation.getIp())
            .mode(mode)
            .name(workstation.getName())
            .port(port);

        when(workstationService.formatterIp(ip)).thenReturn(ip);
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
        var port = 9090;

        var workstation = new WorkstationEntity();
        workstation.setId(1L);
        workstation.setIp(faker.internet().ipV6Address());
        workstation.setName(faker.rockBand().name());
        workstation.setMode(mode.name());

        var input = new InputWorkstationDto()
            .ip(ip)
            .mode(mode)
            .name(name)
            .port(9090);

        when(workstationService.formatterIp(ip)).thenReturn(ip);
        when(workstationRepository.findById(workstation.getId())).thenReturn(Optional.of(workstation));
        when(workstationService.replaceData(workstation, name,  mode.name(), ip, port)).thenReturn(workstation);
        when(workstationRepository.existsByName(name)).thenReturn(FALSE);
        when(workstationRepository.existsByIp(ip)).thenReturn(FALSE);
        when(workstationRepository.saveAndFlush(workstation)).thenReturn(workstation);

        workstationApplicationService.update(input, workstation.getId());

        verify(workstationRepository).findById(workstation.getId());
        verify(workstationService).replaceData(workstation, name, mode.name(), ip, port);
        verify(workstationRepository).existsByName(name);
        verify(workstationRepository).existsByIp(ip);
        verify(workstationRepository).saveAndFlush(workstation);
    }

    @Test
    @DisplayName("Deve verificar se nome existe quando nome for diferente")
    void shouldHaveCheckNameWhenNameStationIsIdf() {
        var faker = Faker.instance();
        var ip = faker.internet().ipV6Address();
        var name = faker.rockBand().name();
        var mode = ModeEnum.AUTOMATIC;
        var port = 9000;

        var workstation = new WorkstationEntity();
        workstation.setId(1L);
        workstation.setName(name);
        workstation.setPort(port);
        workstation.setMode(ModeEnum.MANUAL.name());
        workstation.setIp(faker.internet().ipV6Address());

        var input = new InputWorkstationDto()
            .name(name)
            .port(port)
            .ip(ip)
            .mode(mode);

        when(workstationService.formatterIp(ip)).thenReturn(ip);
        when(workstationRepository.findById(workstation.getId())).thenReturn(Optional.of(workstation));
        when(workstationService.replaceData(workstation, name,  mode.name(), ip, port)).thenReturn(workstation);
        when(workstationRepository.existsByIp(ip)).thenReturn(FALSE);
        when(workstationRepository.saveAndFlush(workstation)).thenReturn(workstation);

        workstationApplicationService.update(input, workstation.getId());

        verify(workstationRepository).findById(workstation.getId());
        verify(workstationService).replaceData(workstation, name, mode.name(), ip, port);
        verify(workstationRepository, never()).existsByName(any());
        verify(workstationRepository).existsByIp(ip);
        verify(workstationRepository).saveAndFlush(workstation);
    }

    @Test
    @DisplayName("Deve verificar se ip existe quando ip for diferente")
    void shouldHaveCheckIpWhenNameStationIsIdf() {
        var faker = Faker.instance();
        var ip = faker.internet().ipV6Address();
        var name = faker.rockBand().name();
        var mode = ModeEnum.AUTOMATIC;
        var port = 9000;

        var workstation = new WorkstationEntity();
        workstation.setId(1L);
        workstation.setName(faker.name().name());
        workstation.setPort(port);
        workstation.setMode(ModeEnum.MANUAL.name());
        workstation.setIp(ip);

        var input = new InputWorkstationDto()
            .name(name)
            .port(port)
            .ip(ip)
            .mode(mode);

        when(workstationService.formatterIp(ip)).thenReturn(ip);
        when(workstationRepository.findById(workstation.getId())).thenReturn(Optional.of(workstation));
        when(workstationService.replaceData(workstation, name,  mode.name(), ip, port)).thenReturn(workstation);
        when(workstationRepository.saveAndFlush(workstation)).thenReturn(workstation);

        workstationApplicationService.update(input, workstation.getId());

        verify(workstationRepository).findById(workstation.getId());
        verify(workstationService).replaceData(workstation, name, mode.name(), ip, port);
        verify(workstationRepository).existsByName(name);
        verify(workstationRepository, never()).existsByIp(any());
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
        workstation.setKey("key");

        doNothing()
            .when(workstationRepository)
            .delete(workstation);
        when(workstationRepository.findById(workstation.getId())).thenReturn(Optional.of(workstation));
        doNothing()
            .when(senderMessageWebSocketService)
            .sendBeforeTransactionCommit(
                any(UpdateWorkstationMessage.class),
                eq(CHANGE_WORKSTATION.topicTo(workstation.getId().toString())));

        workstationApplicationService.delete(workstation.getId());

        verify(workstationRepository).findById(workstation.getId());
        verify(workstationRepository).delete(workstation);
        verify(senderMessageWebSocketService).sendBeforeTransactionCommit(
            any(UpdateWorkstationMessage.class),
            eq(CHANGE_WORKSTATION.topicTo(workstation.getId().toString())));
    }

    @Test
    @DisplayName("Deve lista de Dtos de estações de trabalho")
    void shouldReturnListDtos() {
        when(workstationRepository.findAll()).thenReturn(List.of());
        workstationApplicationService.getAll();
        verify(workstationRepository).findAll();
    }

    @Test
    @DisplayName("Deve lista de Dtos de estações de trabalho")
    void shouldThrowsResourceNotFoundListDtos() {
        when(workstationRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> workstationApplicationService.delete(1L));
        verify(workstationRepository).findById(1L);
    }

    @Test
    @DisplayName("Deve retornar Estação de trabalho por id")
    void shouldReturnWorkstationById() {
        var faker = Faker.instance();

        var workstation = new WorkstationEntity();
        workstation.setId(1L);
        workstation.setIp(faker.internet().ipV6Address());
        workstation.setName(faker.rockBand().name());
        workstation.setMode(ModeEnum.MANUAL.name());
        workstation.setKey("key");
        workstation.setPort(9090);

        when(workstationRepository.findById(workstation.getId())).thenReturn(Optional.of(workstation));

        var result = workstationApplicationService.getWorkstationById(workstation.getId());

        verify(workstationRepository).findById(workstation.getId());

        assertEquals(workstation.getId(), result.getId());
        assertEquals(workstation.getIp(), result.getIp());
        assertEquals(workstation.getName(), result.getName());
        assertEquals(workstation.getMode(), result.getMode().name());
        assertEquals(workstation.getKey(), result.getKey());
        assertEquals(workstation.getPort(), result.getPort());
    }

    @Test
    @DisplayName("Deve retonar a lista de sumários de estações de trabalho cadastradas")
    void shouldReturnListSummaryWorkstation() {
        var summary = new Summary();

        var listSummary = List.<WorstationAndRecognizeSummary>of(summary);

        when(workstationRepository.getWorkstationSummary()).thenReturn(listSummary);

        var result = workstationApplicationService.getWorkstationSummary();

        assertEquals(result.size(), listSummary.size());

        var resultSummary = result.get(0);
        assertEquals(summary.getId(), resultSummary.getId());
        assertEquals(summary.getName(), resultSummary.getWorkstationName());
        assertEquals(summary.getRecognizers(), resultSummary.getAmountAccess());
    }

    @Test
    @DisplayName("Deve aprovar o acesso e atualizar a entidade reconhecimento")
    void shoultApprovedAccessAndUpdateRecognizerEntity(@Mock ResponseEntity<Void> responseEntity) {
        var faker = Faker.instance();

        var workstation = new WorkstationEntity();
        workstation.setId(1L);
        workstation.setIp(faker.internet().ipV6Address());
        workstation.setName(faker.rockBand().name());
        workstation.setMode(ModeEnum.MANUAL.name());
        workstation.setKey("key");
        workstation.setPort(9090);

        doNothing()
            .when(recognizerRepository)
            .updateAccessAuthorized(1L);

        when(recognizerRepository.findById(anyLong())).thenReturn(Optional.of(new RecognizeEntity()));
        configureSendRequestMock(workstation, responseEntity, HttpStatus.NO_CONTENT);

        workstationApplicationService.approvedAccess(1L, 1L);

        verifyRequestSend(responseEntity, workstation);
        verify(recognizerRepository).updateAccessAuthorized(1L);
    }

    @Test
    @DisplayName("Deve enviar pedido para liberar o acesso e ")
    void shoultApprovedAccess(@Mock ResponseEntity<Void> responseEntity) {
        var faker = Faker.instance();

        var workstation = new WorkstationEntity();
        workstation.setId(1L);
        workstation.setIp(faker.internet().ipV6Address());
        workstation.setName(faker.rockBand().name());
        workstation.setMode(ModeEnum.MANUAL.name());
        workstation.setKey("key");
        workstation.setPort(9090);

        configureSendRequestMock(workstation, responseEntity, HttpStatus.NO_CONTENT);

        workstationApplicationService.sendRequestOpen(1L);

        verifyRequestSend(responseEntity, workstation);
    }

    @Test
    @DisplayName("Não deve fazer nada quando retornado bad request")
    void shouldNothingWhenReturnBadrequest(@Mock ResponseEntity<Void> responseEntity) {
        var faker = Faker.instance();

        var workstation = new WorkstationEntity();
        workstation.setId(1L);
        workstation.setIp(faker.internet().ipV6Address());
        workstation.setName(faker.rockBand().name());
        workstation.setMode(ModeEnum.MANUAL.name());
        workstation.setKey("key");
        workstation.setPort(9090);

        configureSendRequestMock(workstation, responseEntity, HttpStatus.BAD_REQUEST);

        workstationApplicationService.sendRequestOpen(1L);

        verifyRequestSend(responseEntity, workstation);
    }

    @Test
    @DisplayName("Deve capturar exception no envio de requisição para abrir")
    void shouldCatchWhenSendRequestToOpen() {
        var faker = Faker.instance();

        var workstation = new WorkstationEntity();
        workstation.setId(1L);
        workstation.setIp(faker.internet().ipV6Address());
        workstation.setName(faker.rockBand().name());
        workstation.setMode(ModeEnum.MANUAL.name());
        workstation.setKey("key");
        workstation.setPort(9090);

        when(workstationRepository.findById(1L)).thenReturn(Optional.of(workstation));
        when(workstationService.buildUriOpen(workstation.getIp(), workstation.getPort())).thenReturn("url");
        when(restTemplate.postForEntity("url", "{}", Void.class)).thenThrow(RestClientException.class);

        assertDoesNotThrow(() -> workstationApplicationService.sendRequestOpen(1L));

        verify(workstationService).buildUriOpen(workstation.getIp(), workstation.getPort());
        verify(restTemplate).postForEntity("url", "{}", Void.class);
    }

    private void verifyRequestSend(
        ResponseEntity<Void> responseEntity,
        WorkstationEntity workstation) {
        verify(responseEntity).getStatusCode();
        verify(workstationService).buildUriOpen(workstation.getIp(), workstation.getPort());
        verify(restTemplate).postForEntity("url", "{}", Void.class);
    }

    private void configureSendRequestMock(
        WorkstationEntity workstation,
        ResponseEntity<Void> responseEntity,
        HttpStatus httpStatus) {
        when(workstationRepository.findById(1L)).thenReturn(Optional.of(workstation));
        when(workstationService.buildUriOpen(workstation.getIp(), workstation.getPort())).thenReturn("url");
        when(restTemplate.postForEntity("url", "{}", Void.class)).thenReturn(responseEntity);
        when(responseEntity.getStatusCode()).thenReturn(httpStatus);
    }

    static class Summary implements WorstationAndRecognizeSummary {
        @Override
        public Long getId() {
            return 1L;
        }

        @Override
        public String getName() {
            return "name";
        }

        @Override
        public Integer getRecognizers() {
            return 10;
        }
    }
}
