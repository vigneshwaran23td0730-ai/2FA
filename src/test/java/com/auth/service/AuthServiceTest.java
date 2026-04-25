package com.auth.service;

import com.auth.dto.RegisterRequest;
import com.auth.dto.LoginRequest;
import com.auth.dto.ApiResponse;
import com.auth.dto.UserProfileResponse;
import com.auth.dto.AuthResponse;
import com.auth.entity.User;
import com.auth.exception.UserAlreadyExistsException;
import com.auth.exception.UserNotFoundException;
import com.auth.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private OtpService otpService;

    @Mock
    private TotpService totpService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("John Doe");
        registerRequest.setEmail("john.doe@example.com");
        registerRequest.setPhone("+1234567890");
        registerRequest.setPassword("SecurePass123!");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john.doe@example.com");
        loginRequest.setPassword("SecurePass123!");

        testUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .password("encodedPassword")
                .enabled(true)
                .twoFaEnabled(false)
                .roles(Set.of(User.Role.USER))
                .build();
    }

    @Test
    void register_Success() {
        // Given
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhone(registerRequest.getPhone())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        ApiResponse<UserProfileResponse> response = authService.register(registerRequest);

        // Then
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertEquals("User registered successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(testUser.getEmail(), response.getData().getEmail());
        assertEquals(testUser.getName(), response.getData().getName());

        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository).existsByPhone(registerRequest.getPhone());
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_UserAlreadyExists_ThrowsException() {
        // Given
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // When & Then
        assertThrows(UserAlreadyExistsException.class, () -> authService.register(registerRequest));
        
        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success_Without2FA() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findActiveUserByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(authentication)).thenReturn("jwt-token");
        when(jwtService.getJwtExpirationInSeconds()).thenReturn(3600);

        // When
        ApiResponse<AuthResponse> response = authService.login(loginRequest, httpServletRequest);

        // Then
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertEquals("Login successful", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("jwt-token", response.getData().getAccessToken());
        assertEquals("Bearer", response.getData().getTokenType());
        assertEquals(3600L, response.getData().getExpiresIn());
        assertFalse(response.getData().getRequiresTwoFactor());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findActiveUserByEmail(loginRequest.getEmail());
        verify(jwtService).generateToken(authentication);
    }

    @Test
    void login_Success_With2FA() {
        // Given
        testUser.setTwoFaEnabled(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findActiveUserByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.of(testUser));

        // When
        ApiResponse<AuthResponse> response = authService.login(loginRequest, httpServletRequest);

        // Then
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertEquals("Login successful, 2FA required", response.getMessage());
        assertNotNull(response.getData());
        assertNull(response.getData().getAccessToken());
        assertTrue(response.getData().getRequiresTwoFactor());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findActiveUserByEmail(loginRequest.getEmail());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_UserNotFound_ThrowsException() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findActiveUserByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> authService.login(loginRequest, httpServletRequest));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findActiveUserByEmail(loginRequest.getEmail());
    }
}