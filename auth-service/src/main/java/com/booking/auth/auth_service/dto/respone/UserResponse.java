package com.booking.auth.auth_service.dto.respone;

import com.booking.auth.auth_service.utils.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User information response")
public class UserResponse {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "Username", example = "johndoe")
    private String username;

    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "First name", example = "John")
    private String firstName;

    @Schema(description = "Last name", example = "Doe")
    private String lastName;

    @Schema(description = "Phone number", example = "+1234567890")
    private String phoneNumber;

    @Schema(description = "Date of birth")
    private LocalDateTime dateOfBirth;

    @Schema(description = "Avatar URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "Account status", example = "ACTIVE")
    private UserStatus status;

    @Schema(description = "Email verification status", example = "true")
    private boolean emailVerified;

    @Schema(description = "Phone verification status", example = "false")
    private boolean phoneVerified;

    @Schema(description = "User roles")
    private Set<String> roles;

    @Schema(description = "Account creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last login timestamp")
    private LocalDateTime lastLogin;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
