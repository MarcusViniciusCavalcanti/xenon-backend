package br.edu.utfpr.tsi.xenon.application.controller;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpStatus.OK;

import br.edu.utfpr.tsi.xenon.AbstractSecurityContext;
import br.edu.utfpr.tsi.xenon.application.dto.InputLoginDto;
import br.edu.utfpr.tsi.xenon.application.dto.InputWorkstationDto.ModeEnum;
import br.edu.utfpr.tsi.xenon.domain.recognize.entity.RecognizeEntity;
import br.edu.utfpr.tsi.xenon.domain.security.entity.AccessCardEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarEntity;
import br.edu.utfpr.tsi.xenon.domain.user.entity.CarStatus;
import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
import br.edu.utfpr.tsi.xenon.domain.user.factory.TypeUser;
import br.edu.utfpr.tsi.xenon.domain.user.state.CarState.CarStateName;
import br.edu.utfpr.tsi.xenon.domain.workstations.entity.WorkstationEntity;
import br.edu.utfpr.tsi.xenon.structure.repository.CarRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.RecognizerRepository;
import br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.ResourceLocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;

@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("Test - Integration - Funcionalidade de Estado dos dados da aplicação")
class StateApplicationEndpointTest extends AbstractSecurityContext {

    @Autowired
    private WorkstationRepository workstationRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private RecognizerRepository recognizerRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clear() {
        userRepository.deleteAll();

        for(String name:cacheManager.getCacheNames()){
            Objects.requireNonNull(cacheManager.getCache(name)).clear();            // clear cache by name
        }
    }

    @Test
    @ResourceLocks(value = {
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.WorkstationRepository")
    })
    @DisplayName("Deve retornar um sumário das estações de trabalho")
    void shouldReturnSummaryWorkstation() {
        var user = createAdmin();

        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        workstationRepository.deleteAll();

        var workstation = new WorkstationEntity();
        workstation.setMode(ModeEnum.NONE.name());
        workstation.setName(faker.name().name());
        workstation.setIp(faker.internet().ipV4Address());
        workstation.setPort(9090);
        workstation.setKey("key");

        workstationRepository.saveAndFlush(workstation);

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .contentType(JSON)
            .expect()
            .statusCode(OK.value())
            .body("[0].id", is(workstation.getId().intValue()))
            .body("[0].workstationName", is(workstation.getName()))
            .body("[0].amountAccess", is(0))
            .when()
            .get("/api/system/workstations-and-recognizer");

        deleteUser(user);
        workstationRepository.delete(workstation);
    }

    @Test
    @DisplayName("Deve retornar um sumário de usuários cadastrados")
    @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository")
    void shouldReturnSummaryUsers() {
        userRepository.deleteAll();
        var user = createAdmin();

        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var speeakers =
            IntStream.range(0, 10).mapToObj(index -> buildUser(TypeUser.SPEAKER)).toList();

        var students =
            IntStream.range(0, 7).mapToObj(index -> buildUser(TypeUser.STUDENTS)).toList();

        userRepository.saveAllAndFlush(speeakers);
        userRepository.saveAllAndFlush(students);

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .contentType(JSON)
            .expect()
            .statusCode(OK.value())
            .body("services", notNullValue())
            .body("speakers", is(speeakers.size()))
            .body("students", is(students.size()))
            .when()
            .get("/api/system/registry-users");

        deleteUser(user);
        userRepository.deleteAll(speeakers);
        userRepository.deleteAll(students);
    }

