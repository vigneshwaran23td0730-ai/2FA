package com.auth.service;

import com.auth.entity.OtpAttempt;
import com.auth.entity.User;
import com.auth.exception.InvalidTotpException;
import com.auth.repository.OtpAttemptRepository;
import com.auth.util.QrCodeGenerator;
import com.google.zxing.WriterException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import org.apache.commons.codec.binary.Base32;

@Service
@RequiredArgsConstructor
@Slf4j
public class TotpService {
    
    private final OtpAttemptRepository otpAttemptRepository;
    
    @Value("${app.totp.issuer:SecureAuth}")
    private String issuer;
    
    @Value("${app.totp.period:30}")
    private int period;
    
    @Value("${app.totp.digits:6}")
    private int digits;
    
    private static final String HMAC_ALGORITHM = "HmacSHA1";
    private static final Base32 base32 = new Base32();
    
    public String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20]; // 160 bits
        random.nextBytes(bytes);
        return base32.encodeToString(bytes);
    }
    
    public String generateQrCodeUrl(String email, String secret) {
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=%d&period=%d",
                issuer, email, secret, issuer, digits, period
        );
    }
    
    public String generateQrCodeImage(String qrCodeUrl) throws WriterException, IOException {
        return QrCodeGenerator.generateQrCodeImage(qrCodeUrl, 200, 200);
    }
    
    public boolean verifyTotp(String secret, String totpCode, User user, String ipAddress) {
        try {
            long currentTimeSlot = Instant.now().getEpochSecond() / period;
            
            // Check current time slot and adjacent slots for clock skew tolerance
            for (int i = -1; i <= 1; i++) {
                String expectedCode = generateTotpCode(secret, currentTimeSlot + i);
                if (totpCode.equals(expectedCode)) {
                    recordAttempt(user.getId(), ipAddress, OtpAttempt.AttemptType.TOTP_VERIFY, OtpAttempt.AttemptResult.SUCCESS);
                    log.info("TOTP verified successfully for user: {}", user.getEmail());
                    return true;
                }
            }
            
            recordAttempt(user.getId(), ipAddress, OtpAttempt.AttemptType.TOTP_VERIFY, OtpAttempt.AttemptResult.FAILURE);
            log.warn("Invalid TOTP attempt for user: {}", user.getEmail());
            return false;
            
        } catch (Exception e) {
            log.error("Error verifying TOTP for user: {}", user.getEmail(), e);
            recordAttempt(user.getId(), ipAddress, OtpAttempt.AttemptType.TOTP_VERIFY, OtpAttempt.AttemptResult.FAILURE);
            throw new InvalidTotpException("Failed to verify TOTP code");
        }
    }
    
    private String generateTotpCode(String secret, long timeSlot) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] secretBytes = base32.decode(secret);
        byte[] timeBytes = ByteBuffer.allocate(8).putLong(timeSlot).array();
        
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(secretBytes, HMAC_ALGORITHM);
        mac.init(keySpec);
        
        byte[] hash = mac.doFinal(timeBytes);
        
        int offset = hash[hash.length - 1] & 0x0F;
        int truncatedHash = ((hash[offset] & 0x7F) << 24) |
                           ((hash[offset + 1] & 0xFF) << 16) |
                           ((hash[offset + 2] & 0xFF) << 8) |
                           (hash[offset + 3] & 0xFF);
        
        int code = truncatedHash % (int) Math.pow(10, digits);
        return String.format("%0" + digits + "d", code);
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
}