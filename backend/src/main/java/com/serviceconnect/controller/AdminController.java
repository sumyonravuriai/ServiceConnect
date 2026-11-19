package com.serviceconnect.controller;

import com.serviceconnect.dao.*;
import com.serviceconnect.dto.ApiResponse;
import com.serviceconnect.model.CustomerAddress;
import com.serviceconnect.model.ProviderProfile;
import com.serviceconnect.model.Review;
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
    private final ReviewDao reviewDao;
    private final AuditDao auditDao;
    private final ProviderService providerService;

    public AdminController(UserDao userDao, ProviderDao providerDao, BookingDao bookingDao,
                           CategoryDao categoryDao, AddressDao addressDao, ReviewDao reviewDao,
                           AuditDao auditDao, ProviderService providerService) {
        this.userDao = userDao;
        this.providerDao = providerDao;
        this.bookingDao = bookingDao;
        this.categoryDao = categoryDao;
        this.addressDao = addressDao;
        this.reviewDao = reviewDao;
        this.auditDao = auditDao;
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
            @RequestParam String status,
            @RequestAttribute(value = "authenticatedUserId", required = false) Long adminId) {
        userDao.updateStatus(id, status.toUpperCase());
        auditDao.log(adminId != null ? adminId : 1L, "UPDATE_USER_STATUS", "USER", id, "Changed user status to " + status);
        return ResponseEntity.ok(ApiResponse.ok("User status updated to " + status, status));
    }

    @GetMapping("/admin/providers/pending")
    public ResponseEntity<ApiResponse<List<ProviderProfile>>> getPendingProviders() {
        List<ProviderProfile> list = providerService.getPendingProviders();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PutMapping("/admin/providers/{id}/approve")
    public ResponseEntity<ApiResponse<String>> approveProvider(
            @PathVariable Long id,
            @RequestAttribute(value = "authenticatedUserId", required = false) Long adminId) {
        providerService.approveProvider(id, adminId != null ? adminId : 1L);
        return ResponseEntity.ok(ApiResponse.ok("Provider approved successfully. Notification dispatched.", "APPROVED"));
    }

    @PutMapping("/admin/providers/{id}/reject")
    public ResponseEntity<ApiResponse<String>> rejectProvider(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            @RequestAttribute(value = "authenticatedUserId", required = false) Long adminId) {
        String reason = body != null && body.containsKey("reason") ? body.get("reason") : "Documentation requirements not met.";
        providerService.rejectProvider(id, reason, adminId != null ? adminId : 1L);
        return ResponseEntity.ok(ApiResponse.ok("Provider verification rejected. Notification dispatched.", "REJECTED"));
    }

    @GetMapping("/admin/reviews")
    public ResponseEntity<ApiResponse<List<Review>>> getAllReviews() {
        List<Review> reviews = reviewDao.findAll();
        return ResponseEntity.ok(ApiResponse.ok(reviews));
    }

    @DeleteMapping("/admin/reviews/{id}")
    public ResponseEntity<ApiResponse<String>> deleteReview(
            @PathVariable Long id,
            @RequestAttribute(value = "authenticatedUserId", required = false) Long adminId) {
        reviewDao.findById(id).ifPresent(r -> {
            reviewDao.deleteById(id);
            providerDao.updateRatings(r.getProviderId());
            auditDao.log(adminId != null ? adminId : 1L, "DELETE_REVIEW", "REVIEW", id, "Moderated review for provider " + r.getProviderId());
        });
        return ResponseEntity.ok(ApiResponse.ok("Review removed successfully", "DELETED"));
    }

    @GetMapping("/admin/audit-logs")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAuditLogs() {
        List<Map<String, Object>> logs = auditDao.findRecent(50);
        return ResponseEntity.ok(ApiResponse.ok(logs));
    }

    @GetMapping("/customer/addresses")
    public ResponseEntity<ApiResponse<List<CustomerAddress>>> getCustomerAddresses(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long customerId) {
        List<CustomerAddress> addresses = addressDao.findByUser(customerId);
        return ResponseEntity.ok(ApiResponse.ok(addresses));
    }
}
