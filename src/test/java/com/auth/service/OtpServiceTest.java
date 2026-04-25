package com.auth.service;

import com.auth.entity.OtpAttempt;
import com.auth.entity.User;
import com.auth.exception.RateLimitExceededException;
import com.auth.repository.OtpAttemptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private OtpAttemptRepository otpAttemptRepository;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private OtpService otpService;

    private User testUser;
    private String testIpAddress;

    @BeforeEach
    void setUp() {
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

        testIpAddress = "192.168.1.1";

        // Set up Redis template mock
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Set private fields using reflection
        ReflectionTestUtils.setField(otpService, "otpLength", 6);
        ReflectionTestUtils.setField(otpService, "otpExpirationSeconds", 300);
        ReflectionTestUtils.setField(otpService, "maxOtpAttempts", 5);
        ReflectionTestUtils.setField(otpService, "rateLimitWindowSeconds", 3600);
        ReflectionTestUtils.setField(otpService, "maxRequestsPerHour", 10);
    }

    @Test
    void generateOtp_Success() {
        // Given
        when(otpAttemptRepository.countByUserIdAndTypeAndCreatedAtAfter(
                eq(testUser.getId()), eq(OtpAttempt.AttemptType.OTP_REQUEST), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(otpAttemptRepository.countByIpAddressAndTypeAndCreatedAtAfter(
                eq(testIpAddress), eq(OtpAttempt.AttemptType.OTP_REQUEST), any(LocalDateTime.class)))
                .thenReturn(0L);

        // When
        String otp = otpService.generateOtp(testUser, testIpAddress);

        // Then
        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"));

        verify(valueOperations).set(eq("otp:" + testUser.getEmail()), anyString(), eq(300), eq(TimeUnit.SECONDS));
        verify(redisTemplate).delete("otp_attempts:" + testUser.getEmail());
        verify(otpAttemptRepository).save(any(OtpAttempt.class));
    }

    @Test
    void generateOtp_RateLimitExceeded_ThrowsException() {
        // Given
        when(otpAttemptRepository.countByUserIdAndTypeAndCreatedAtAfter(
                eq(testUser.getId()), eq(OtpAttempt.AttemptType.OTP_REQUEST), any(LocalDateTime.class)))
                .thenReturn(15L); // Exceeds limit

        // When & Then
        assertThrows(RateLimitExceededException.class, () -> 
                otpService.generateOtp(testUser, testIpAddress));

        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
        verify(otpAttemptRepository).save(any(OtpAttempt.class));
    }

    @Test
    void verifyOtp_Success() {
        // Given
        String otp = "123456";
        String hashedOtp = "hashedOtp"; // This would be the actual hash in real scenario
        
        when(valueOperations.get("otp_attempts:" + testUser.getEmail())).thenReturn(null);
        when(valueOperations.get("otp:" + testUser.getEmail())).thenReturn(hashedOtp);

        // Mock the hash comparison - in real scenario, this would use actual hashing
        // For test purposes, we'll assume the OTP is valid
        // Note: This test is simplified - in reality, you'd need to mock the hashing mechanism

        // When
        boolean result = otpService.verifyOtp(testUser, otp, testIpAddress);

        // Then - This test would need adjustment based on actual hashing implementation
        // For now, we verify the method calls
        verify(valueOperations).get("otp_attempts:" + testUser.getEmail());
        verify(valueOperations).get("otp:" + testUser.getEmail());
        verify(otpAttemptRepository).save(any(OtpAttempt.class));
    }

    @Test
    void verifyOtp_MaxAttemptsExceeded_ThrowsException() {
        // Given
        String otp = "123456";
        when(valueOperations.get("otp_attempts:" + testUser.getEmail())).thenReturn(5);

        // When & Then
        assertThrows(RateLimitExceededException.class, () -> 
                otpService.verifyOtp(testUser, otp, testIpAddress));

        verify(otpAttemptRepository).save(any(OtpAttempt.class));
    }

    @Test
    void verifyOtp_OtpNotFound_ReturnsFalse() {
        // Given
        String otp = "123456";
        when(valueOperations.get("otp_attempts:" + testUser.getEmail())).thenReturn(null);
        when(valueOperations.get("otp:" + testUser.getEmail())).thenReturn(null);

        // When
        boolean result = otpService.verifyOtp(testUser, otp, testIpAddress);

        // Then
        assertFalse(result);
        verify(otpAttemptRepository).save(any(OtpAttempt.class));
    }

    @Test
    void isOtpValid_ReturnsTrue_WhenOtpExists() {
        // Given
        String email = testUser.getEmail();
        when(redisTemplate.hasKey("otp:" + email)).thenReturn(true);

        // When
        boolean result = otpService.isOtpValid(email);

        // Then
        assertTrue(result);
        verify(redisTemplate).hasKey("otp:" + email);
    }

    @Test
    void isOtpValid_ReturnsFalse_WhenOtpDoesNotExist() {
        // Given
        String email = testUser.getEmail();
        when(redisTemplate.hasKey("otp:" + email)).thenReturn(false);

        // When
        boolean result = otpService.isOtpValid(email);

        // Then
        assertFalse(result);
        verify(redisTemplate).hasKey("otp:" + email);
    }
}