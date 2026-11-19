package com.serviceconnect.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Repository
public class EmailVerificationDao {

    private final JdbcTemplate jdbcTemplate;

    public EmailVerificationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(Long userId, String token, String otpCode, LocalDateTime expiresAt) {
        // Delete any prior active verification requests for this user
        jdbcTemplate.update("DELETE FROM email_verifications WHERE user_id = ?", userId);
        String sql = "INSERT INTO email_verifications (user_id, token, otp_code, expires_at) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, userId, token, otpCode, Timestamp.valueOf(expiresAt));
    }

    public Optional<Map<String, Object>> findByToken(String token) {
        String sql = "SELECT * FROM email_verifications WHERE token = ? AND expires_at > NOW()";
        var list = jdbcTemplate.queryForList(sql, token);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<Map<String, Object>> findByUserIdAndOtp(Long userId, String otpCode) {
        String sql = "SELECT * FROM email_verifications WHERE user_id = ? AND otp_code = ? AND expires_at > NOW()";
        var list = jdbcTemplate.queryForList(sql, userId, otpCode);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public void deleteByUserId(Long userId) {
        jdbcTemplate.update("DELETE FROM email_verifications WHERE user_id = ?", userId);
    }
}
