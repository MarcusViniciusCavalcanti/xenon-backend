package br.edu.utfpr.tsi.xenon.application.config.bean;

import br.edu.utfpr.tsi.xenon.domain.recognize.entity.RecognizeEntity;
import br.edu.utfpr.tsi.xenon.domain.recognize.service.GetterAllRecognizeSpec;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.service.GetterAllUserSpec;
import br.edu.utfpr.tsi.xenon.structure.ParamsQuerySearchRecognizeDto;
import br.edu.utfpr.tsi.xenon.structure.ParamsQuerySearchUserDto;
import br.edu.utfpr.tsi.xenon.structure.repository.BasicSpecification;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpecificationConfiguration {

    public static final String QUALIFIER_GET_ALL_USER = "QUALIFIER_GET_ALL_USER";
    public static final String QUALIFIER_GET_ALL_RECOGNIZE = "QUALIFIER_GET_ALL_RECOGNIZE";

    @Bean(QUALIFIER_GET_ALL_USER)
    public BasicSpecification<UserEntity, ParamsQuerySearchUserDto> getterAllUserSpec() {
        return new GetterAllUserSpec();
    }

    @Bean(QUALIFIER_GET_ALL_RECOGNIZE)
    public BasicSpecification<RecognizeEntity, ParamsQuerySearchRecognizeDto> getterAllRecognize() {
        return new GetterAllRecognizeSpec();
    }
}
