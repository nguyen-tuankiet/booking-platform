# Multi-stage build for Auth Service
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Install maven
RUN apk add --no-cache maven

# Copy entire project root context
COPY . .

# Build common-library first, then auth-service
RUN mvn clean install -DskipTests -pl common-library,auth-service -am

# Production stage
FROM eclipse-temurin:21-jre-alpine

# Install necessary packages
RUN apk add --no-cache curl

# Create application user
RUN addgroup -g 1001 -S auth && \
    adduser -u 1001 -S auth -G auth

# Set working directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/auth-service/target/*.jar app.jar

# Change ownership
RUN chown auth:auth app.jar

# Switch to non-root user
USER auth

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]