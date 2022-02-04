package br.edu.utfpr.tsi.xenon.domain.user.aggregator;

import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.FILE_ALLOWED;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.KNOWN;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.LIMIT_EXCEEDED_CAR;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.PLATE_ALREADY;
import static br.edu.utfpr.tsi.xenon.structure.MessagesMapper.PLATE_INVALID;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import br.edu.utfpr.tsi.xenon.application.config.property.FilesProperty;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.service.ValidatorFile;
import br.edu.utfpr.tsi.xenon.structure.exception.BusinessException;
import br.edu.utfpr.tsi.xenon.structure.exception.PlateException;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import com.cloudinary.Cloudinary;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CarsAggregator {

    private static final Pattern REGEX_PLATE =
        Pattern.compile("^[a-zA-Z]{3}-?[0-9][a-zA-Z][0-9]{2}$|^[a-zA-Z]{3}-?[0-9]{4}$");

    private static final ExampleMatcher EXIST_CAR_PLATE_MATCHER;

    static {
        EXIST_CAR_PLATE_MATCHER = ExampleMatcher.matching()
            .withIgnorePaths(
                "id",
                "document",
                "lastAccess",
                "model",
                "createdAt",
                "updatedAt",
                "user"
            );
    }

    private final CarRepository carRepository;
    private final ValidatorFile validatorFile;
    private final FilesProperty filesProperty;
    private final Cloudinary cloudinary;
    private final ChangeStateCar changeStateCar;

    @Transactional(propagation = Propagation.MANDATORY)
    public void includeNewCar(UserEntity user, String modelCar, String plateCar) {
        checkLimitCars(plateCar, user);

        if (StringUtils.isBlank(modelCar)) {
            log.info("carro não cadatrado [modelo não informado]");
            return;
        }

        if (StringUtils.isBlank(plateCar)) {
            log.info("carro não cadatrado [placa não informado]");
            return;
        }

        var value = plateCar.trim()
            .replace(" ", "")
            .replace("-", "")
            .replace("_", "");

        if (REGEX_PLATE.asPredicate().test(value)) {
            var plateFormatted = formatterPlate(value);

            existCarByPlate(plateFormatted);

            var car = new CarEntity();
            car.setModel(modelCar);
            car.setPlate(plateFormatted);
            car.setNumberAccess(0);
            car.setUser(user);
            car.setAuthorisedAccess(FALSE);

            changeStateCar.executeProcess(car);
            user.includeLastCar(car);
        } else {
            throw new PlateException(plateCar, PLATE_INVALID.getCode());
        }
    }

    public String normalizePlate(String value) {
        return value.trim()
            .replace(" ", "")
            .replace("-", "")
            .replace("_", "");
    }

    public String formatterPlate(String value) {
        var partOne = value.toLowerCase(Locale.ROOT).substring(0, 3);
        var partTow = value.toLowerCase(Locale.ROOT).substring(3, 7);
        return "%s-%s".formatted(partOne, partTow).toUpperCase(Locale.ROOT);
    }

    private void existCarByPlate(String plate) {
        log.info("vericando se placa existe: {}", plate);
        var probe = new CarEntity();
        probe.setPlate(plate);
        var example = Example.of(probe, EXIST_CAR_PLATE_MATCHER);

        if (TRUE.equals(carRepository.exists(example))) {
            throw new PlateException(plate, PLATE_ALREADY.getCode());
        }
    }

    private void checkLimitCars(String plate, UserEntity userEntity) {
        if (userEntity.getCar().size() > 5) {
            throw new BusinessException(422, LIMIT_EXCEEDED_CAR.getCode(), plate);
        }
    }

    public void includeDocumentToCar(CarEntity carEntity, File document) {
        try {
            log.info("Validando arquivo");
            if (FALSE.equals(validatorFile.validateDocumentFile(document))) {
                throw new BusinessException(400, FILE_ALLOWED.getCode(), "pdf");
            }

            var options = Map.of(
                "resource_type", "auto",
                "public_id", "%d".formatted(carEntity.getId()),
                "folder", filesProperty.getDocUrl(),
                "filename_override", TRUE,
                "use_filename", TRUE,
                "overwrite", TRUE,
                "type", "private"
            );

            log.info("Salvando arquivo");
            log.debug("Enviando arquivo para store com os parâmentros: {}", options);
            var publicId = (String)
                cloudinary.uploader().upload(document, options).get("public_id");

            var isRecoupled = StringUtils.isBlank(carEntity.getDocument());
            carEntity.setDocument(publicId);
            if (FALSE.equals(isRecoupled)) {
                log.info("Reupando documento");
                return;
            }

            changeStateCar.executeProcess(carEntity);
        } catch (IOException e) {
            log.error("Error enviar arquivo para servidor de arquivo");
            throw new BusinessException(422, KNOWN.getCode());
        }

    }
}
