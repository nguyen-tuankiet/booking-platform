FROM maven:3.9.11-amazoncorretto-21-debian AS build

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM amazoncorretto:21.0.8
WORKDIR /app
COPY --from=build /app/notification-service/target/notification-service-*.jar app.jar
EXPOSE 8084
CMD ["java", "-jar", "app.jar"]