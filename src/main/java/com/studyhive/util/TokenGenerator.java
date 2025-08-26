package com.studyhive.util;

import java.security.SecureRandom;
import java.util.Base64;

public class TokenGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    // Strong secure token
    public static String generateSecureToken(int byteLength) {
        byte[] tokenBytes = new byte[byteLength];
        secureRandom.nextBytes(tokenBytes);
        return base64Encoder.encodeToString(tokenBytes);
    }
}