# booking-platform

## ✈️ Airline Booking Microservices System

Hệ thống đặt vé máy bay được xây dựng theo kiến trúc **Microservices**, sử dụng **Java Spring Boot**, hỗ trợ thanh toán, tìm chuyến bay, quản lý vé, OTP, thông báo và khuyến mãi. Hệ thống có khả năng mở rộng và chịu tải cao.

## 🐳 Docker Setup

### Quick Start với Docker Compose

1. **Clone repository:**
   ```bash
   git clone https://github.com/nguyen-tuankiet/booking-platform.git
   cd booking-platform
   ```

2. **Khởi động toàn bộ hệ thống:**
   ```bash
   docker-compose up --build
   ```

3. **Khởi động chỉ infrastructure:**
   ```bash
   docker-compose up mysql mongodb redis kafka zookeeper
   ```

4. **Khởi động từng service:**
   ```bash
   docker-compose up eureka-server
   docker-compose up auth-service
   ```

### Services và Ports

#### Infrastructure Services
- **MySQL**: `localhost:3306` - Database cho auth và payment services
- **MongoDB**: `localhost:27017` - Database cho booking và notification services  
- **Redis**: `localhost:6379` - Cache và session storage
- **Kafka**: `localhost:9092` - Message broker
- **Zookeeper**: `localhost:2181` - Kafka coordination

#### Microservices
- **Eureka Server**: `localhost:8761` - Service discovery
- **API Gateway**: `localhost:8080` - Main entry point  
- **Auth Service**: `localhost:8081` - Authentication và authorization
- **Booking Service**: `localhost:8082` - Quản lý đặt vé
- **Payment Service**: `localhost:8083` - Xử lý thanh toán
- **Notification Service**: `localhost:8084` - Thông báo và messaging

### Kiến trúc Dockerfiles

Mỗi service sử dụng **multi-stage build** với:
- **Build stage**: Eclipse Temurin JDK 21 Alpine + Maven
- **Runtime stage**: Eclipse Temurin JRE 21 Alpine (nhẹ hơn)
- **Security**: Non-root user cho mỗi container
- **Health checks**: Monitoring tự động

### Environment Variables

```yaml
# Database
MYSQL_ROOT_PASSWORD: booking123
MONGO_INITDB_ROOT_PASSWORD: booking123

# Service URLs  
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE: http://eureka-server:8761/eureka/
SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/...
SPRING_DATA_MONGODB_URI: mongodb://admin:booking123@mongodb:27017/...
```

### Commands Hữu ích

```bash
# Xem logs tất cả services
docker-compose logs -f

# Xem logs của service cụ thể
docker-compose logs -f auth-service

# Scale services
docker-compose up --scale booking-service=3

# Dừng và xóa containers + volumes
docker-compose down -v --remove-orphans

# Build lại images
docker-compose build --no-cache

# Xem trạng thái services
docker-compose ps
```

### Database Initialization

- **MySQL**: Tự động tạo databases: `auth_booking`, `payment_service`, `booking_db`
- **MongoDB**: Tự động tạo databases khi service chạy lần đầu

### Monitoring & Health Checks

Tất cả services có health checks:
```bash
# Kiểm tra health của tất cả services
curl http://localhost:8761/actuator/health  # Eureka
curl http://localhost:8081/actuator/health  # Auth Service
curl http://localhost:8082/actuator/health  # Booking Service
```

### Troubleshooting

1. **Build lỗi**: Kiểm tra Java 21 và Maven trong container
2. **Database connection**: Đảm bảo infrastructure services healthy trước
3. **Service discovery**: Eureka Server phải chạy trước các services khác
4. **Port conflicts**: Sửa port mapping trong docker-compose.yml

---

## 🧩 Các Service và Thành phần

### 1. **Auth Service**
**Chức năng**:
- Đăng ký, đăng nhập, JWT authentication
- Quản lý user roles và permissions
- OTP verification, password reset

**Công nghệ**:
- Spring Security + JWT
- MySQL database
- Redis cho session caching
- Email service integration

### 2. **Booking Service**
**Chức năng**:
- Tìm kiếm chuyến bay
- Đặt vé và quản lý booking
- Seat selection và pricing

**Công nghệ**:
- Spring Boot + Spring Data MongoDB
- Redis caching
- Kafka events
- External flight APIs

### 3. **Payment Service**
**Chức năng**:
- Xử lý thanh toán VNPay, MoMo
- Transaction management
- Payment history

**Công nghệ**:
- Spring Boot + JPA
- MySQL database
- Payment gateway integration
- Saga pattern cho distributed transactions

### 4. **Notification Service**
**Chức năng**:
- Email notifications
- SMS/Push notifications  
- Template management

**Công nghệ**:
- Spring Boot
- MongoDB cho notification history
- Kafka consumers
- Email/SMS providers

### 5. **API Gateway**
**Chức năng**:
- Request routing và load balancing
- Authentication middleware
- Rate limiting và CORS

**Công nghệ**:
- Spring Cloud Gateway
- JWT validation
- Circuit breaker pattern

### 6. **Eureka Server**
**Chức năng**:
- Service discovery và registration
- Health monitoring

**Công nghệ**:
- Netflix Eureka
- Service mesh coordination

---

## 📦 Hạ tầng & DevOps

- **Containerization**: Docker, Docker Compose
- **Message Queue**: Apache Kafka
- **Database**:
  - MySQL: User, Payment, Promotion
  - MongoDB: Booking, Notification, History
  - Redis: Cache, Session, Promotions
- **Monitoring**: Spring Actuator, Health checks
- **Documentation**: Swagger/OpenAPI
- **Version Control**: Git + GitHub

---

## 📖 Tài liệu tham khảo

- Spring Boot & Spring Cloud Docs  
- Apache Kafka Documentation  
- Microservices Patterns - Chris Richardson  
- Building Event-Driven Microservices  
- Docker & Docker Compose Documentation

---

> Hệ thống được chia theo service độc lập, giao tiếp bằng Kafka events và REST. Mỗi service có thể phát triển, triển khai, mở rộng riêng biệt và được containerized với Docker.