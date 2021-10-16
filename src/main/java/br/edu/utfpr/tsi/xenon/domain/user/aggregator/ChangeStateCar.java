package br.edu.utfpr.tsi.xenon.domain.user.aggregator;

import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.domain.user.state.CarState;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChangeStateCar {
    private final BeanFactory beanFactory;

    public CarEntity executeProcess(CarEntity carEntity) {
        var actualState = carEntity.getState();

        if (StringUtils.isBlank(actualState)) {
            var initState = beanFactory.getBean("CREATE", CarState.class);
            initState.executeProcess(carEntity);
            return carEntity;
        }

        var state = beanFactory.getBean(actualState, CarState.class);
        state.executeProcess(carEntity);
        return carEntity;
    }
}
