package com.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_attempts", indexes = {
    @Index(name = "idx_user_id_created", columnList = "user_id, created_at"),
    @Index(name = "idx_ip_created", columnList = "ip_address, created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpAttempt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttemptType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttemptResult result;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public enum AttemptType {
        OTP_REQUEST, OTP_VERIFY, TOTP_VERIFY
    }
    
    public enum AttemptResult {
        SUCCESS, FAILURE, RATE_LIMITED
    }
}