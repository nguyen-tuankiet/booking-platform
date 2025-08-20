# Docker Setup for Booking Platform

This directory contains Docker configurations for all microservices in the booking platform.

## Quick Start

1. **Build and run all services:**
   ```bash
   docker-compose up --build
   ```

2. **Run only infrastructure services:**
   ```bash
   docker-compose up mysql mongodb redis kafka zookeeper
   ```

3. **Run specific service:**
   ```bash
   docker-compose up eureka-server
   ```

## Services and Ports

### Infrastructure Services
- **MySQL**: `localhost:3306` - Database for auth and payment services
- **MongoDB**: `localhost:27017` - Database for booking and notification services  
- **Redis**: `localhost:6379` - Cache and session storage
- **Kafka**: `localhost:9092` - Message broker
- **Zookeeper**: `localhost:2181` - Kafka coordination

### Microservices
- **Eureka Server**: `localhost:8761` - Service discovery
- **API Gateway**: `localhost:8080` - Main entry point
- **Auth Service**: `localhost:8081` - Authentication and authorization
- **Booking Service**: `localhost:8082` - Flight booking management
- **Payment Service**: `localhost:8083` - Payment processing
- **Notification Service**: `localhost:8084` - Notifications and messaging

## Environment Variables

The docker-compose.yml is configured with default values for development. For production, override these:

- `MYSQL_ROOT_PASSWORD`: MySQL root password (default: booking123)
- `MONGO_INITDB_ROOT_PASSWORD`: MongoDB root password (default: booking123)
- External API keys for payment gateways (VNPay, MoMo)

## Database Initialization

- MySQL databases are automatically created: `auth_booking`, `payment_service`, `booking_db`
- MongoDB databases are created on first use: `booking_service`, `notification_service`

## Health Checks

All services include health checks. Use `docker-compose ps` to verify all services are healthy.

## Scaling

To scale services horizontally:
```bash
docker-compose up --scale booking-service=3 --scale payment-service=2
```

## Logs

View logs for all services:
```bash
docker-compose logs -f
```

View logs for specific service:
```bash
docker-compose logs -f auth-service
```

## Troubleshooting

1. **Build failures**: Ensure Java 21 is used in Dockerfiles
2. **Database connection issues**: Check if infrastructure services are healthy
3. **Service discovery issues**: Ensure Eureka server is running first
4. **Port conflicts**: Modify port mappings in docker-compose.yml if needed