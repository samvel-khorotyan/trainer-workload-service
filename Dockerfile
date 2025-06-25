# trainer-workload-service/Dockerfile
# ---------- Build Stage ----------
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

COPY src src

RUN ./mvnw clean package spring-boot:repackage -DskipTests -Dmaven.test.skip=true

# ---------- Runtime Stage ----------
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# AWS SDK requires CA certificates
RUN apk add --no-cache ca-certificates curl

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# Environment Variables
ENV AWS_REGION=eu-north-1
ENV AWS_SQS_TRAINER_WORKLOAD_QUEUE=trainer-workload-queue
ENV AWS_SQS_TRAINER_WORKLOAD_RESPONSE_QUEUE=trainer-workload-response-queue
ENV AWS_SQS_DEAD_LETTER_QUEUE=dead-letter-queue

EXPOSE 8081

ENTRYPOINT ["java", \
  "-Dspring.main.allow-bean-definition-overriding=true", \
  "-Daws.region=${AWS_REGION}", \
  "-Daws.sqs.trainer-workload-queue=${AWS_SQS_TRAINER_WORKLOAD_QUEUE}", \
  "-Daws.sqs.trainer-workload-response-queue=${AWS_SQS_TRAINER_WORKLOAD_RESPONSE_QUEUE}", \
  "-Daws.sqs.dead-letter-queue=${AWS_SQS_DEAD_LETTER_QUEUE}", \
  "-jar", "app.jar"]
  
