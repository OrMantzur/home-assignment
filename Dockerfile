# Stage 1: Build
# Use a maintained Maven image with JDK 11
FROM maven:3.9.6-eclipse-temurin-11 AS build
WORKDIR /app

# Copy and download dependencies (for caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build the JAR
COPY src ./src
RUN mvn package -DskipTests

# Stage 2: Tiny Runtime Image
# Use a maintained Eclipse Temurin JRE 11 image
FROM eclipse-temurin:11-jre-alpine
WORKDIR /app

# Copy the JAR from the build stage (renaming it to app.jar inside the container)
COPY --from=build /app/target/anomaly-detector.jar app.jar

# Security: Run as a non-root user
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

EXPOSE 8080

# JVM Tuning: MaxRAMPercentage ensures the JVM respects Docker memory limits
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:InitialRAMPercentage=75.0", "-XX:MaxRAMPercentage=75.0", "-noverify", "-jar", "app.jar"]