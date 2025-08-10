package com.booking.auth.auth_service.dto.request;

import com.booking.auth.auth_service.utils.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserStatusRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "User status is required")
    private UserStatus status;
    
    private String reason;
} 