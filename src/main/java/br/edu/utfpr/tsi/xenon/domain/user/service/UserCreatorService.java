package br.edu.utfpr.tsi.xenon.domain.user.service;

import br.edu.utfpr.tsi.xenon.application.dto.InputRegistryStudentDto;
import br.edu.utfpr.tsi.xenon.domain.user.aggregator.AccessCardAggregator;
import br.edu.utfpr.tsi.xenon.domain.user.aggregator.AvatarAggregator;
import br.edu.utfpr.tsi.xenon.domain.user.aggregator.CarsAggregator;
import br.edu.utfpr.tsi.xenon.domain.user.aggregator.RolesAggregator;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.factory.TypeUser;
import br.edu.utfpr.tsi.xenon.domain.user.factory.UserFactory;
import br.edu.utfpr.tsi.xenon.structure.repository.UserRepository;
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

    private final UserRepository userRepository;
    private final AccessCardAggregator accessCardAggregator;
    private final RolesAggregator rolesAggregator;
    private final CarsAggregator carsAggregator;
    private final AvatarAggregator avatarAggregator;

    @Transactional(propagation = Propagation.MANDATORY)
    public UserEntity registryNewStudent(InputRegistryStudentDto input) {
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
        carsAggregator.includeNewCar(entity, input.getModelCar(), input.getPlateCar());
        avatarAggregator.includeDefaultAvatarUrl(entity);

        return entity;
    }

//    @Transactional(propagation = Propagation.MANDATORY)
//    public UserDto createNewUser(InputUserDto input, String pass) {
//        log.info("Executando cadastro de usuário");
//        var userFactory = UserFactory.getInstance();
//        var entity = userFactory.create(input);
//        var type = TypeUser.valueOf(input.getTypeUser().name());
//
//        accessCardAggregator.includeAccessCard(entity, input.getEmail(), pass, pass);
//        rolesAggregator.includeRoles(entity.getAccessCard(), type, input.getRoles());
//        avatarAggregator.includeDefaultAvatarUrl(entity);
//        entity = userRepository.saveAndFlush(entity);
//
//        return userFactory.buildUserDto(entity);
//    }
}
