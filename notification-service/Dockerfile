FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY gradlew build.gradle.kts settings.gradle.kts gradle.properties gradle.lockfile ./
COPY gradle ./gradle
COPY src/main ./src/main
RUN ./gradlew --no-daemon clean bootJar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /workspace/build/libs/*.jar /app/notification-service.jar
USER app
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "/app/notification-service.jar"]
