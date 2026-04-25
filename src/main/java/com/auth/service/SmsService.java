package com.auth.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;

@Service
@Slf4j
public class SmsService {
    
    @Value("${app.twilio.account-sid:}")
    private String accountSid;
    
    @Value("${app.twilio.auth-token:}")
    private String authToken;
    
    @Value("${app.twilio.phone-number:}")
    private String fromPhoneNumber;
    
    @Value("${app.sms.enabled:false}")
    private boolean smsEnabled;
    
    @PostConstruct
    public void init() {
        if (smsEnabled && StringUtils.hasText(accountSid) && StringUtils.hasText(authToken)) {
            Twilio.init(accountSid, authToken);
            log.info("Twilio SMS service initialized");
        } else {
            log.warn("SMS service is disabled or not configured properly");
        }
    }
    
    public void sendSms(String toPhoneNumber, String messageBody) {
        if (!smsEnabled) {
            log.info("SMS service is disabled. Would send SMS to {}: {}", toPhoneNumber, messageBody);
            return;
        }
        
        if (!StringUtils.hasText(accountSid) || !StringUtils.hasText(authToken) || !StringUtils.hasText(fromPhoneNumber)) {
            log.error("SMS service is not properly configured");
            throw new RuntimeException("SMS service configuration is incomplete");
        }
        
        try {
            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(fromPhoneNumber),
                    messageBody
            ).create();
            
            log.info("SMS sent successfully. SID: {}", message.getSid());
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", toPhoneNumber, e.getMessage(), e);
            throw new RuntimeException("Failed to send SMS", e);
        }
    }
}