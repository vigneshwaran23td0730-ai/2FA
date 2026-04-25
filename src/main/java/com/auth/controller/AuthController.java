package com.auth.controller;

import com.auth.dto.*;
import com.auth.service.AuthService;
import com.google.zxing.WriterException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and 2FA management endpoints")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Create a new user account with email and password")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User registered successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "User already exists"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<ApiResponse<UserProfileResponse>> register(@Valid @RequestBody RegisterRequest request) {
        ApiResponse<UserProfileResponse> response = authService.register(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user with email and password")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request, 
                                                          HttpServletRequest httpRequest) {
        ApiResponse<AuthResponse> response = authService.login(request, httpRequest);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/request-otp")
    @Operation(summary = "Request OTP", description = "Generate and send OTP to user via email or SMS")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP sent successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    public ResponseEntity<ApiResponse<String>> requestOtp(@Valid @RequestBody OtpRequest request,
                                                         HttpServletRequest httpRequest) {
        ApiResponse<String> response = authService.requestOtp(request, httpRequest);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP", description = "Verify OTP code and complete authentication")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP verified successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired OTP"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@Valid @RequestBody OtpVerificationRequest request,
                                                              HttpServletRequest httpRequest) {
        ApiResponse<AuthResponse> response = authService.verifyOtp(request, httpRequest);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/enable-totp")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Enable TOTP", description = "Generate TOTP secret and QR code for authenticator app setup")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "TOTP setup initiated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<TotpSetupResponse>> enableTotp(HttpServletRequest httpRequest) 
            throws WriterException, IOException {
        ApiResponse<TotpSetupResponse> response = authService.enableTotp(httpRequest);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/verify-totp")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Verify TOTP", description = "Verify TOTP code and complete 2FA setup")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "TOTP verified and enabled"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid TOTP code"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<String>> verifyTotp(@Valid @RequestBody TotpSetupRequest request,
                                                         HttpServletRequest httpRequest) {
        ApiResponse<String> response = authService.verifyTotp(request, httpRequest);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/me")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get current user profile", description = "Retrieve authenticated user's profile information")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser() {
        ApiResponse<UserProfileResponse> response = authService.getCurrentUserProfile();
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "User logout", description = "Logout current user and invalidate session")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logout successful"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<String>> logout() {
        ApiResponse<String> response = authService.logout();
        return ResponseEntity.ok(response);
    }
}