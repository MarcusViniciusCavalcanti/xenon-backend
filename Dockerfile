FROM maven:3.6-openjdk-16-slim as api
WORKDIR /usr/src/api
COPY pom.xml .
RUN mvn -B -f pom.xml -s /usr/share/maven/ref/settings-docker.xml dependency:resolve
COPY . .
RUN mvn -B -s /usr/share/maven/ref/settings-docker.xml package -DskipTests

FROM openjdk:16
RUN useradd -ms /bin/bash application
WORKDIR /backend

RUN chown application:application /backend
COPY --from=api /usr/src/api/target/xenon-0.0.1.jar .

ENTRYPOINT ["java", "-jar", "/backend/xenon-0.0.1.jar"]
CMD ["--spring.profiles.active=docker"]