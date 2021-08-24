package br.edu.utfpr.tsi.xenon.domain.user.factory;

import br.edu.utfpr.tsi.xenon.application.dto.CarDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputRegistryStudentDto;
import br.edu.utfpr.tsi.xenon.application.dto.RoleDto;
import br.edu.utfpr.tsi.xenon.application.dto.UserDto;
import br.edu.utfpr.tsi.xenon.application.dto.UserDto.TypeEnum;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import java.util.EnumMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface UserFactory {

    UserEntity create(InputRegistryStudentDto input);
//    UserEntity create(InputUserDto input);

    default UserDto buildUserDto(UserEntity entity) {
        var user = new UserDto()
            .id(entity.getId())
            .name(entity.getName())
            .email(entity.getAccessCard().getUsername())
            .avatar(entity.getAvatar())
            .type(TypeEnum.fromValue(entity.getTypeUser()));

        entity.getAccessCard().getRoleEntities().stream()
            .map(role -> new RoleDto()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription()))
            .forEach(user::addRolesItem);

        entity.getCar().stream()
            .map(car -> new CarDto()
                .id(car.getId())
                .plateCar(car.getPlate())
                .modelCar(car.getModel())
                .document(car.getDocument())
                .lastAcess(car.getLastAccess())
                .numberAccess(car.getNumberAccess()))
            .forEach(user::addCarsItem);

        return user;
    }

    static UserFactory getInstance() {
        return UserFactory.factory(userBuilder -> {
            userBuilder.add(TypeUser.STUDENTS, () -> {
                var user = new UserEntity();
                user.setTypeUser(TypeUser.STUDENTS.name());
                return user;
            });
            userBuilder.add(TypeUser.SERVICE, () -> {
                var user = new UserEntity();
                user.setTypeUser(TypeUser.SERVICE.name());
                return user;
            });
            userBuilder.add(TypeUser.SPEAKER, () -> {
                var user = new UserEntity();
                user.setTypeUser(TypeUser.SPEAKER.name());
                return user;
            });
        });
    }

    private static UserFactory factory(Consumer<UserBuilder> builder) {
        var map = new EnumMap<TypeUser, Supplier<UserEntity>>(TypeUser.class);
        builder.accept(map::put);

        return new UserFactory() {
            @Override
            public UserEntity create(InputRegistryStudentDto input) {
                var user = map.get(TypeUser.STUDENTS).get();
                user.setName(input.getName());
                return user;
            }

//            @Override
//            public UserEntity create(InputUserDto input) {
//                var type = TypeUser.valueOf(input.getTypeUser().name());
//                var user = map.get(type).get();
//                user.setTypeUser(type.name());
//                user.setName(input.getName());
//                user.setAuthorisedAccess(input.getAuthorisedAccess());
//                return user;
//            }
        };
    }
}
