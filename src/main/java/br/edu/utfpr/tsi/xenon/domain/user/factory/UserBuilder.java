package br.edu.utfpr.tsi.xenon.domain.user.factory;


import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import java.util.function.Supplier;

public interface UserBuilder {

    void add(TypeUser name, Supplier<UserEntity> supplier);
}
