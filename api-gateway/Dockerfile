# Multi-stage build for API Gateway
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Install maven
RUN apk add --no-cache maven

# Copy entire project root context
COPY . .

# Build common-library first, then api-gateway
RUN mvn clean install -DskipTests -pl common-library,api-gateway -am

# Production stage
FROM eclipse-temurin:21-jre-alpine

# Install necessary packages
RUN apk add --no-cache curl

# Create application user
RUN addgroup -g 1001 -S gateway && \
    adduser -u 1001 -S gateway -G gateway

# Set working directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/api-gateway/target/*.jar app.jar

# Change ownership
RUN chown gateway:gateway app.jar

# Switch to non-root user
USER gateway

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]