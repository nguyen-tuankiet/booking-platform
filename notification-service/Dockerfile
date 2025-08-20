# Multi-stage build for Notification Service
FROM openjdk:21-jdk-slim as builder

WORKDIR /app

# Copy entire project root context
COPY . .

# Make Maven wrapper executable
RUN chmod +x ./mvnw

# Build common-library first, then notification-service
RUN ./mvnw clean install -DskipTests -pl common-library,notification-service -am

# Production stage
FROM openjdk:21-jre-slim

# Install necessary packages
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# Create application user
RUN addgroup --system notification && \
    adduser --system --group notification

# Set working directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/notification-service/target/*.jar app.jar

# Change ownership
RUN chown notification:notification app.jar

# Switch to non-root user
USER notification

# Expose port
EXPOSE 8084

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8084/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]