package com.serviceconnect.dao;

import com.serviceconnect.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setPhone(rs.getString("phone"));
        u.setRole(rs.getString("role"));
        u.setStatus(rs.getString("status"));
        try {
            u.setEmailVerified(rs.getBoolean("email_verified"));
        } catch (Exception ignored) {}
        u.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        u.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        return u;
    };

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        List<User> list = jdbcTemplate.query(sql, userRowMapper, email);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> list = jdbcTemplate.query(sql, userRowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Long create(User user) {
        String sql = "INSERT INTO users (email, password_hash, full_name, phone, role, status, email_verified) VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getRole());
            ps.setString(6, user.getStatus() != null ? user.getStatus() : "PENDING_VERIFICATION");
            ps.setBoolean(7, user.isEmailVerified());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    public int verifyEmail(Long userId) {
        String sql = "UPDATE users SET email_verified = TRUE, status = 'ACTIVE' WHERE id = ?";
        return jdbcTemplate.update(sql, userId);
    }

    public int updatePassword(Long userId, String passwordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        return jdbcTemplate.update(sql, passwordHash, userId);
    }

    public int updateStatus(Long id, String status) {
        String sql = "UPDATE users SET status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, status, id);
    }

    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    public int countByRole(String role) {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, role);
        return count != null ? count : 0;
    }
}
