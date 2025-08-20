# Multi-stage build for Eureka Server
FROM openjdk:21-jdk-slim as builder

WORKDIR /app

# Copy entire project root context
COPY . .

# Make Maven wrapper executable
RUN chmod +x ./mvnw

# Build common-library first, then eureka-server
RUN ./mvnw clean install -DskipTests -pl common-library,eureka-server -am

# Production stage
FROM openjdk:21-jre-slim

# Install necessary packages
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# Create application user
RUN addgroup --system eureka && \
    adduser --system --group eureka

# Set working directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/eureka-server/target/*.jar app.jar

# Change ownership
RUN chown eureka:eureka app.jar

# Switch to non-root user
USER eureka

# Expose port
EXPOSE 8761

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8761/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]