package com.serviceconnect.controller;

import com.serviceconnect.dto.ApiResponse;
import com.serviceconnect.model.ProviderProfile;
import com.serviceconnect.service.ProviderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/providers")
public class ProviderController {

    private final ProviderService providerService;

    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProviderProfile>>> searchProviders(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false, defaultValue = "rating_desc") String sortBy) {

        List<ProviderProfile> providers = providerService.searchProviders(category, city, maxPrice, minRating, sortBy);
        return ResponseEntity.ok(ApiResponse.ok(providers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProviderProfile(@PathVariable Long id) {
        Map<String, Object> profile = providerService.getProviderDetails(id);
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProviderByUserId(@PathVariable Long userId) {
        Map<String, Object> profile = providerService.getProviderDetailsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }
}
