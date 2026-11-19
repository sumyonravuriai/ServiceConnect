package com.serviceconnect.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class AuditDao {

    private final JdbcTemplate jdbcTemplate;

    public AuditDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(Long adminId, String action, String targetType, Long targetId, String details) {
        String sql = "INSERT INTO audit_logs (admin_id, action, target_type, target_id, details) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, adminId, action, targetType, targetId, details);
    }

    public List<Map<String, Object>> findRecent(int limit) {
        String sql = """
            SELECT a.*, u.full_name AS admin_name, u.email AS admin_email
            FROM audit_logs a
            JOIN users u ON a.admin_id = u.id
            ORDER BY a.created_at DESC
            LIMIT ?
        """;
        return jdbcTemplate.queryForList(sql, limit);
    }
}
