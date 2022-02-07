package br.edu.utfpr.tsi.xenon.domain.user.service;

import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import br.edu.utfpr.tsi.xenon.AbstractContextTest;
import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.domain.security.entity.RoleEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.factory.TypeUser;
import br.edu.utfpr.tsi.xenon.structure.ParamsQuerySearchUserDto;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.RoleRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.UserRepository;
import com.github.javafaker.Faker;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("Teste - Unidade - GetterAllUserSpec")
@ResourceLock(value = "br.edu.utfpr.tsi.xenon.structure.repository.UserRepository")
class GetterAllUserSpecTest extends AbstractContextTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setupDatabase() {
        deleteAll();

        var faker = Faker.instance();
        var roles = roleRepository.findAll();

        var speakers = IntStream.range(0, 5).boxed()
            .map(index -> getUserEntity(faker, roles, TypeUser.SPEAKER))
            .collect(Collectors.toList());

        var students = IntStream.range(0, 5).boxed()
            .map(index -> getUserEntity(faker, roles, TypeUser.STUDENTS))
            .collect(Collectors.toList());

        var service = IntStream.range(0, 5).boxed()
            .map(index -> getUserEntity(faker, roles, TypeUser.SERVICE))
            .collect(Collectors.toList());

        userRepository.saveAllAndFlush(speakers);
        userRepository.saveAllAndFlush(students);
        userRepository.saveAllAndFlush(service);
    }

    @AfterEach
    void clearDatabase() {
        deleteAll();
    }

    @Test
    @DisplayName("Deve retornar uma página de usuários apenas palestrantes")
    void shouldReturnPageUserOnlySpeaker() {
        assertions("SPEAKER");
    }

    @Test
    @DisplayName("Deve retornar uma página de usuários apenas estudantes")
    void shouldReturnPageUserOnlyStudents() {
        assertions("STUDENTS");
    }

    @Test
    @DisplayName("Deve retornar uma página de usuários apenas serviço")
    void shouldReturnPageUserOnlyService() {
        assertions("SERVICE");
    }

    @Test
    @DisplayName("Deve retornar uma página de usuários apenas com email especificado")
    void shouldReturnUserEmail() {
        var user = new UserEntity();
        user.setName(faker.name().fullName());
        user.setAuthorisedAccess(TRUE);
        user.setAvatar("url");
        user.setTypeUser(TypeUser.SERVICE.name());

        var accessCard = new AccessCardEntity();
        accessCard.setUser(user);
        accessCard.setEnabled(TRUE);
        accessCard.setUsername(faker.internet().emailAddress());
        accessCard.setPassword("pass");
        accessCard.setRoleEntities(roleRepository.findAll());

        user.setAccessCard(accessCard);

        userRepository.saveAndFlush(user);

        var spec = new GetterAllUserSpec();
        var input = ParamsQuerySearchUserDto.builder()
            .nameOrEmail(user.getAccessCard().getUsername())
            .build();

        var page = PageRequest.of(0, 10);
        var specification = spec.filterBy(input);

        var result = userRepository.findAll(specification, page);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getTotalElements());
    }

    private UserEntity getUserEntity(Faker faker, List<RoleEntity> roles, TypeUser speaker) {
        var user = new UserEntity();
        user.setName(faker.name().fullName());
        user.setAuthorisedAccess(TRUE);
        user.setAvatar("url");
        user.setTypeUser(speaker.name());

        var accessCard = new AccessCardEntity();
        accessCard.setUser(user);
        accessCard.setEnabled(TRUE);
        accessCard.setUsername(faker.internet().emailAddress());
        accessCard.setPassword("pass");
        accessCard.setRoleEntities(roles);

        user.setAccessCard(accessCard);

        return user;
    }

    private void assertions(String type) {
        var spec = new GetterAllUserSpec();
        var input = ParamsQuerySearchUserDto.builder()
            .type(type)
            .build();

        var page = PageRequest.of(0, 10);
        var specification = spec.filterBy(input);

        var result = userRepository.findAll(specification, page);
        assertEquals(5, result.getTotalElements());
        var count = result.getContent()
            .stream()
            .filter(entity -> entity.getTypeUser().equals(type))
            .count();

        assertEquals(result.getContent().size(), count);
        assertEquals(1, result.getTotalPages(), 1);
        assertEquals(5, result.getTotalElements());
    }

    private void deleteAll() {
        carRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        userRepository.flush();
    }
}
