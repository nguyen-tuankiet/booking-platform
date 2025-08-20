# Multi-stage build for Payment Service
FROM openjdk:21-jdk-slim as builder

WORKDIR /app

# Copy entire project root context
COPY . .

# Make Maven wrapper executable
RUN chmod +x ./mvnw

# Build common-library first, then payment-service
RUN ./mvnw clean install -DskipTests -pl common-library,payment-service -am

# Production stage
FROM openjdk:21-jre-slim

# Install necessary packages
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# Create application user
RUN addgroup --system payment && \
    adduser --system --group payment

# Set working directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/payment-service/target/*.jar app.jar

# Change ownership
RUN chown payment:payment app.jar

# Switch to non-root user
USER payment

# Expose port
EXPOSE 8083

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8083/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]