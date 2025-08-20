# Multi-stage build for API Gateway
FROM openjdk:21-jdk-slim as builder

WORKDIR /app

# Copy entire project root context
COPY . .

# Make Maven wrapper executable
RUN chmod +x ./mvnw

# Build common-library first, then api-gateway
RUN ./mvnw clean install -DskipTests -pl common-library,api-gateway -am

# Production stage
FROM openjdk:21-jre-slim

# Install necessary packages
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# Create application user
RUN addgroup --system gateway && \
    adduser --system --group gateway

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