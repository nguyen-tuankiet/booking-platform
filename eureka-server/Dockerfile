# Multi-stage build for Eureka Server
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Install maven
RUN apk add --no-cache maven

# Copy entire project root context
COPY . .

# Build common-library first, then eureka-server
RUN mvn clean install -DskipTests -pl common-library,eureka-server -am

# Production stage
FROM eclipse-temurin:21-jre-alpine

# Install necessary packages
RUN apk add --no-cache curl

# Create application user
RUN addgroup -g 1001 -S eureka && \
    adduser -u 1001 -S eureka -G eureka

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