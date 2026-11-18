package com.serviceconnect.dao;

import com.serviceconnect.model.Booking;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class BookingDao {

    private final JdbcTemplate jdbcTemplate;

    public BookingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Booking> bookingRowMapper = (rs, rowNum) -> {
        Booking b = new Booking();
        b.setId(rs.getLong("id"));
        b.setBookingReference(rs.getString("booking_reference"));
        b.setCustomerId(rs.getLong("customer_id"));
        b.setCustomerName(rs.getString("customer_name"));
        b.setCustomerPhone(rs.getString("customer_phone"));
        b.setProviderId(rs.getLong("provider_id"));
        b.setProviderName(rs.getString("provider_name"));
        b.setProviderBusiness(rs.getString("provider_business"));
        b.setServiceId(rs.getLong("service_id"));
        b.setServiceName(rs.getString("service_name"));
        b.setBookingDate(rs.getDate("booking_date") != null ? rs.getDate("booking_date").toLocalDate() : null);
        b.setTimeSlot(rs.getString("time_slot"));
        b.setStatus(rs.getString("status"));
        b.setCustomerAddress(rs.getString("customer_address"));
        b.setProblemDescription(rs.getString("problem_description"));
        b.setPrice(rs.getBigDecimal("price"));
        b.setCancellationReason(rs.getString("cancellation_reason"));
        b.setHasReviewed(rs.getInt("review_count") > 0);
        b.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        return b;
    };

    public boolean isSlotBooked(Long providerId, LocalDate date, String timeSlot) {
        String sql = """
            SELECT COUNT(*) FROM bookings 
            WHERE provider_id = ? AND booking_date = ? AND time_slot = ? 
            AND status NOT IN ('CANCELLED', 'REJECTED')
        """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, providerId, Date.valueOf(date), timeSlot);
        return count != null && count > 0;
    }

    public Long create(Booking b) {
        String sql = """
            INSERT INTO bookings (booking_reference, customer_id, provider_id, service_id, booking_date, 
                time_slot, status, customer_name, customer_phone, customer_address, problem_description, price)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, b.getBookingReference());
            ps.setLong(2, b.getCustomerId());
            ps.setLong(3, b.getProviderId());
            if (b.getServiceId() != null) ps.setLong(4, b.getServiceId()); else ps.setNull(4, java.sql.Types.BIGINT);
            ps.setDate(5, Date.valueOf(b.getBookingDate()));
            ps.setString(6, b.getTimeSlot());
            ps.setString(7, b.getStatus() != null ? b.getStatus() : "PENDING");
            ps.setString(8, b.getCustomerName());
            ps.setString(9, b.getCustomerPhone());
            ps.setString(10, b.getCustomerAddress());
            ps.setString(11, b.getProblemDescription());
            ps.setBigDecimal(12, b.getPrice());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    public Optional<Booking> findById(Long id) {
        String sql = """
            SELECT b.*, u.full_name AS provider_name, p.business_name AS provider_business,
                   COALESCE(s.name, 'Standard Service') AS service_name,
                   (SELECT COUNT(*) FROM reviews r WHERE r.booking_id = b.id) AS review_count
            FROM bookings b
            JOIN provider_profiles p ON b.provider_id = p.id
            JOIN users u ON p.user_id = u.id
            LEFT JOIN services s ON b.service_id = s.id
            WHERE b.id = ?
        """;
        List<Booking> list = jdbcTemplate.query(sql, bookingRowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<Booking> findByCustomer(Long customerId) {
        String sql = """
            SELECT b.*, u.full_name AS provider_name, p.business_name AS provider_business,
                   COALESCE(s.name, 'Standard Service') AS service_name,
                   (SELECT COUNT(*) FROM reviews r WHERE r.booking_id = b.id) AS review_count
            FROM bookings b
            JOIN provider_profiles p ON b.provider_id = p.id
            JOIN users u ON p.user_id = u.id
            LEFT JOIN services s ON b.service_id = s.id
            WHERE b.customer_id = ?
            ORDER BY b.booking_date DESC, b.created_at DESC
        """;
        return jdbcTemplate.query(sql, bookingRowMapper, customerId);
    }

    public List<Booking> findByProvider(Long providerId) {
        String sql = """
            SELECT b.*, u.full_name AS provider_name, p.business_name AS provider_business,
                   COALESCE(s.name, 'Standard Service') AS service_name,
                   (SELECT COUNT(*) FROM reviews r WHERE r.booking_id = b.id) AS review_count
            FROM bookings b
            JOIN provider_profiles p ON b.provider_id = p.id
            JOIN users u ON p.user_id = u.id
            LEFT JOIN services s ON b.service_id = s.id
            WHERE b.provider_id = ?
            ORDER BY b.booking_date DESC, b.created_at DESC
        """;
        return jdbcTemplate.query(sql, bookingRowMapper, providerId);
    }

    public List<Booking> findAll(String status) {
        StringBuilder sql = new StringBuilder("""
            SELECT b.*, u.full_name AS provider_name, p.business_name AS provider_business,
                   COALESCE(s.name, 'Standard Service') AS service_name,
                   (SELECT COUNT(*) FROM reviews r WHERE r.booking_id = b.id) AS review_count
            FROM bookings b
            JOIN provider_profiles p ON b.provider_id = p.id
            JOIN users u ON p.user_id = u.id
            LEFT JOIN services s ON b.service_id = s.id
        """);

        List<Object> params = new ArrayList<>();
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" WHERE b.status = ?");
            params.add(status.toUpperCase());
        }
        sql.append(" ORDER BY b.created_at DESC");

        return jdbcTemplate.query(sql.toString(), bookingRowMapper, params.toArray());
    }

    public int updateStatus(Long id, String status, String cancellationReason) {
        String sql = "UPDATE bookings SET status = ?, cancellation_reason = ? WHERE id = ?";
        return jdbcTemplate.update(sql, status, cancellationReason, id);
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM bookings";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    public List<Map<String, Object>> getMonthlyStats() {
        String sql = """
            SELECT 
                DATE_FORMAT(booking_date, '%Y-%m') AS booking_month,
                COUNT(id) AS total_bookings,
                SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed_bookings,
                COALESCE(SUM(CASE WHEN status = 'COMPLETED' THEN price ELSE 0 END), 0) AS revenue
            FROM bookings
            GROUP BY DATE_FORMAT(booking_date, '%Y-%m')
            ORDER BY booking_month DESC
            LIMIT 6
        """;
        return jdbcTemplate.queryForList(sql);
    }
}
