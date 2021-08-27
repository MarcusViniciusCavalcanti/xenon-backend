package br.edu.utfpr.tsi.xenon.application.service;

import br.edu.utfpr.tsi.xenon.application.dto.CarDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputNewCarDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputRemoveCarDto;
import br.edu.utfpr.tsi.xenon.domain.security.service.SecurityContextUserService;
import br.edu.utfpr.tsi.xenon.domain.user.aggregator.CarsAggregator;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.structure.exception.ResourceNotFoundException;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarApplicationService {

    private final SecurityContextUserService securityContextUserService;
    private final CarRepository carRepository;
    private final CarsAggregator carsAggregator;

    @Transactional
    public CarDto includeNewCar(InputNewCarDto input, String authorization) {
        return securityContextUserService.getUserByContextSecurity(authorization)
            .map(userEntity -> {
                carsAggregator.includeNewCar(userEntity, input.getModel(), input.getPlate());
                return getCarInUser(userEntity, input.getPlate());
            })
            .map(carRepository::saveAndFlush)
            .map(this::getCarDto)
            .orElse(new CarDto());
    }

    @Transactional
    public void removeCar(InputRemoveCarDto input, String authorization) {
        securityContextUserService.getUserByContextSecurity(authorization)
            .map(userEntity -> getCarInUser(userEntity, input.getPlate()))
            .ifPresent(carRepository::delete);
    }

    private CarEntity getCarInUser(UserEntity userEntity, String plate) {
        return userEntity.getCar().stream()
            .filter(carEntity -> carEntity.getPlate().equals(plate))
            .findFirst()
            .orElse(new CarEntity());
    }

    private CarDto getCarDto(CarEntity carEntity) {
        return new CarDto()
            .plateCar(carEntity.getPlate())
            .modelCar(carEntity.getModel())
            .lastAcess(carEntity.getLastAccess())
            .document(carEntity.getDocument())
            .numberAccess(carEntity.getNumberAccess())
            .id(carEntity.getId());
    }
}
