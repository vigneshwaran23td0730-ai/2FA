package com.auth.service;

import com.auth.dto.OtpRequest;
import com.auth.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final JavaMailSender mailSender;
    private final SmsService smsService;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.name:SecureAuth}")
    private String appName;
    
    @Async
    public void sendOtp(User user, String otp, OtpRequest.DeliveryMethod deliveryMethod) {
        switch (deliveryMethod) {
            case EMAIL -> sendOtpByEmail(user, otp);
            case SMS -> sendOtpBySms(user, otp);
            case BOTH -> {
                sendOtpByEmail(user, otp);
                if (StringUtils.hasText(user.getPhone())) {
                    sendOtpBySms(user, otp);
                }
            }
        }
    }
    
    private void sendOtpByEmail(User user, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject(appName + " - Your Verification Code");
            message.setText(buildEmailContent(user.getName(), otp));
            
            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", user.getEmail(), e);
        }
    }
    
    private void sendOtpBySms(User user, String otp) {
        if (!StringUtils.hasText(user.getPhone())) {
            log.warn("Cannot send SMS OTP to user {} - no phone number", user.getEmail());
            return;
        }
        
        try {
            String message = buildSmsContent(otp);
            smsService.sendSms(user.getPhone(), message);
            log.info("OTP SMS sent successfully to: {}", user.getPhone());
        } catch (Exception e) {
            log.error("Failed to send OTP SMS to: {}", user.getPhone(), e);
        }
    }
    
    private String buildEmailContent(String name, String otp) {
        return String.format("""
                Hello %s,
                
                Your verification code for %s is: %s
                
                This code will expire in 5 minutes. Please do not share this code with anyone.
                
                If you did not request this code, please ignore this email.
                
                Best regards,
                %s Team
                """, name, appName, otp, appName);
    }
    
    private String buildSmsContent(String otp) {
        return String.format("Your %s verification code is: %s. Valid for 5 minutes.", appName, otp);
    }
}