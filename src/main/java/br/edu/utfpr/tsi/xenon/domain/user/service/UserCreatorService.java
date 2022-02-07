package br.edu.utfpr.tsi.xenon.domain.user.service;

import br.edu.utfpr.tsi.xenon.application.dto.InputRegistryStudentDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputUserDto;
import br.edu.utfpr.tsi.xenon.domain.user.aggregator.AccessCardAggregator;
import br.edu.utfpr.tsi.xenon.domain.user.aggregator.AvatarAggregator;
import br.edu.utfpr.tsi.xenon.domain.user.aggregator.RolesAggregator;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.factory.TypeUser;
import br.edu.utfpr.tsi.xenon.domain.user.factory.UserFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCreatorService {

    private final AccessCardAggregator accessCardAggregator;
    private final RolesAggregator rolesAggregator;
    private final AvatarAggregator avatarAggregator;

    @Transactional(propagation = Propagation.MANDATORY)
    public UserEntity createNewStudents(InputRegistryStudentDto input) {
        log.info("Executando cadastro de usuário");
        var userFactory = UserFactory.getInstance();
        var entity = userFactory.create(input);

        accessCardAggregator.includeAccessCard(
            entity,
            input.getEmail(),
            input.getPassword(),
            input.getConfirmPassword()
        );

        rolesAggregator.includeRoles(entity.getAccessCard(), TypeUser.STUDENTS, List.of(1L));
        avatarAggregator.includeDefaultAvatarUrl(entity);

        return entity;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public UserEntity createNewUser(InputUserDto input, String pass) {
        log.info("Executando cadastro de usuário");
        var userFactory = UserFactory.getInstance();
        var entity = userFactory.create(input);
        var type = TypeUser.valueOf(input.getTypeUser().name());

        accessCardAggregator.includeAccessCard(entity, input.getEmail(), pass, pass);
        rolesAggregator.includeRoles(entity.getAccessCard(), type, input.getRoles());
        avatarAggregator.includeDefaultAvatarUrl(entity);

        return entity;
    }
}
