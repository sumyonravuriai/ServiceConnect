package com.serviceconnect.controller;

import com.serviceconnect.dto.*;
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
        return ResponseEntity.ok(ApiResponse.ok("Provider registered successfully. Verification required.", data));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        Map<String, Object> data = authService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.ok("Email verified successfully", data));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resendVerification(@RequestParam String email) {
        Map<String, Object> data = authService.resendVerificationCode(email);
        return ResponseEntity.ok(ApiResponse.ok("Verification code dispatched successfully", data));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Map<String, Object>>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        Map<String, Object> data = authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Password reset request processed", data));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        Map<String, Object> data = authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Password reset successfully", data));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully", "Session cleared"));
    }
}
