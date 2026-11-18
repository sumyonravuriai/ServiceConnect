package com.serviceconnect.controller;

import com.serviceconnect.dto.ApiResponse;
import com.serviceconnect.dto.AuthRequest;
import com.serviceconnect.dto.CustomerRegisterRequest;
import com.serviceconnect.dto.ProviderRegisterRequest;
import com.serviceconnect.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @RequestBody AuthRequest request) {
        Map<String, Object> data = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", data));
    }

    @PostMapping("/register/customer")
    public ResponseEntity<ApiResponse<Map<String, Object>>> registerCustomer(@Valid @RequestBody CustomerRegisterRequest request) {
        Map<String, Object> data = authService.registerCustomer(request);
        return ResponseEntity.ok(ApiResponse.ok("Customer registered successfully", data));
    }

    @PostMapping("/register/provider")
    public ResponseEntity<ApiResponse<Map<String, Object>>> registerProvider(@Valid @RequestBody ProviderRegisterRequest request) {
        Map<String, Object> data = authService.registerProvider(request);
        return ResponseEntity.ok(ApiResponse.ok("Provider registered successfully. Profile is pending admin verification.", data));
    }
}
