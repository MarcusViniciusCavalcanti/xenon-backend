package br.edu.utfpr.tsi.xenon.domain.workstations.service;

import static org.junit.jupiter.api.Assertions.*;

import br.edu.utfpr.tsi.xenon.application.dto.InputWorkstationDto.ModeEnum;
import com.github.javafaker.Faker;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste - Unidade - ValidatorFile")
class WorkstationServiceTest {

    @InjectMocks
    private WorkstationService workstationService;

    @Test
    @DisplayName("Deve criar workstation com ipv4")
    void shouldHaveCreateWorkstationIpv4() {
        var faker = Faker.instance();
        var ip = faker.internet().ipV4Address();
        var split = ip.split("\\.");
        var ipExpected = "%s.%s.%s.%s".formatted(
            StringUtils.leftPad(split[0], 3, '0'),
            StringUtils.leftPad(split[1], 3, '0'),
            StringUtils.leftPad(split[2], 3, '0'),
            StringUtils.leftPad(split[3], 3, '0'));
        var name = faker.rockBand().name();
        var mode = ModeEnum.MANUAL.name();

        var workstation = workstationService.create(ip, name, mode);
        assertEquals(ipExpected, workstation.getIp());
        assertEquals(name, workstation.getName());
        assertEquals(mode, workstation.getMode());
    }

    @Test
    @DisplayName("Deve criar workstation com ipv6")
    void shouldHaveCreateWorkstationIpv6() {
        var faker = Faker.instance();
        var ip = faker.internet().ipV6Address();
        var name = faker.rockBand().name();
        var mode = ModeEnum.AUTOMATIC.name();

        var workstation = workstationService.create(ip, name, mode);
        assertEquals(ip, workstation.getIp());
        assertEquals(name, workstation.getName());
        assertEquals(mode, workstation.getMode());
    }
}
