package com.serviceconnect.dao;

import com.serviceconnect.model.ProviderProfile;
import com.serviceconnect.model.ProviderCustomService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ProviderDao {

    private final JdbcTemplate jdbcTemplate;

    public ProviderDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<ProviderProfile> providerRowMapper = (rs, rowNum) -> {
        ProviderProfile p = new ProviderProfile();
        p.setId(rs.getLong("id"));
        p.setUserId(rs.getLong("user_id"));
        p.setBusinessName(rs.getString("business_name"));
        p.setCategoryId(rs.getLong("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        p.setProviderName(rs.getString("provider_name"));
        p.setPhone(rs.getString("phone"));
        p.setBio(rs.getString("bio"));
        p.setExperienceYears(rs.getInt("experience_years"));
        p.setServiceArea(rs.getString("service_area"));
        p.setProfileImage(rs.getString("profile_image"));
        p.setVerified(rs.getBoolean("is_verified"));
        p.setAvailable(rs.getBoolean("is_available"));
        p.setRatingAvg(rs.getBigDecimal("rating_avg"));
        p.setRatingCount(rs.getInt("rating_count"));
        p.setStartingPrice(rs.getBigDecimal("starting_price"));
        return p;
    };

    public List<ProviderProfile> search(String category, String city, BigDecimal maxPrice, Double minRating, String sortBy) {
        StringBuilder sql = new StringBuilder("""
            SELECT p.*, sc.name AS category_name, u.full_name AS provider_name, u.phone
            FROM provider_profiles p
            JOIN users u ON p.user_id = u.id
            JOIN service_categories sc ON p.category_id = sc.id
            WHERE u.status = 'ACTIVE'
        """);

        List<Object> params = new ArrayList<>();

        if (category != null && !category.isBlank()) {
            sql.append(" AND sc.name LIKE ?");
            params.add("%" + category.trim() + "%");
        }
        if (city != null && !city.isBlank()) {
            sql.append(" AND (p.service_area LIKE ?)");
            params.add("%" + city.trim() + "%");
        }
        if (maxPrice != null) {
            sql.append(" AND p.starting_price <= ?");
            params.add(maxPrice);
        }
        if (minRating != null) {
            sql.append(" AND p.rating_avg >= ?");
            params.add(minRating);
        }

        if ("price_asc".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY p.starting_price ASC");
        } else if ("price_desc".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY p.starting_price DESC");
        } else {
            sql.append(" ORDER BY p.rating_avg DESC, p.rating_count DESC");
        }

        return jdbcTemplate.query(sql.toString(), providerRowMapper, params.toArray());
    }

    public Optional<ProviderProfile> findById(Long id) {
        String sql = """
            SELECT p.*, sc.name AS category_name, u.full_name AS provider_name, u.phone
            FROM provider_profiles p
            JOIN users u ON p.user_id = u.id
            JOIN service_categories sc ON p.category_id = sc.id
            WHERE p.id = ?
        """;
        List<ProviderProfile> list = jdbcTemplate.query(sql, providerRowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<ProviderProfile> findByUserId(Long userId) {
        String sql = """
            SELECT p.*, sc.name AS category_name, u.full_name AS provider_name, u.phone
            FROM provider_profiles p
            JOIN users u ON p.user_id = u.id
            JOIN service_categories sc ON p.category_id = sc.id
            WHERE p.user_id = ?
        """;
        List<ProviderProfile> list = jdbcTemplate.query(sql, providerRowMapper, userId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Long create(ProviderProfile profile) {
        String sql = """
            INSERT INTO provider_profiles (user_id, business_name, category_id, bio, experience_years, 
                service_area, profile_image, is_verified, is_available, rating_avg, rating_count, starting_price)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, profile.getUserId());
            ps.setString(2, profile.getBusinessName());
            ps.setLong(3, profile.getCategoryId());
            ps.setString(4, profile.getBio());
            ps.setInt(5, profile.getExperienceYears() != null ? profile.getExperienceYears() : 1);
            ps.setString(6, profile.getServiceArea());
            ps.setString(7, profile.getProfileImage());
            ps.setBoolean(8, profile.isVerified());
            ps.setBoolean(9, profile.isAvailable());
            ps.setBigDecimal(10, BigDecimal.ZERO);
            ps.setInt(11, 0);
            ps.setBigDecimal(12, profile.getStartingPrice() != null ? profile.getStartingPrice() : BigDecimal.valueOf(350));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    public int updateVerification(Long id, boolean isVerified) {
        String sql = "UPDATE provider_profiles SET is_verified = ? WHERE id = ?";
        return jdbcTemplate.update(sql, isVerified, id);
    }

    public void updateRatings(Long providerId) {
        String sql = """
            UPDATE provider_profiles p
            SET 
                rating_count = (SELECT COUNT(*) FROM reviews WHERE provider_id = ?),
                rating_avg = COALESCE((SELECT ROUND(AVG(rating), 2) FROM reviews WHERE provider_id = ?), 0.00)
            WHERE p.id = ?
        """;
        jdbcTemplate.update(sql, providerId, providerId, providerId);
    }

    public List<ProviderCustomService> getProviderServices(Long providerId) {
        String sql = """
            SELECT ps.id, ps.provider_id, ps.service_id, s.name AS service_name, s.estimated_duration,
                   ps.custom_price, ps.custom_description, ps.is_available
            FROM provider_services ps
            JOIN services s ON ps.service_id = s.id
            WHERE ps.provider_id = ? AND ps.is_available = TRUE
        """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ProviderCustomService ps = new ProviderCustomService();
            ps.setId(rs.getLong("id"));
            ps.setProviderId(rs.getLong("provider_id"));
            ps.setServiceId(rs.getLong("service_id"));
            ps.setServiceName(rs.getString("service_name"));
            ps.setDuration(rs.getString("estimated_duration"));
            ps.setCustomPrice(rs.getBigDecimal("custom_price"));
            ps.setCustomDescription(rs.getString("custom_description"));
            ps.setAvailable(rs.getBoolean("is_available"));
            return ps;
        }, providerId);
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM provider_profiles";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    public int countPending() {
        String sql = "SELECT COUNT(*) FROM provider_profiles WHERE is_verified = FALSE";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }
}