    @Test
    @DisplayName("Deve retornar um sumário dos carros cadastrados no sistema")
    @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.CarRepository")
    @ResourceLocks(value = {
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.CarRepository")
    })
    void shouldReturnSummaryCars() {
        carRepository.deleteAll();

        var user = createAdmin();

        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var carsApproved =
            IntStream.range(0, 10).mapToObj(index -> buildCar(user, CarStatus.APPROVED)).toList();

        var carsWaiting =
            IntStream.range(0, 15).mapToObj(index -> buildCar(user, CarStatus.WAITING)).toList();

        var carsReproved =
            IntStream.range(0, 5).mapToObj(index -> buildCar(user, CarStatus.REPROVED)).toList();

        var carsBlock =
            IntStream.range(0, 25).mapToObj(index -> buildCar(user, CarStatus.BLOCK)).toList();

        carRepository.saveAllAndFlush(carsWaiting);
        carRepository.saveAllAndFlush(carsBlock);
        carRepository.saveAllAndFlush(carsReproved);
        carRepository.saveAllAndFlush(carsApproved);

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .contentType(JSON)
            .expect()
            .statusCode(OK.value())
            .body("waiting", is(carsWaiting.size()))
            .body("approved", is(carsApproved.size() + 1))
            .body("reproved", is(carsReproved.size()))
            .body("block", is(carsBlock.size()))
            .when()
            .get("/api/system/registry-cars");

        deleteUser(user);
        carRepository.deleteAll(carsWaiting);
        carRepository.deleteAll(carsBlock);
        carRepository.deleteAll(carsReproved);
        carRepository.deleteAll(carsApproved);
    }

    @Test
    @DisplayName("Deve retornar sumário de reconhecimentos")
    @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.RecognizerRepository")
    @ResourceLocks(value = {
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.UserRepository"),
        @ResourceLock("br.edu.utfpr.tsi.xenon.structure.repository.RecognizerRepository")
    })
    void shouldReturnSummaryRecognizer() {
        var user = createAdmin();

        var input = new InputLoginDto()
            .password(PASS)
            .email(user.getAccessCard().getUsername());

        setAuthentication(input);

        var workstation = new WorkstationEntity();
        workstation.setMode(ModeEnum.NONE.name());
        workstation.setName(faker.name().name());
        workstation.setIp(faker.internet().ipV4Address());
        workstation.setPort(9090);
        workstation.setKey("key");

        workstationRepository.saveAndFlush(workstation);

        recognizerRepository.deleteAll();

        var recognizerExpected = new RecognizeEntity();
        recognizerExpected.setConfidence(99.9F);
        recognizerExpected.setEpochTime(LocalDateTime.now());
        recognizerExpected.setPlate("plate");
        recognizerExpected.setOriginIp("ip");
        recognizerExpected.setDriverName("driveName");
        recognizerExpected.setHasError(TRUE);

        recognizerRepository.saveAndFlush(recognizerExpected);

        given(specAuthentication)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .contentType(JSON)
            .expect()
            .statusCode(OK.value())
            .body("seventhDayBefore", is(0))
            .body("sixthDayBefore", is(0))
            .body("fifthDayBefore", is(0))
            .body("fourthDayBefore", is(0))
            .body("thirdhDayBefore", is(0))
            .body("secondDayBefore", is(0))
            .body("firstDayBefore", is(0))
            .body("now", is(1))
            .when()
            .get("/api/system/recognizers-week");

        deleteUser(user);
        recognizerRepository.delete(recognizerExpected);
    }

    private UserEntity buildUser(TypeUser typeUser) {
        var email = faker.internet().emailAddress();
        var user = new UserEntity();
        user.setName(faker.name().fullName());
        user.setTypeUser(typeUser.name());

        var accessCard = new AccessCardEntity();
        accessCard.setPassword(PASS);
        accessCard.setUsername(email);

        var roles = roleRepository.findAllById(List.of(1L));
        accessCard.setRoleEntities(roles);
        accessCard.setEnabled(TRUE);
        accessCard.setUser(user);
        user.setAccessCard(accessCard);

        return user;
    }

    private CarEntity buildCar(UserEntity user, CarStatus carStatus) {
        var car = new CarEntity();
        car.setPlate(faker.bothify("???-####", TRUE));
        car.setModel("Gol 1.0");
        user.getCar().add(car);
        car.setUser(user);
        car.setCarStatus(carStatus);
        car.setState(CarStateName.APPROVED.name());

        return car;
    }
}
