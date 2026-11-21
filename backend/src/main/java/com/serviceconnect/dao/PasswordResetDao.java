package com.serviceconnect.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Repository
public class PasswordResetDao {

    private final JdbcTemplate jdbcTemplate;

    public PasswordResetDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(Long userId, String token, LocalDateTime expiresAt) {
        jdbcTemplate.update("DELETE FROM password_resets WHERE user_id = ?", userId);
        String sql = "INSERT INTO password_resets (user_id, token, expires_at) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userId, token, Timestamp.valueOf(expiresAt));
    }

    public Optional<Map<String, Object>> findByToken(String token) {
        String sql = "SELECT * FROM password_resets WHERE token = ? AND is_used = FALSE";
        var list = jdbcTemplate.queryForList(sql, token);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public void markUsed(String token) {
        jdbcTemplate.update("UPDATE password_resets SET is_used = TRUE WHERE token = ?", token);
    }
}
