# Build stage
FROM maven:3.9.11-amazoncorretto-21-debian AS build


WORKDIR /app

# Copy entire project structure
COPY . .

# Build the project
RUN mvn clean package -DskipTests

# Runtime stage
FROM amazoncorretto:21.0.8

WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/payment-service/target/payment-service-*.jar app.jar

# Run application
EXPOSE 8761
CMD ["java", "-jar", "app.jar"]