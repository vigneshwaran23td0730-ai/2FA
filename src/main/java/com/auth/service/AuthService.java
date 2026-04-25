package com.auth.service;

import com.auth.dto.*;
import com.auth.entity.User;
import com.auth.exception.InvalidCredentialsException;
import com.auth.exception.InvalidTotpException;
import com.auth.exception.UserAlreadyExistsException;
import com.auth.exception.UserNotFoundException;
import com.auth.repository.UserRepository;
import com.auth.security.UserPrincipal;
import com.auth.util.IpAddressUtil;
import com.google.zxing.WriterException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final TotpService totpService;
    private final NotificationService notificationService;
    
    @Transactional
    public ApiResponse<UserProfileResponse> register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User already exists with email: " + request.getEmail());
        }
        
        if (StringUtils.hasText(request.getPhone()) && userRepository.existsByPhone(request.getPhone())) {
            throw new UserAlreadyExistsException("User already exists with phone: " + request.getPhone());
        }
        
        // Create new user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .twoFaEnabled(false)
                .build();
        
        user = userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());
        
        UserProfileResponse response = mapToUserProfileResponse(user);
        return ApiResponse.success("User registered successfully", response);
    }
    
    public ApiResponse<AuthResponse> login(LoginRequest request, HttpServletRequest httpRequest) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        User user = userRepository.findActiveUserByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        // Check if 2FA is enabled
        if (user.getTwoFaEnabled()) {
            // Don't generate JWT yet, require 2FA verification
            AuthResponse response = AuthResponse.builder()
                    .requiresTwoFactor(true)
                    .message("2FA verification required")
                    .build();
            
            return ApiResponse.success("Login successful, 2FA required", response);
        }
        
        // Generate JWT token
        String jwt = jwtService.generateToken(authentication);
        
        AuthResponse response = AuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .expiresIn((long) jwtService.getJwtExpirationInSeconds())
                .requiresTwoFactor(false)
                .message("Login successful")
                .build();
        
        log.info("User logged in successfully: {}", user.getEmail());
        return ApiResponse.success("Login successful", response);
    }
    
    public ApiResponse<String> requestOtp(OtpRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findActiveUserByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        String ipAddress = IpAddressUtil.getClientIpAddress(httpRequest);
        String otp = otpService.generateOtp(user, ipAddress);
        
        // Send OTP via requested delivery method
        notificationService.sendOtp(user, otp, request.getDeliveryMethod());
        
        return ApiResponse.success("OTP sent successfully");
    }
    
    public ApiResponse<AuthResponse> verifyOtp(OtpVerificationRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findActiveUserByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        String ipAddress = IpAddressUtil.getClientIpAddress(httpRequest);
        boolean isValid = otpService.verifyOtp(user, request.getOtp(), ipAddress);
        
        if (!isValid) {
            throw new InvalidCredentialsException("Invalid or expired OTP");
        }
        
        // Create authentication and generate JWT
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities());
        
        String jwt = jwtService.generateToken(authentication);
        
        AuthResponse response = AuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .expiresIn((long) jwtService.getJwtExpirationInSeconds())
                .requiresTwoFactor(false)
                .message("OTP verified successfully")
                .build();
        
        return ApiResponse.success("OTP verification successful", response);
    }
    
    @Transactional
    public ApiResponse<TotpSetupResponse> enableTotp(HttpServletRequest httpRequest) throws WriterException, IOException {
        UserPrincipal userPrincipal = getCurrentUser();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        // Generate TOTP secret
        String secret = totpService.generateSecret();
        String qrCodeUrl = totpService.generateQrCodeUrl(user.getEmail(), secret);
        String qrCodeImage = totpService.generateQrCodeImage(qrCodeUrl);
        
        // Save secret temporarily (will be confirmed when user verifies TOTP)
        user.setTotpSecret(secret);
        userRepository.save(user);
        
        TotpSetupResponse response = TotpSetupResponse.builder()
                .secret(secret)
                .qrCodeUrl(qrCodeUrl)
                .qrCodeImage(qrCodeImage)
                .instructions("Scan the QR code with your authenticator app and enter the 6-digit code to complete setup")
                .build();
        
        return ApiResponse.success("TOTP setup initiated", response);
    }
    
    @Transactional
    public ApiResponse<String> verifyTotp(TotpSetupRequest request, HttpServletRequest httpRequest) {
        UserPrincipal userPrincipal = getCurrentUser();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        if (!StringUtils.hasText(user.getTotpSecret())) {
            throw new InvalidTotpException("TOTP setup not initiated");
        }
        
        String ipAddress = IpAddressUtil.getClientIpAddress(httpRequest);
        boolean isValid = totpService.verifyTotp(user.getTotpSecret(), request.getTotpCode(), user, ipAddress);
        
        if (!isValid) {
            throw new InvalidTotpException("Invalid TOTP code");
        }
        
        // Enable 2FA for user
        user.setTwoFaEnabled(true);
        userRepository.save(user);
        
        log.info("TOTP enabled for user: {}", user.getEmail());
        return ApiResponse.success("TOTP enabled successfully");
    }
    
    public ApiResponse<UserProfileResponse> getCurrentUserProfile() {
        UserPrincipal userPrincipal = getCurrentUser();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        UserProfileResponse response = mapToUserProfileResponse(user);
        return ApiResponse.success("User profile retrieved", response);
    }
    
    public ApiResponse<String> logout() {
        // In a more complete implementation, you might want to blacklist the JWT token
        // For now, we'll just clear the security context
        SecurityContextHolder.clearContext();
        return ApiResponse.success("Logged out successfully");
    }
    
    private UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new RuntimeException("No authenticated user found");
        }
        return (UserPrincipal) authentication.getPrincipal();
    }
    
    private UserProfileResponse mapToUserProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .enabled(user.getEnabled())
                .twoFaEnabled(user.getTwoFaEnabled())
                .roles(user.getRoles())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}