package br.edu.utfpr.tsi.xenon.application.config.bean;

import br.edu.utfpr.tsi.xenon.domain.security.entity.RoleEntity;
import br.edu.utfpr.tsi.xenon.structure.repository.RoleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RolesInsertConfiguration implements ApplicationRunner {

    private final RoleRepository repository;

    @Override
    public void run(ApplicationArguments args) {
        var roleDrive = new RoleEntity();
        roleDrive.setId(1L);
        roleDrive.setDescription("Perfil Motorista");
        roleDrive.setName("ROLE_DRIVER");

        var roleAdmin = new RoleEntity();
        roleAdmin.setId(2L);
        roleAdmin.setDescription("Perfil Administrador");
        roleAdmin.setName("ROLE_ADMIN");

        var roleOperator = new RoleEntity();
        roleOperator.setId(3L);
        roleOperator.setDescription("Perfil Operdor");
        roleOperator.setName("ROLE_OPERATOR");

        repository.saveAll(List.of(roleDrive, roleAdmin, roleOperator));
    }
}
