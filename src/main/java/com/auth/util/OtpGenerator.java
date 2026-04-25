package com.auth.util;

import java.security.SecureRandom;

public class OtpGenerator {
    
    private static final SecureRandom random = new SecureRandom();
    
    public static String generate(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("OTP length must be positive");
        }
        
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < length; i++) {
            otp.append(random.nextInt(10));
        }
        
        return otp.toString();
    }
}