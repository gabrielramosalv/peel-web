FROM maven:3.9.16-eclipse-temurin-17-alpine AS build

WORKDIR /workspace

COPY pom.xml ./
COPY peel-web-core/pom.xml peel-web-core/pom.xml
COPY peel-web-demo/pom.xml peel-web-demo/pom.xml

RUN mvn -B -pl peel-web-demo -am dependency:go-offline

COPY peel-web-core/src peel-web-core/src
COPY peel-web-demo/src peel-web-demo/src

RUN mvn -B -pl peel-web-demo -am clean package -Dmaven.test.skip=true

FROM eclipse-temurin:17.0.20_8-jre-alpine-3.24

RUN addgroup -S peel && adduser -S peel -G peel

WORKDIR /app

COPY --from=build --chown=peel:peel /workspace/peel-web-demo/target/peel-web-demo.jar /app/peel-web-demo.jar

USER peel

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/peel-web-demo.jar"]
