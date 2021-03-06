package br.edu.utfpr.tsi.xenon.domain.workstations.service;

import br.edu.utfpr.tsi.xenon.domain.security.service.KeyService;
import br.edu.utfpr.tsi.xenon.domain.workstations.entity.WorkstationEntity;
import java.security.NoSuchAlgorithmException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkstationService {

    private static final String IPV4_PATTERN = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$";
    private static final String HTTP_IP_OPEN_IPV4 = "http://%s:%d/open";
    private static final String HTTP_IP_OPEN_IPV6 = "http://[%s]:%d/open";


    private final KeyService keyService;

    public WorkstationEntity create(String ip, String name, String mode, Integer port) {
        var ipFormatted = formatterIp(ip);
        var workstation = new WorkstationEntity();
        workstation.setName(name);
        workstation.setIp(ipFormatted);
        workstation.setMode(mode);
        workstation.setPort(port);

        try {
            var key = keyService.createKey();
            workstation.setKey(key);
        } catch (NoSuchAlgorithmException e) {
            log.error("erro ao criar chave {}", e.getMessage());
            throw new IllegalStateException(e);
        }

        return workstation;
    }

    public WorkstationEntity replaceData(
        WorkstationEntity entity,
        String name,
        String mode,
        String ip,
        Integer port) {

        var ipFormatted = formatterIp(ip);
        entity.setMode(mode);
        entity.setName(name);
        entity.setIp(ipFormatted);
        entity.setPort(port);
        return entity;
    }

    public String formatterIp(String ip) {
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

    public String buildUriOpen(String ip, Integer port) {
        var isIpv4 = ip.matches(IPV4_PATTERN);
        if (isIpv4) {
            return HTTP_IP_OPEN_IPV4.formatted(ip, port);
        }
        return HTTP_IP_OPEN_IPV6.formatted(ip, port);
    }
}
