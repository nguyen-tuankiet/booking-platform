package com.booking.auth.auth_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "First name can only contain letters and spaces")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Last name can only contain letters and spaces")
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number should be valid")
    private String phoneNumber;
    
    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    private String bio;
    
    @Size(max = 100, message = "Address cannot exceed 100 characters")
    private String address;
    
    @Size(max = 50, message = "City cannot exceed 50 characters")
    private String city;
    
    @Size(max = 50, message = "Country cannot exceed 50 characters")
    private String country;
    
    @Pattern(regexp = "^[0-9]{5,10}$", message = "Postal code should be valid")
    private String postalCode;

    private String avatar;
} 