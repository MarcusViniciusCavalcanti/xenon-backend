package br.edu.utfpr.tsi.xenon.application.service;

import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.CAR_UNABLE_AUTHORIZED;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.CAR_UNABLE_UNAUTHORIZED;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.KNOWN;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import br.edu.utfpr.tsi.xenon.application.dto.CarDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputNewCarDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputRemoveCarDto;
import br.edu.utfpr.tsi.xenon.domain.security.service.SecurityContextUserService;
import br.edu.utfpr.tsi.xenon.domain.user.aggregator.CarsAggregator;
import br.edu.utfpr.tsi.xenon.domain.user.aggregator.ChangeStateCar;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.structure.exception.BusinessException;
import br.edu.utfpr.tsi.xenon.structure.exception.ResourceNotFoundException;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarApplicationService {

    private final SecurityContextUserService securityContextUserService;
    private final CarRepository carRepository;
    private final CarsAggregator carsAggregator;
    private final ChangeStateCar changeStateCar;

    @Transactional
    public CarDto includeNewCar(InputNewCarDto input, String authorization) {
        return securityContextUserService.getUserByContextSecurity(authorization)
            .map(userEntity -> {
                carsAggregator.includeNewCar(userEntity, input.getModel(), input.getPlate());
                return userEntity.lastCar();
            })
            .map(carRepository::saveAndFlush)
            .map(this::getCarDto)
            .orElse(new CarDto());
    }

    @Transactional
    public void includeDocument(Long carId, MultipartFile file, String authorization) {
        log.info("Iniciando processo para incluir documento do carro");
        securityContextUserService.getUserByContextSecurity(authorization)
            .map(UserEntity::getCar)
            .flatMap(carEntities -> carEntities.stream()
                .filter(carEntity -> carEntity.getId().equals(carId))
                .findFirst())
            .map(carEntity -> {
                var document = extracted(file, carEntity);
                carsAggregator.includeDocumentToCar(carEntity, document);
                return carEntity;
            })
            .ifPresent(carRepository::saveAndFlush);
    }

    @Transactional
    public void removeCar(InputRemoveCarDto input, String authorization) {
        securityContextUserService.getUserByContextSecurity(authorization)
            .ifPresent(userEntity -> {
                var car = getCarInUser(userEntity, input.getPlate());
                userEntity.getCar().remove(car);
                carRepository.deleteByUserAndPlate(userEntity, car.getPlate());
            });
    }

    @Transactional
    public void authorisedCar(Long id) {
        try {
            var car = getById(id);
            car.setAuthorisedAccess(Boolean.TRUE);
            carRepository.saveAndFlush(changeStateCar.executeProcess(car));
        } catch (IllegalStateException ex) {
            log.error("Error para desautorizar carro {}", ex.getMessage());
            throw new BusinessException(
                UNPROCESSABLE_ENTITY.value(),
                CAR_UNABLE_AUTHORIZED.getCode()
            );
        }
    }

    @Transactional
    public void unauthorisedCar(Long id) {
        try {
            var car = getById(id);
            car.setAuthorisedAccess(Boolean.FALSE);
            carRepository.saveAndFlush(changeStateCar.executeProcess(car));
        } catch (IllegalStateException ex) {
            log.error("Error para desautorizar carro {}", ex.getMessage());
            throw new BusinessException(
                UNPROCESSABLE_ENTITY.value(),
                CAR_UNABLE_UNAUTHORIZED.getCode()
            );
        }

    }

    private File extracted(MultipartFile file, CarEntity carEntity) {
        try {
            log.info("Montando arquivo da requisição");
            var document = Files.createTempFile("%s".formatted(carEntity.getId()), ".pdf");
            log.debug("Transferindo dados do arquivo recebido para o temporário");
            file.transferTo(document);
            return document.toFile();
        } catch (IOException e) {
            log.error("Error ao recuperar o arquivo da requisição");
            throw new BusinessException(422, KNOWN.getCode());
        }
    }

    private CarEntity getById(Long id) {
        return carRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Carro", "id"));
    }

    private CarEntity getCarInUser(UserEntity userEntity, String plate) {
        return userEntity.getCar().stream()
            .filter(carEntity -> {
                var plateNormalize = carsAggregator.normalizePlate(plate);
                return carEntity.getPlate().equals(carsAggregator.formatterPlate(plateNormalize));
            })
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
