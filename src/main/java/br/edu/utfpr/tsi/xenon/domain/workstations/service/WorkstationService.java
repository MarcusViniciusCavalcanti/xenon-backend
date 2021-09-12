package br.edu.utfpr.tsi.xenon.domain.workstations.service;

import br.edu.utfpr.tsi.xenon.domain.security.service.KeyService;
import br.edu.utfpr.tsi.xenon.domain.workstations.entity.WorkstationEntity;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkstationService {

    private static final String IPV4_PATTERN = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$";

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
}
