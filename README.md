# ✈️ Airline Booking Microservices System

Hệ thống đặt vé máy bay được xây dựng theo kiến trúc **Microservices**, sử dụng **Java Spring Boot**, hỗ trợ thanh toán, tìm chuyến bay, quản lý vé, OTP, thông báo và khuyến mãi. Hệ thống có khả năng mở rộng và chịu tải cao.

---

## 🧩 Các Service và Thành phần

### 1. **Auth Service**
**Chức năng**:
- Đăng ký, đăng nhập người dùng
- Quản lý hồ sơ cá nhân
- Đặt lại mật khẩu
**Công nghệ**:
- Spring Boot, Spring Security
- JWT Authentication
- MySQL (User Database)
- Redis (Session cache)
- Swagger (API docs)

---

### 2. **Booking Service**
**Chức năng**:
- Tìm kiếm chuyến bay
- Đặt chỗ, chọn ghế, khóa ghế
- Quản lý trạng thái đặt vé

**Công nghệ**:
- Spring Boot, MongoDB
- Kafka (Booking events)
- Pessimistic Locking, Seat Timeout
- External API Adapter (mock)

---

### 3. **Payment Service**
**Chức năng**:
- Xử lý thanh toán, hoàn tiền
- Tích hợp OTP và xác thực giao dịch
- Quản lý lịch sử thanh toán

**Công nghệ**:
- Spring Boot, MySQL
- Kafka (Payment events)
- Saga Pattern (Distributed transaction)
- Mock Payment Gateway (VNPay, MoMo)

---

### 4. **Notification Service**
**Chức năng**:
- Gửi thông báo Email, SMS (mock)
- Push notifications (mock)
- Quản lý mẫu thông báo

**Công nghệ**:
- Spring Boot, MongoDB
- Kafka Consumer (notification events)
- SMTP, Rate Limiting

---

### 5. **Booking History Service**
**Chức năng**:
- Truy vấn lịch sử đặt vé
- Theo dõi hoạt động người dùng
- Tìm kiếm và lọc lịch sử

**Công nghệ**:
- Spring Boot, MongoDB
- Redis (Cache lịch sử)
- Kafka (Sync dữ liệu)

---

### 6. **Promotion Service**
**Chức năng**:
- Quản lý chương trình khuyến mãi
- Áp dụng mã giảm giá
- Tính toán ưu đãi

**Công nghệ**:
- Spring Boot, MySQL
- Redis (Cache ưu đãi)
- AOP (Logging)

---

### 7. **API Gateway**
**Chức năng**:
- Routing các request đến từng service
- Quản lý bảo mật API và CORS

**Công nghệ**:
- Spring Cloud Gateway
- Security Filter
- JWT Token Validation

---

### 8. **Service Discovery**
**Chức năng**:
- Đăng ký và khám phá dịch vụ động

**Công nghệ**:
- Eureka Server (Netflix OSS)

---

### 9. **Common Library**
**Chức năng**:
- Base DTOs, Exception handler
- Cấu hình chung, logging, validation

**Công nghệ**:
- Spring Boot
- Lombok, MapStruct
- Custom annotations

---

## 📦 Hạ tầng & DevOps

- **Containerization**: Docker, Docker Compose
- **Message Queue**: Apache Kafka
- **Database**:
  - MySQL: User, Payment, Promotion
  - MongoDB: Booking, Notification, History
  - Redis: Cache, Session, Promotions
- **Monitoring**: Spring Actuator, Micrometer (Prometheus optional)
- **Documentation**: Swagger/OpenAPI
- **Version Control**: Git + GitHub

---

## 📖 Tài liệu tham khảo

- Spring Boot & Spring Cloud Docs  
- Apache Kafka Documentation  
- Microservices Patterns - Chris Richardson  
- Building Event-Driven Microservices  

---

> Hệ thống được chia theo service độc lập, giao tiếp bằng Kafka events và REST. Mỗi service có thể phát triển, triển khai, mở rộng riêng biệt.
