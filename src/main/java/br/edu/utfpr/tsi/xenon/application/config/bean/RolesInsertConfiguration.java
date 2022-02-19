package br.edu.utfpr.tsi.xenon.application.config.bean;

import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.domain.security.entity.RoleEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.factory.TypeUser;
import br.edu.utfpr.tsi.xenon.structure.repository.AccessCardRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.RoleRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RolesInsertConfiguration implements ApplicationRunner {

    private final RoleRepository repository;
    private final UserRepository userRepository;
    private final AccessCardRepository accessCardRepository;

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

        var roles = List.of(roleDrive, roleAdmin, roleOperator);
        repository.saveAll(roles);

        var hasCreateUser = accessCardRepository.existsByUsername("user_admin@admin.com");

        if (Boolean.FALSE.equals(hasCreateUser)) {
            var user = new UserEntity();
            user.setTypeUser(TypeUser.SERVICE.name());
            user.setName("Venicius Cavalcanti");
            user.setAuthorisedAccess(Boolean.TRUE);

            var accessCard = new AccessCardEntity();
            accessCard.setRoleEntities(roles);
            accessCard.setUser(user);
            accessCard.setUsername("user_admin@admin.com");
            accessCard.setPassword("$2a$10$wkCHAIGphKy/rrIaomiGAu.Vm.hGMWTWjSjoTxUITMqxP.EfVCRee");
            accessCard.setEnabled(Boolean.TRUE);
            accessCard.setAccountNonExpired(Boolean.TRUE);
            accessCard.setAccountNonLocked(Boolean.TRUE);
            accessCard.setCredentialsNonExpired(Boolean.TRUE);

            user.setAccessCard(accessCard);
            userRepository.save(user);
        }
    }
}
