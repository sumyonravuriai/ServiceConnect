package com.serviceconnect.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class JwtUtil {

    private final String secretKey;
    private final long expirationMillis;

    public JwtUtil(
            @Value("${jwt.secret:ServiceConnect2026SecureJwtSecretKeyWithHighEntropyAtLeast256BitsPlatform}") String secretKey,
            @Value("${jwt.expiration.hours:24}") long expirationHours) {
        this.secretKey = secretKey;
        this.expirationMillis = expirationHours * 3600 * 1000;
    }

    public String generateToken(Long userId, String email, String role, String fullName) {
        long now = System.currentTimeMillis();
        long exp = now + expirationMillis;

        String header = Base64.getUrlEncoder().withoutPadding().encodeToString(
                "{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8)
        );

        String payloadJson = String.format(
                "{\"sub\":\"%d\",\"email\":\"%s\",\"role\":\"%s\",\"name\":\"%s\",\"iat\":%d,\"exp\":%d}",
                userId, escape(email), escape(role), escape(fullName != null ? fullName : ""), now / 1000, exp / 1000
        );
        String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(
                payloadJson.getBytes(StandardCharsets.UTF_8)
        );

        String signature = sign(header + "." + payload, secretKey);
        return header + "." + payload + "." + signature;
    }

    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) return false;
        String[] parts = token.split("\\.");
        if (parts.length != 3) return false;

        String expectedSignature = sign(parts[0] + "." + parts[1], secretKey);
        if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
            return false;
        }

        try {
            long expSeconds = getClaimLong(parts[1], "exp");
            return (expSeconds * 1000) > System.currentTimeMillis();
        } catch (Exception e) {
            return false;
        }
    }

    public Long getUserId(String token) {
        String payload = getPayloadPart(token);
        String sub = getClaimString(payload, "sub");
        return sub != null ? Long.parseLong(sub) : null;
    }

    public String getEmail(String token) {
        String payload = getPayloadPart(token);
        return getClaimString(payload, "email");
    }

    public String getRole(String token) {
        String payload = getPayloadPart(token);
        return getClaimString(payload, "role");
    }

    private String getPayloadPart(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) throw new IllegalArgumentException("Malformed JWT token");
        return parts[1];
    }

    private String sign(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC-SHA256 signature", e);
        }
    }

    private String getClaimString(String base64Payload, String claimName) {
        try {
            String json = new String(Base64.getUrlDecoder().decode(base64Payload), StandardCharsets.UTF_8);
            String search = "\"" + claimName + "\":\"";
            int idx = json.indexOf(search);
            if (idx == -1) return null;
            int start = idx + search.length();
            int end = json.indexOf("\"", start);
            return end != -1 ? json.substring(start, end) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private long getClaimLong(String base64Payload, String claimName) {
        String json = new String(Base64.getUrlDecoder().decode(base64Payload), StandardCharsets.UTF_8);
        String search = "\"" + claimName + "\":";
        int idx = json.indexOf(search);
        if (idx == -1) throw new IllegalArgumentException("Missing claim " + claimName);
        int start = idx + search.length();
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
            end++;
        }
        return Long.parseLong(json.substring(start, end).trim());
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
