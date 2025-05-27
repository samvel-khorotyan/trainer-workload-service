# ---------- Build Stage ----------
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline -B

COPY src src

RUN ./mvnw clean package spring-boot:repackage -DskipTests

# ---------- Runtime Stage ----------
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Disabled integrations
ENV SPRING_DATA_MONGODB_URI=mongodb://localhost:27017/disabled_db
ENV SPRING_DATA_MONGODB_DATABASE=disabled_db
ENV SPRING_ACTIVEMQ_BROKER_URL=tcp://disabled:61616
ENV SPRING_ACTIVEMQ_USER=disabled
ENV SPRING_ACTIVEMQ_PASSWORD=disabled
ENV EUREKA_CLIENT_ENABLED=false

EXPOSE 8081

ENTRYPOINT ["java", \
  "-Dspring.main.allow-bean-definition-overriding=true", \
  "-jar", "app.jar"]
