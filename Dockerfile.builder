# Root Dockerfile for building all services
# This builds the entire multi-module project and creates individual service JARs

FROM openjdk:21-jdk-slim as common-builder

WORKDIR /app

# Copy Maven wrapper and root configuration
COPY mvnw .
COPY .mvn .mvn/
COPY pom.xml .

# Copy all module pom.xml files first for better caching
COPY common-library/pom.xml common-library/
COPY eureka-server/pom.xml eureka-server/
COPY auth-service/pom.xml auth-service/
COPY booking-service/pom.xml booking-service/
COPY payment-service/pom.xml payment-service/
COPY notification-service/pom.xml notification-service/
COPY api-gateway/pom.xml api-gateway/

# Make Maven wrapper executable
RUN chmod +x ./mvnw

# Download dependencies for all modules
RUN ./mvnw dependency:go-offline -B

# Copy all source code
COPY common-library/src common-library/src/
COPY eureka-server/src eureka-server/src/
COPY auth-service/src auth-service/src/
COPY booking-service/src booking-service/src/
COPY payment-service/src payment-service/src/
COPY notification-service/src notification-service/src/
COPY api-gateway/src api-gateway/src/

# Build all modules
RUN ./mvnw clean install -DskipTests -B