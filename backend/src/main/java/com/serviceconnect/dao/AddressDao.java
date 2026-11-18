package com.serviceconnect.dao;

import com.serviceconnect.model.CustomerAddress;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AddressDao {

    private final JdbcTemplate jdbcTemplate;

    public AddressDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<CustomerAddress> addressRowMapper = (rs, rowNum) -> {
        CustomerAddress a = new CustomerAddress();
        a.setId(rs.getLong("id"));
        a.setUserId(rs.getLong("user_id"));
        a.setLabel(rs.getString("label"));
        a.setAddressLine1(rs.getString("address_line1"));
        a.setAddressLine2(rs.getString("address_line2"));
        a.setCity(rs.getString("city"));
        a.setState(rs.getString("state"));
        a.setPostalCode(rs.getString("postal_code"));
        a.setDefault(rs.getBoolean("is_default"));
        return a;
    };

    public List<CustomerAddress> findByUser(Long userId) {
        String sql = "SELECT * FROM customer_addresses WHERE user_id = ? ORDER BY is_default DESC, id DESC";
        return jdbcTemplate.query(sql, addressRowMapper, userId);
    }

    public int create(CustomerAddress a) {
        String sql = """
            INSERT INTO customer_addresses (user_id, label, address_line1, address_line2, city, state, postal_code, is_default)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        return jdbcTemplate.update(sql, a.getUserId(), a.getLabel(), a.getAddressLine1(), a.getAddressLine2(),
                a.getCity(), a.getState(), a.getPostalCode(), a.isDefault());
    }
}
