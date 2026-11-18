package com.serviceconnect.controller;

import com.serviceconnect.dao.AddressDao;
import com.serviceconnect.dao.BookingDao;
import com.serviceconnect.dao.CategoryDao;
import com.serviceconnect.dao.ProviderDao;
import com.serviceconnect.dao.UserDao;
import com.serviceconnect.dto.ApiResponse;
import com.serviceconnect.model.CustomerAddress;
import com.serviceconnect.model.User;
import com.serviceconnect.service.ProviderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AdminController {

    private final UserDao userDao;
    private final ProviderDao providerDao;
    private final BookingDao bookingDao;
    private final CategoryDao categoryDao;
    private final AddressDao addressDao;
    private final ProviderService providerService;

    public AdminController(UserDao userDao, ProviderDao providerDao, BookingDao bookingDao,
                           CategoryDao categoryDao, AddressDao addressDao, ProviderService providerService) {
        this.userDao = userDao;
        this.providerDao = providerDao;
        this.bookingDao = bookingDao;
        this.categoryDao = categoryDao;
        this.addressDao = addressDao;
        this.providerService = providerService;
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCustomers", userDao.countByRole("CUSTOMER"));
        stats.put("totalProviders", providerDao.count());
        stats.put("totalServices", categoryDao.count());
        stats.put("totalBookings", bookingDao.count());
        stats.put("pendingApprovals", providerDao.countPending());
        stats.put("monthlyStats", bookingDao.getMonthlyStats());

        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @GetMapping("/admin/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userDao.findAll();
        // Do not expose password hashes in API responses
        users.forEach(u -> u.setPasswordHash(null));
        return ResponseEntity.ok(ApiResponse.ok(users));
    }

    @PutMapping("/admin/users/{id}/status")
    public ResponseEntity<ApiResponse<String>> updateUserStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        userDao.updateStatus(id, status.toUpperCase());
        return ResponseEntity.ok(ApiResponse.ok("User status updated to " + status, status));
    }

    @PutMapping("/admin/providers/{id}/approve")
    public ResponseEntity<ApiResponse<String>> approveProvider(@PathVariable Long id) {
        providerService.approveProvider(id);
        return ResponseEntity.ok(ApiResponse.ok("Provider approved successfully", "APPROVED"));
    }

    @PutMapping("/admin/providers/{id}/reject")
    public ResponseEntity<ApiResponse<String>> rejectProvider(@PathVariable Long id) {
        providerService.rejectProvider(id);
        return ResponseEntity.ok(ApiResponse.ok("Provider verification rejected", "REJECTED"));
    }

    @GetMapping("/customer/addresses")
    public ResponseEntity<ApiResponse<List<CustomerAddress>>> getCustomerAddresses(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long customerId) {
        List<CustomerAddress> addresses = addressDao.findByUser(customerId);
        return ResponseEntity.ok(ApiResponse.ok(addresses));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("platform", "ServiceConnect");
        health.put("database", "MySQL 8.0");
        return ResponseEntity.ok(ApiResponse.ok("ServiceConnect API is operational", health));
    }
}
