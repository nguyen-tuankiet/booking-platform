# Auth Service API Documentation

## Overview
The Auth Service provides authentication and authorization functionality for the Booking Platform. It handles user registration, login, profile management, and admin operations.

## Base URL
- Local: `http://localhost:8081`
- Production: `https://auth-service.bookingplatform.com`

## Authentication
All protected endpoints require a JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## API Endpoints

### Authentication Endpoints

#### 1. Register User
- **POST** `/auth/register`
- **Description**: Register a new user account
- **Request Body**:
```json
{
  "username": "tuankiet2211",
  "email": "tuankietcoder2211@gmail.com",
  "password": "password123",
  "firstName": "Tuấn",
  "lastName": "Kiệt",
  "phoneNumber": "+1234567890"
}
```
- **Response**: `201 Created`
```json
{
  "code": 201,
  "message": "User Registered Successfully",
  "data": {
    "accessToken": "jwt-token",
    "refreshToken": "refresh-token",
    "user": {
      "id": 1,
      "username": "tuankiet2211",
      "email": "tuankietcoder2211@gmail.com",
      "firstName": "Tuấn",
      "lastName": "Kiệt"
    }
  }
}
```

#### 2. Login
- **POST** `/auth/login`
- **Description**: Authenticate user and return JWT tokens
- **Request Body**:
```json
{
  "username": "john_doe",
  "password": "password123"
}
```
- **Response**: `200 OK`
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "accessToken": "jwt-token",
    "refreshToken": "refresh-token",
    "user": {
      "id": 1,
      "username": "tuankiet2211",
      "email": "tuankietcoder2211@gmail.com"
    }
  }
}
```

#### 3. Logout
- **POST** `/auth/logout`
- **Description**: Logout user and invalidate refresh token
- **Query Parameters**: `refreshToken` (optional)
- **Response**: `200 OK`

#### 4. Refresh Token
- **POST** `/auth/refresh-token`
- **Description**: Generate new access token using refresh token
- **Query Parameters**: `refreshToken`
- **Response**: `200 OK`

#### 5. Forgot Password
- **POST** `/auth/forgot-password`
- **Description**: Send password reset email
- **Query Parameters**: `email`
- **Response**: `200 OK`

#### 6. Reset Password
- **POST** `/auth/reset-password`
- **Description**: Reset password using reset token
- **Request Body**:
```json
{
  "token": "reset-token",
  "newPassword": "newpassword123"
}
```
- **Response**: `200 OK`

#### 7. Verify Email
- **GET** `/auth/verify-email`
- **Description**: Verify user email using verification token
- **Query Parameters**: `token`
- **Response**: `200 OK`

#### 8. Resend Email Verification
- **POST** `/auth/resend-verification`
- **Description**: Resend email verification to current user
- **Headers**: `Authorization: Bearer <token>`
- **Response**: `200 OK`

### User Management Endpoints

#### 1. Get Current User Profile
- **GET** `/users/profile`
- **Description**: Get current user's profile information
- **Headers**: `Authorization: Bearer <token>`
- **Response**: `200 OK`

#### 2. Get User by ID
- **GET** `/users/{userId}`
- **Description**: Get user profile by user ID
- **Response**: `200 OK`

#### 3. Get User by Username
- **GET** `/users/username/{username}`
- **Description**: Get user profile by username
- **Response**: `200 OK`

#### 4. Check Username Availability
- **GET** `/users/check/username`
- **Description**: Check if username is available
- **Query Parameters**: `username`
- **Response**: `200 OK`

#### 5. Check Email Availability
- **GET** `/users/check/email`
- **Description**: Check if email is available
- **Query Parameters**: `email`
- **Response**: `200 OK`

#### 6. Update Profile
- **PUT** `/users/profile`
- **Description**: Update current user's profile
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**:
```json
{
  "firstName": "Nguyen Tuan",
  "lastName": "Kiet",
  "email": "tuankietcoder2211@gmail.com",
  "phoneNumber": "+1234567890",
  "bio": "Software Developer",
  "address": "379E Phu Thanh, Phu Hung, Ben Tre",
  "city": "Ben Tre",
  "country": "Vn",
  "postalCode": "10001"
}
```
- **Response**: `200 OK`

#### 7. Change Password
- **POST** `/users/change-password`
- **Description**: Change current user's password
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**:
```json
{
  "currentPassword": "oldpassword",
  "newPassword": "newpassword123"
}
```
- **Response**: `200 OK`

#### 8. Deactivate Account
- **POST** `/users/deactivate`
- **Description**: Deactivate current user's account
- **Headers**: `Authorization: Bearer <token>`
- **Response**: `200 OK`

#### 9. Delete Account
- **DELETE** `/users/delete`
- **Description**: Permanently delete current user's account
- **Headers**: `Authorization: Bearer <token>`
- **Response**: `200 OK`

### Admin Endpoints

#### 1. Get All Users (Admin)
- **GET** `/admin/users`
- **Description**: Get all users with pagination and filtering
- **Headers**: `Authorization: Bearer <admin-token>`
- **Query Parameters**:
  - `page` (default: 0)
  - `size` (default: 10)
  - `sortBy` (default: "id")
  - `sortDir` (default: "DESC")
  - `status` (optional)
- **Response**: `200 OK`

#### 2. Search Users (Admin)
- **GET** `/admin/users/search`
- **Description**: Search users by keyword
- **Headers**: `Authorization: Bearer <admin-token>`
- **Query Parameters**:
  - `keyword`
  - `page` (default: 0)
  - `size` (default: 10)
- **Response**: `200 OK`

#### 3. Get User by ID (Admin)
- **GET** `/admin/users/{userId}`
- **Description**: Get specific user details
- **Headers**: `Authorization: Bearer <admin-token>`
- **Response**: `200 OK`

#### 4. Update User Status (Admin)
- **PUT** `/admin/users/{userId}/status`
- **Description**: Update user status (activate, deactivate, suspend)
- **Headers**: `Authorization: Bearer <admin-token>`
- **Request Body**:
```json
{
  "userId": 1,
  "status": "ACTIVE",
  "reason": "Account reactivated"
}
```
- **Response**: `200 OK`

#### 5. Delete User (Admin)
- **DELETE** `/admin/users/{userId}`
- **Description**: Permanently delete a user account
- **Headers**: `Authorization: Bearer <admin-token>`
- **Response**: `200 OK`

#### 6. Get User Statistics (Admin)
- **GET** `/admin/users/statistics`
- **Description**: Get user statistics for dashboard
- **Headers**: `Authorization: Bearer <admin-token>`
- **Response**: `200 OK`

### Health Check Endpoints

#### 1. Health Check
- **GET** `/health`
- **Description**: Check if the service is running
- **Response**: `200 OK`

#### 2. Readiness Check
- **GET** `/health/ready`
- **Description**: Check if the service is ready to handle requests
- **Response**: `200 OK`

## Error Responses

### Common Error Codes
- `400 Bad Request`: Invalid input data
- `401 Unauthorized`: Authentication required
- `403 Forbidden`: Access denied
- `404 Not Found`: Resource not found
- `409 Conflict`: Resource already exists
- `423 Locked`: Account locked
- `500 Internal Server Error`: Server error

### Error Response Format
```json
{
  "code": 400,
  "message": "Validation failed",
  "errors": [
    {
      "field": "email",
      "message": "Email should be valid"
    }
  ]
}
```

## Rate Limiting
- Authentication endpoints: 5 requests per minute per IP
- Other endpoints: 100 requests per minute per user

## Security
- All passwords are hashed using BCrypt
- JWT tokens expire after 15 minutes (access token) and 7 days (refresh token)
- CORS is configured for allowed origins
- Rate limiting is applied to prevent abuse

## Testing
You can test the API using the Swagger UI at:
- Local: `http://localhost:8081/swagger-ui.html`
- Production: `https://auth-service.bookingplatform.com/swagger-ui.html` 