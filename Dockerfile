FROM maven:3.6-openjdk-17-slim as api
WORKDIR /usr/src/api
COPY pom.xml .
RUN mvn -B -f pom.xml -s /usr/share/maven/ref/settings-docker.xml dependency:resolve
COPY . .
RUN mvn -B -s /usr/share/maven/ref/settings-docker.xml package -DskipTests

FROM openjdk:17
RUN useradd -ms /bin/bash application
WORKDIR /backend

RUN chown application:application /backend
COPY --from=api /usr/src/api/target/xenon-api.jar .

ENTRYPOINT ["java", "-jar", "/backend/xenon-api.jar"]
CMD ["--spring.profiles.active=docker"]
