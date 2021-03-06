package br.edu.utfpr.tsi.xenon.application.service;

import static br.edu.utfpr.tsi.xenon.domain.user.state.CarState.CarStateName.WAITING_DECISION;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.CAR_UNABLE_AUTHORIZED;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.CAR_UNABLE_UNAUTHORIZED;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.KNOWN;
import static java.lang.Boolean.TRUE;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import br.edu.utfpr.tsi.xenon.application.dto.CarDto;
import br.edu.utfpr.tsi.xenon.application.dto.DocumentUriDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputNewCarDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputRemoveCarDto;
import br.edu.utfpr.tsi.xenon.application.dto.PageCarWaitingDecisionDto;
import br.edu.utfpr.tsi.xenon.application.dto.UserCarsSummaryDto;
import br.edu.utfpr.tsi.xenon.domain.security.service.SecurityContextUserService;
import br.edu.utfpr.tsi.xenon.domain.user.aggregator.CarsAggregator;
import br.edu.utfpr.tsi.xenon.domain.user.aggregator.ChangeStateCar;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.structure.exception.BusinessException;
import br.edu.utfpr.tsi.xenon.structure.exception.ResourceNotFoundException;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import com.cloudinary.Cloudinary;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private final Cloudinary cloudinary;

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
    public void unauthorisedCar(Long id, String reason) {
        try {
            var car = getById(id);
            car.setAuthorisedAccess(Boolean.FALSE);
            car.setReasonBlock(reason);
            carRepository.saveAndFlush(changeStateCar.executeProcess(car));
        } catch (IllegalStateException ex) {
            log.error("Error para desautorizar carro {}", ex.getMessage());
            throw new BusinessException(
                UNPROCESSABLE_ENTITY.value(),
                CAR_UNABLE_UNAUTHORIZED.getCode()
            );
        }

    }

    public List<CarDto> getAllCarsByUser(Long userId) {
        return carRepository.findByUserId(userId).stream()
            .map(this::getCarDto)
            .toList();
    }

    public DocumentUriDto downloadDocument(Long carId) {
        return carRepository.findById(carId)
            .map(carEntity -> {
                var expire =
                    LocalDateTime.now().plusMinutes(5).toInstant(ZoneOffset.UTC).toEpochMilli();
                var options = Map.<String, Object>of(
                    "expires_at", expire,
                    "attachment", TRUE
                );

                try {
                    var url = cloudinary.privateDownload(carEntity.getDocument(), "pdf", options);
                    return new DocumentUriDto()
                        .ttl(5)
                        .uri(url);
                } catch (Exception e) {
                    throw new ResourceNotFoundException("documento", "id do carro");
                }
            })
            .orElseThrow(() -> new ResourceNotFoundException("carro", "id"));
    }

    @Cacheable(cacheNames = "UserCarSummary")
    public UserCarsSummaryDto getUserCarsSummary() {
        var summary = carRepository.getCarsSummary();
        return new UserCarsSummaryDto()
            .approved(summary.getApproved())
            .waiting(summary.getWaiting())
            .reproved(summary.getReproved())
            .block(summary.getBlock());
    }

    public PageCarWaitingDecisionDto pageCarWaitingDecision(Integer size, Integer page) {
        var sort = Sort.by(DESC, "createdAt");
        var pageRequest = PageRequest.of(page, size, sort);

        var pageable = carRepository.findAllByState(WAITING_DECISION.name(), pageRequest);
        var content = pageable.getContent()
            .stream()
            .map(this::getCarDto)
            .toList();

        var pageCarWaitingDecision = new PageCarWaitingDecisionDto().items(content);
        pageCarWaitingDecision.sorted("createdAt");
        pageCarWaitingDecision.setTotalPage(pageable.getTotalPages());
        pageCarWaitingDecision.page(pageable.getNumber());
        pageCarWaitingDecision.size(pageable.getSize());
        pageCarWaitingDecision.totalElements(pageable.getTotalElements());
        pageCarWaitingDecision.direction(DESC.name());

        return pageCarWaitingDecision;
    }

    private File extracted(MultipartFile file, CarEntity carEntity) {
        try {
            log.info("Montando arquivo da requisi????o");
            var document = Files.createTempFile("%s".formatted(carEntity.getId()), ".pdf");
            log.debug("Transferindo dados do arquivo recebido para o tempor??rio");
            file.transferTo(document);
            return document.toFile();
        } catch (IOException e) {
            log.error("Error ao recuperar o arquivo da requisi????o");
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
            .numberAccess(carEntity.getNumberAccess())
            .authorisedAccess(carEntity.getAuthorisedAccess())
            .status(carEntity.getCarStatus().name())
            .reasonLock(carEntity.getReasonBlock())
            .id(carEntity.getId())
            .document(carEntity.getDocument());
    }
}
