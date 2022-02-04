package br.edu.utfpr.tsi.xenon.domain.workstations.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import br.edu.utfpr.tsi.xenon.application.dto.InputWorkstationDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputWorkstationDto.ModeEnum;
import br.edu.utfpr.tsi.xenon.domain.security.service.KeyService;
import br.edu.utfpr.tsi.xenon.domain.workstations.entity.WorkstationEntity;
import com.github.javafaker.Faker;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - ValidatorFile")
class WorkstationServiceTest {

    @Mock
    private KeyService keyService;

    @InjectMocks
    private WorkstationService workstationService;

    @Test
    @DisplayName("Deve criar workstation com ipv4")
    void shouldHaveCreateWorkstationIpv4() throws NoSuchAlgorithmException {
        var faker = Faker.instance();
        var ip = faker.internet().ipV4Address();
        var split = ip.split("\\.");
        var port = 9090;
        var ipExpected = "%s.%s.%s.%s".formatted(
            StringUtils.leftPad(split[0], 3, '0'),
            StringUtils.leftPad(split[1], 3, '0'),
            StringUtils.leftPad(split[2], 3, '0'),
            StringUtils.leftPad(split[3], 3, '0'));
        var name = faker.rockBand().name();
        var mode = ModeEnum.MANUAL.name();

        when(keyService.createKey()).thenReturn("key");

        var workstation = workstationService.create(ip, name, mode, port);
        assertEquals(ipExpected, workstation.getIp());
        assertEquals(name, workstation.getName());
        assertEquals(mode, workstation.getMode());
        assertEquals(port, workstation.getPort());
    }

    @Test
    @DisplayName("Deve criar workstation com ipv6")
    void shouldHaveCreateWorkstationIpv6() throws NoSuchAlgorithmException {
        var faker = Faker.instance();
        var ip = faker.internet().ipV6Address();
        var name = faker.rockBand().name();
        var mode = ModeEnum.AUTOMATIC.name();
        var port = 9000;

        when(keyService.createKey()).thenReturn("key");

        var workstation = workstationService.create(ip, name, mode, port);
        assertEquals(ip, workstation.getIp());
        assertEquals(name, workstation.getName());
        assertEquals(mode, workstation.getMode());
        assertEquals(port, workstation.getPort());
    }

    @Test
    @DisplayName("Deve deve lançar IllegalStateException")
    void shouldThrowsIllegalStateException() throws NoSuchAlgorithmException {
        var faker = Faker.instance();
        var ip = faker.internet().ipV6Address();
        var name = faker.rockBand().name();
        var mode = ModeEnum.AUTOMATIC.name();
        var port = 9000;

        when(keyService.createKey()).thenThrow(new NoSuchAlgorithmException());

        assertThrows(IllegalStateException.class, () -> workstationService.create(ip, name, mode, port));
    }

    @Test
    @DisplayName("Deve formatar uri para ipv4")
    void shouldReturnIpv4Formatted() {
        var faker = Faker.instance();
        var ip = faker.internet().ipV4Address();
        var port = 9000;
        var expecteFormatter = "http://%s:%d/open".formatted(ip, port);

        var result = workstationService.buildUriOpen(ip, port);

        assertEquals(expecteFormatter, result);
    }

    @Test
    @DisplayName("Deve formatar uri para ipv4")
    void shouldReturnIpv6Formatted() {
        var faker = Faker.instance();
        var ip = faker.internet().ipV6Address();
        var port = 9000;
        var expecteFormatter = "http://[%s]:%d/open".formatted(ip, port);

        var result = workstationService.buildUriOpen(ip, port);

        assertEquals(expecteFormatter, result);
    }
}
