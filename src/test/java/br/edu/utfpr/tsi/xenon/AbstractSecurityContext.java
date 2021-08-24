package br.edu.utfpr.tsi.xenon;
//
//import static io.restassured.RestAssured.given;
//
//import br.edu.utfpr.tsi.xenon.application.config.property.SecurityProperty;
//import br.edu.utfpr.tsi.xenon.application.dto.InputLoginDto;
//import br.edu.utfpr.tsi.xenon.domain.user.entity.AccessCardEntity;
//import br.edu.utfpr.tsi.xenon.domain.user.entity.UserEntity;
//import br.edu.utfpr.tsi.xenon.domain.user.factory.TypeUser;
//import br.edu.utfpr.tsi.xenon.structure.repository.RoleRepository;
//import br.edu.utfpr.tsi.xenon.structure.repository.UserRepository;
//import io.restassured.builder.RequestSpecBuilder;
//import io.restassured.specification.RequestSpecification;
//import java.util.List;
//import org.junit.jupiter.api.parallel.ResourceAccessMode;
//import org.junit.jupiter.api.parallel.ResourceLock;
//import org.junit.jupiter.api.parallel.ResourceLocks;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//
//@ResourceLocks(value = {
//    @ResourceLock(value = "br.edu.utfpr.tsi.xenon.structure.repository.UserRepository", mode = ResourceAccessMode.READ),
//    @ResourceLock(value = "br.edu.utfpr.tsi.xenon.structure.repository.RoleRepository")
//})
//public abstract class AbstractSecurityContext extends AbstractContextTest {
//
//    protected static final String PASS = "12345678";
//    private static final String URI_LOGIN = "/api/login";
//    protected RequestSpecification specAuthentication;
//    protected String token;
//    @Autowired
//    private RoleRepository roleRepository;
//    @Autowired
//    private UserRepository userRepository;
//    @Autowired
//    private SecurityProperty securityProperty;
//
//    protected UserEntity createAdmin() {
//        return getInputLoginDto(List.of(1L, 2L, 3L));
//    }
//
//    protected UserEntity createOperator() {
//        return getInputLoginDto(List.of(1L, 2L));
//    }
//
//    protected UserEntity createDriver() {
//        return getInputLoginDto(List.of(1L));
//    }
//
//    protected void setAuthentication(InputLoginDto input) {
//        token = given()
//            .basePath(URI_LOGIN)
//            .port(port)
//            .contentType("application/json")
//            .body(input)
//            .when()
//            .post()
//            .then()
//            .statusCode(HttpStatus.OK.value())
//            .extract()
//            .header(securityProperty.getHeader().getName());
//
//        specAuthentication = new RequestSpecBuilder()
//            .setPort(port)
//            .addHeader(securityProperty.getHeader().getName(), token)
//            .build();
//    }
//
//    private UserEntity getInputLoginDto(List<Long> ids) {
//        var email = faker.internet().emailAddress();
//        var user = new UserEntity();
//        user.setName(faker.name().fullName());
//        user.setTypeUser(TypeUser.SERVICE.name());
//
//        var accessCard = new AccessCardEntity();
//        accessCard.setPassword(new BCryptPasswordEncoder().encode(PASS));
//        accessCard.setUsername(email);
//
//        var roles = roleRepository.findAllById(ids);
//        accessCard.setRoleEntities(roles);
//        accessCard.setEnabled(Boolean.TRUE);
//        accessCard.setUser(user);
//
//        user.setAccessCard(accessCard);
//        return userRepository.saveAndFlush(user);
//    }
//}
