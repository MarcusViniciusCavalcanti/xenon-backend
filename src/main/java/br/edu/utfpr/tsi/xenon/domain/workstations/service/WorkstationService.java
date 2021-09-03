package br.edu.utfpr.tsi.xenon.domain.workstations.service;

import br.edu.utfpr.tsi.xenon.domain.workstations.entity.WorkstationEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class WorkstationService {

    private static final String IPV4_PATTERN = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$";

    public WorkstationEntity create(String ip, String name, String mode) {
        var ipFormatted = formatterIp(ip);
        var workstation = new WorkstationEntity();
        workstation.setName(name);
        workstation.setIp(ipFormatted);
        workstation.setMode(mode);

        return workstation;
    }

    public WorkstationEntity replaceData(
        WorkstationEntity entity,
        String name,
        String mode,
        String ip) {

        var ipFormatted = formatterIp(ip);
        entity.setMode(mode);
        entity.setName(name);
        entity.setIp(ipFormatted);
        return entity;
    }

    private String formatterIp(String ip) {
        var isIpv4 = ip.matches(IPV4_PATTERN);
        if (isIpv4) {
            var split = ip.split("\\.");
            return "%s.%s.%s.%s".formatted(
                StringUtils.leftPad(split[0], 3, '0'),
                StringUtils.leftPad(split[1], 3, '0'),
                StringUtils.leftPad(split[2], 3, '0'),
                StringUtils.leftPad(split[3], 3, '0'));
        }

        return ip;
    }
}
