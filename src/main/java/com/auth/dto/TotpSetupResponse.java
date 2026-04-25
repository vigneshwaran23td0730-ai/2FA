package com.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "TOTP setup response")
public class TotpSetupResponse {
    
    @Schema(description = "TOTP secret key", example = "JBSWY3DPEHPK3PXP")
    private String secret;
    
    @Schema(description = "QR code URL for authenticator apps", 
            example = "otpauth://totp/SecureAuth:john.doe@example.com?secret=JBSWY3DPEHPK3PXP&issuer=SecureAuth")
    private String qrCodeUrl;
    
    @Schema(description = "Base64 encoded QR code image")
    private String qrCodeImage;
    
    @Schema(description = "Setup instructions", 
            example = "Scan the QR code with your authenticator app and enter the 6-digit code to complete setup")
    private String instructions;
}