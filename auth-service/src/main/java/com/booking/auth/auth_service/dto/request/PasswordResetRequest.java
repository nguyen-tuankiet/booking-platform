package com.booking.auth.auth_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Password reset request payload")
public class PasswordResetRequest {

    @NotBlank(message = "Token is required")
    @Schema(description = "Password reset token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @NotBlank(message = "New password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "Password must contain at least one lowercase letter, one uppercase letter, and one digit"
    )
    @Schema(description = "New password", example = "NewPassword123")
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    @Schema(description = "Password confirmation", example = "NewPassword123")
    private String confirmPassword;

    public boolean isPasswordConfirmed() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}