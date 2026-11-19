package com.serviceconnect.controller;

import com.serviceconnect.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> details = new HashMap<>();
        details.put("status", "UP");
        details.put("platform", "ServiceConnect");

        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            details.put("database", "CONNECTED (MySQL 8.0)");
            details.put("dbPing", result != null && result == 1 ? "OK" : "FAILED");
        } catch (Exception ex) {
            details.put("database", "DISCONNECTED: " + ex.getMessage());
        }

        return ResponseEntity.ok(ApiResponse.ok("ServiceConnect API is operational", details));
    }
}
