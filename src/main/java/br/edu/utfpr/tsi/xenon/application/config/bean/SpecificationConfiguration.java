package br.edu.utfpr.tsi.xenon.application.config.bean;

import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.service.GetterAllUserSpec;
import br.edu.utfpr.tsi.xenon.structure.ParamsQuerySearchUserDto;
import br.edu.utfpr.tsi.xenon.structure.repository.BasicSpecification;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpecificationConfiguration {

    public static final String QUALIFIER_GET_ALL_SPEC = "QUALIFIER_GET_ALL_SPEC";

    @Bean(QUALIFIER_GET_ALL_SPEC)
    public BasicSpecification<UserEntity, ParamsQuerySearchUserDto> getterAllUserSpec() {
        return new GetterAllUserSpec();
    }
}
