package com.serviceconnect.dao;

import com.serviceconnect.model.Review;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReviewDao {

    private final JdbcTemplate jdbcTemplate;

    public ReviewDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Review> reviewRowMapper = (rs, rowNum) -> {
        Review r = new Review();
        r.setId(rs.getLong("id"));
        r.setBookingId(rs.getLong("booking_id"));
        r.setCustomerId(rs.getLong("customer_id"));
        r.setCustomerName(rs.getString("customer_name"));
        r.setProviderId(rs.getLong("provider_id"));
        r.setRating(rs.getInt("rating"));
        r.setComment(rs.getString("comment"));
        r.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        return r;
    };

    public boolean existsByBooking(Long bookingId) {
        String sql = "SELECT COUNT(*) FROM reviews WHERE booking_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, bookingId);
        return count != null && count > 0;
    }

    public int create(Review r) {
        String sql = "INSERT INTO reviews (booking_id, customer_id, provider_id, rating, comment) VALUES (?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql, r.getBookingId(), r.getCustomerId(), r.getProviderId(), r.getRating(), r.getComment());
    }

    public List<Review> findByProvider(Long providerId) {
        String sql = """
            SELECT r.*, u.full_name AS customer_name
            FROM reviews r
            JOIN users u ON r.customer_id = u.id
            WHERE r.provider_id = ?
            ORDER BY r.created_at DESC
        """;
        return jdbcTemplate.query(sql, reviewRowMapper, providerId);
    }

    public List<Review> findAll() {
        String sql = """
            SELECT r.*, u.full_name AS customer_name
            FROM reviews r
            JOIN users u ON r.customer_id = u.id
            ORDER BY r.created_at DESC
        """;
        return jdbcTemplate.query(sql, reviewRowMapper);
    }

    public java.util.Optional<Review> findById(Long id) {
        String sql = """
            SELECT r.*, u.full_name AS customer_name
            FROM reviews r
            JOIN users u ON r.customer_id = u.id
            WHERE r.id = ?
        """;
        var list = jdbcTemplate.query(sql, reviewRowMapper, id);
        return list.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(list.get(0));
    }

    public int deleteById(Long id) {
        String sql = "DELETE FROM reviews WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
