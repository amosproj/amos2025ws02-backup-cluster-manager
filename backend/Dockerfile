# Use Maven with JDK 21 as the build environment
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

ARG APP_USER=appuser
RUN addgroup -S $APP_USER && adduser -S -G $APP_USER $APP_USER

WORKDIR /app

# Copy project metadata and download dependencies
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B

# Copy source code into container and build the application
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -B

# Use JRE 21 image for running the app
FROM eclipse-temurin:21-jre-alpine

ARG APP_USER=appuser
RUN addgroup -S $APP_USER && \
    adduser -S -G $APP_USER $APP_USER
USER $APP_USER

# Working directory
WORKDIR /app

# Copy the fat JAR from the builder stage
COPY --from=builder /app/target/backend-*.jar app.jar

# default port
EXPOSE 8080

# Entrypoint
ENTRYPOINT ["java", "-jar", "/app/app.jar"]