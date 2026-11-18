package com.serviceconnect.dao;

import com.serviceconnect.model.ServiceCategory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CategoryDao {

    private final JdbcTemplate jdbcTemplate;

    public CategoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<ServiceCategory> categoryRowMapper = (rs, rowNum) -> {
        ServiceCategory c = new ServiceCategory();
        c.setId(rs.getLong("id"));
        c.setName(rs.getString("name"));
        c.setSlug(rs.getString("slug"));
        c.setIconName(rs.getString("icon_name"));
        c.setDescription(rs.getString("description"));
        c.setActive(rs.getBoolean("is_active"));
        return c;
    };

    public List<ServiceCategory> findAllActive() {
        String sql = "SELECT * FROM service_categories WHERE is_active = TRUE ORDER BY name ASC";
        return jdbcTemplate.query(sql, categoryRowMapper);
    }

    public Optional<ServiceCategory> findById(Long id) {
        String sql = "SELECT * FROM service_categories WHERE id = ?";
        List<ServiceCategory> list = jdbcTemplate.query(sql, categoryRowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<ServiceCategory> findBySlug(String slug) {
        String sql = "SELECT * FROM service_categories WHERE slug = ?";
        List<ServiceCategory> list = jdbcTemplate.query(sql, categoryRowMapper, slug);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public int create(ServiceCategory cat) {
        String sql = "INSERT INTO service_categories (name, slug, icon_name, description, is_active) VALUES (?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql, cat.getName(), cat.getSlug(), cat.getIconName(), cat.getDescription(), cat.isActive());
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM service_categories";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }
}
