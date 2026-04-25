package com.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "TOTP setup request")
public class TotpSetupRequest {
    
    @NotBlank(message = "TOTP code is required")
    @Pattern(regexp = "^\\d{6}$", message = "TOTP code must be 6 digits")
    @Schema(description = "6-digit TOTP code from authenticator app", example = "123456")
    private String totpCode;
}