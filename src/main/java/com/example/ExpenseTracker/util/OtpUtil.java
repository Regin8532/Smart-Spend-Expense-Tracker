package com.example.ExpenseTracker.util;

import java.security.SecureRandom;

public class OtpUtil {
    private static final SecureRandom random = new SecureRandom();
    public static String generate6DigitOtp() {
        int n = 100000 + random.nextInt(900000);
        return String.valueOf(n);
    }
}
