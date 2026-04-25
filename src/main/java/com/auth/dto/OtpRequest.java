package com.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "OTP request")
public class OtpRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Schema(description = "User's email address", example = "john.doe@example.com")
    private String email;
    
    @NotNull(message = "Delivery method is required")
    @Schema(description = "OTP delivery method", example = "EMAIL")
    private DeliveryMethod deliveryMethod;
    
    public enum DeliveryMethod {
        EMAIL, SMS, BOTH
    }
}