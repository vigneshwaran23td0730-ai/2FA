package com.auth.service;

import com.auth.entity.OtpAttempt;
import com.auth.entity.User;
import com.auth.exception.RateLimitExceededException;
import com.auth.repository.OtpAttemptRepository;
import com.auth.util.OtpGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final OtpAttemptRepository otpAttemptRepository;
    
    @Value("${app.otp.length:6}")
    private int otpLength;
    
    @Value("${app.otp.expiration:300}")
    private int otpExpirationSeconds;
    
    @Value("${app.otp.max-attempts:5}")
    private int maxOtpAttempts;
    
    @Value("${app.otp.rate-limit-window:3600}")
    private int rateLimitWindowSeconds;
    
    @Value("${app.otp.max-requests-per-hour:10}")
    private int maxRequestsPerHour;
    
    private static final String OTP_KEY_PREFIX = "otp:";
    private static final String OTP_ATTEMPTS_PREFIX = "otp_attempts:";
    private static final String OTP_REQUESTS_PREFIX = "otp_requests:";
    
    public String generateOtp(User user, String ipAddress) {
        checkRateLimit(user.getId(), ipAddress);
        
        String otp = OtpGenerator.generate(otpLength);
        String hashedOtp = hashOtp(otp);
        
        // Store hashed OTP in Redis with expiration
        String otpKey = OTP_KEY_PREFIX + user.getEmail();
        redisTemplate.opsForValue().set(otpKey, hashedOtp, otpExpirationSeconds, TimeUnit.SECONDS);
        
        // Reset attempt counter
        String attemptsKey = OTP_ATTEMPTS_PREFIX + user.getEmail();
        redisTemplate.delete(attemptsKey);
        
        // Track request for rate limiting
        trackOtpRequest(user.getId(), ipAddress);
        
        log.info("OTP generated for user: {}", user.getEmail());
        return otp;
    }
    
    public boolean verifyOtp(User user, String otp, String ipAddress) {
        String otpKey = OTP_KEY_PREFIX + user.getEmail();
        String attemptsKey = OTP_ATTEMPTS_PREFIX + user.getEmail();
        
        // Check attempt limit
        Integer attempts = (Integer) redisTemplate.opsForValue().get(attemptsKey);
        if (attempts != null && attempts >= maxOtpAttempts) {
            recordAttempt(user.getId(), ipAddress, OtpAttempt.AttemptType.OTP_VERIFY, OtpAttempt.AttemptResult.RATE_LIMITED);
            throw new RateLimitExceededException("Maximum OTP verification attempts exceeded");
        }
        
        // Get stored hashed OTP
        String storedHashedOtp = (String) redisTemplate.opsForValue().get(otpKey);
        if (storedHashedOtp == null) {
            recordAttempt(user.getId(), ipAddress, OtpAttempt.AttemptType.OTP_VERIFY, OtpAttempt.AttemptResult.FAILURE);
            return false;
        }
        
        // Verify OTP
        String hashedInputOtp = hashOtp(otp);
        boolean isValid = MessageDigest.isEqual(storedHashedOtp.getBytes(), hashedInputOtp.getBytes());
        
        if (isValid) {
            // Clean up on successful verification
            redisTemplate.delete(otpKey);
            redisTemplate.delete(attemptsKey);
            recordAttempt(user.getId(), ipAddress, OtpAttempt.AttemptType.OTP_VERIFY, OtpAttempt.AttemptResult.SUCCESS);
            log.info("OTP verified successfully for user: {}", user.getEmail());
        } else {
            // Increment attempt counter
            redisTemplate.opsForValue().increment(attemptsKey);
            redisTemplate.expire(attemptsKey, otpExpirationSeconds, TimeUnit.SECONDS);
            recordAttempt(user.getId(), ipAddress, OtpAttempt.AttemptType.OTP_VERIFY, OtpAttempt.AttemptResult.FAILURE);
            log.warn("Invalid OTP attempt for user: {}", user.getEmail());
        }
        
        return isValid;
    }
    
    private void checkRateLimit(Long userId, String ipAddress) {
        LocalDateTime windowStart = LocalDateTime.now().minusSeconds(rateLimitWindowSeconds);
        
        // Check user-based rate limit
        long userRequests = otpAttemptRepository.countByUserIdAndTypeAndCreatedAtAfter(
                userId, OtpAttempt.AttemptType.OTP_REQUEST, windowStart);
        
        // Check IP-based rate limit
        long ipRequests = otpAttemptRepository.countByIpAddressAndTypeAndCreatedAtAfter(
                ipAddress, OtpAttempt.AttemptType.OTP_REQUEST, windowStart);
        
        if (userRequests >= maxRequestsPerHour || ipRequests >= maxRequestsPerHour) {
            recordAttempt(userId, ipAddress, OtpAttempt.AttemptType.OTP_REQUEST, OtpAttempt.AttemptResult.RATE_LIMITED);
            throw new RateLimitExceededException("OTP request rate limit exceeded");
        }
    }
    
    private void trackOtpRequest(Long userId, String ipAddress) {
        recordAttempt(userId, ipAddress, OtpAttempt.AttemptType.OTP_REQUEST, OtpAttempt.AttemptResult.SUCCESS);
    }
    
    private void recordAttempt(Long userId, String ipAddress, OtpAttempt.AttemptType type, OtpAttempt.AttemptResult result) {
        OtpAttempt attempt = OtpAttempt.builder()
                .userId(userId)
                .ipAddress(ipAddress)
                .type(type)
                .result(result)
                .build();
        
        otpAttemptRepository.save(attempt);
    }
    
    private String hashOtp(String otp) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(otp.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    public boolean isOtpValid(String email) {
        String otpKey = OTP_KEY_PREFIX + email;
        return redisTemplate.hasKey(otpKey);
    }
}