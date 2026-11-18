package com.serviceconnect.dao;

import com.serviceconnect.model.ChatMessage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class ChatDao {

    private final JdbcTemplate jdbcTemplate;

    public ChatDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<ChatMessage> chatRowMapper = (rs, rowNum) -> {
        ChatMessage msg = new ChatMessage();
        msg.setId(rs.getLong("id"));
        msg.setBookingId(rs.getLong("booking_id"));
        msg.setSenderId(rs.getLong("sender_id"));
        msg.setSenderName(rs.getString("sender_name"));
        msg.setMessage(rs.getString("message"));
        msg.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        return msg;
    };

    public List<ChatMessage> findByBookingId(Long bookingId) {
        String sql = "SELECT * FROM chat_messages WHERE booking_id = ? ORDER BY created_at ASC";
        return jdbcTemplate.query(sql, chatRowMapper, bookingId);
    }

    public Long create(Long bookingId, Long senderId, String senderName, String message) {
        String sql = "INSERT INTO chat_messages (booking_id, sender_id, sender_name, message) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, bookingId);
            ps.setLong(2, senderId);
            ps.setString(3, senderName);
            ps.setString(4, message);
            return ps;
        }, keyHolder);

        return keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;
    }
}
