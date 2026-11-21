package com.serviceconnect.service;

import com.serviceconnect.config.JwtUtil;
import com.serviceconnect.dao.*;
import com.serviceconnect.dto.*;
import com.serviceconnect.exception.BadRequestException;
import com.serviceconnect.exception.UnauthorizedException;
import com.serviceconnect.model.CustomerAddress;
import com.serviceconnect.model.ProviderProfile;
import com.serviceconnect.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
public class AuthService {

    private final UserDao userDao;
    private final ProviderDao providerDao;
    private final AddressDao addressDao;
    private final EmailVerificationDao emailVerificationDao;
    private final PasswordResetDao passwordResetDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public AuthService(
            UserDao userDao,
            ProviderDao providerDao,
            AddressDao addressDao,
            EmailVerificationDao emailVerificationDao,
            PasswordResetDao passwordResetDao,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            EmailService emailService) {
        this.userDao = userDao;
        this.providerDao = providerDao;
        this.addressDao = addressDao;
        this.emailVerificationDao = emailVerificationDao;
        this.passwordResetDao = passwordResetDao;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    public Map<String, Object> login(AuthRequest request) {
        String cleanEmail = request.getEmail().trim().toLowerCase();
        User user = userDao.findByEmail(cleanEmail)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        if (!user.isEmailVerified()) {
            throw new UnauthorizedException("Your email address is not verified yet. Please enter the verification code sent to your inbox to activate your account.");
        }

        if ("SUSPENDED".equalsIgnoreCase(user.getStatus())) {
            throw new UnauthorizedException("Your account has been suspended by system administrator. Please contact support.");
        }

        // Generate real RFC 7519 HMAC-SHA256 JWT
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole(), user.getFullName());

        Map<String, Object> authData = new HashMap<>();
        authData.put("token", token);

        Map<String, Object> userDto = new HashMap<>();
        userDto.put("id", user.getId());
        userDto.put("email", user.getEmail());
        userDto.put("fullName", user.getFullName());
        userDto.put("role", user.getRole());
        userDto.put("phone", user.getPhone());
        userDto.put("status", user.getStatus());
        userDto.put("emailVerified", user.isEmailVerified());

        if ("PROVIDER".equals(user.getRole())) {
            providerDao.findByUserId(user.getId()).ifPresent(p -> {
                userDto.put("providerId", p.getId());
                userDto.put("businessName", p.getBusinessName());
                userDto.put("isVerified", p.isVerified());
                userDto.put("approvalStatus", p.getApprovalStatus());
                userDto.put("rejectionReason", p.getRejectionReason());
            });
        }

        authData.put("user", userDto);
        return authData;
    }

    @Transactional
    public Map<String, Object> registerCustomer(CustomerRegisterRequest req) {
        String cleanEmail = req.getEmail().trim().toLowerCase();
        if (userDao.findByEmail(cleanEmail).isPresent()) {
            throw new BadRequestException("An account with this email address already exists.");
        }

        if (req.getPassword() == null || req.getPassword().length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters long.");
        }

        User u = new User();
        u.setEmail(cleanEmail);
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        u.setFullName(req.getFullName().trim());
        u.setPhone(req.getPhone().trim());
        u.setRole("CUSTOMER");
        u.setStatus("PENDING_VERIFICATION");
        u.setEmailVerified(false);

        Long userId = userDao.create(u);
        u.setId(userId);

        if (req.getAddress() != null && !req.getAddress().isBlank()) {
            CustomerAddress addr = new CustomerAddress();
            addr.setUserId(userId);
            addr.setLabel("Home");
            addr.setAddressLine1(req.getAddress().trim());
            addr.setCity(req.getCity() != null ? req.getCity() : "Local");
            addr.setPostalCode(req.getPostalCode() != null ? req.getPostalCode() : "000000");
            addr.setDefault(true);
            addressDao.create(addr);
        }

        // Generate verification token and 6-digit OTP
        String token = UUID.randomUUID().toString();
        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);

        emailVerificationDao.create(userId, token, otp, expiresAt);
        emailService.sendVerificationEmail(cleanEmail, u.getFullName(), token, otp);

        Map<String, Object> response = new HashMap<>();
        response.put("email", cleanEmail);
        response.put("message", "Registration successful! A 6-digit verification code has been dispatched to your email address.");
        response.put("requiresVerification", true);
        response.put("verificationCode", otp);
        return response;
    }

    @Transactional
    public Map<String, Object> registerProvider(ProviderRegisterRequest req) {
        String cleanEmail = req.getEmail().trim().toLowerCase();
        if (userDao.findByEmail(cleanEmail).isPresent()) {
            throw new BadRequestException("An account with this email address already exists.");
        }

        if (req.getPassword() == null || req.getPassword().length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters long.");
        }

        User u = new User();
        u.setEmail(cleanEmail);
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        u.setFullName(req.getFullName().trim());
        u.setPhone(req.getPhone().trim());
        u.setRole("PROVIDER");
        u.setStatus("PENDING_VERIFICATION");
        u.setEmailVerified(false);

        Long userId = userDao.create(u);
        u.setId(userId);

        ProviderProfile p = new ProviderProfile();
        p.setUserId(userId);
        p.setBusinessName(req.getBusinessName().trim());
        p.setCategoryId(req.getCategoryId());
        p.setBio(req.getBio());
        p.setExperienceYears(req.getExperienceYears() != null ? req.getExperienceYears() : 1);
        p.setServiceArea(req.getServiceArea() != null ? req.getServiceArea() : "City Wide");
        p.setStartingPrice(req.getStartingPrice());
        p.setVerified(false);
        p.setApprovalStatus("PENDING_APPROVAL");
        p.setAvailable(true);
        p.setProfileImage("https://images.unsplash.com/photo-1540569014015-19a7be504e3a?w=150");

        providerDao.create(p);

        String token = UUID.randomUUID().toString();
        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);

        emailVerificationDao.create(userId, token, otp, expiresAt);
        emailService.sendVerificationEmail(cleanEmail, u.getFullName(), token, otp);

        Map<String, Object> response = new HashMap<>();
        response.put("email", cleanEmail);
        response.put("message", "Provider registration submitted! A verification code has been sent to your email. Once verified, your application will be submitted for administrative approval.");
        response.put("requiresVerification", true);
        response.put("verificationCode", otp);
        return response;
    }

    @Transactional
    public Map<String, Object> resendVerificationCode(String email) {
        String cleanEmail = email.trim().toLowerCase();
        User user = userDao.findByEmail(cleanEmail)
                .orElseThrow(() -> new BadRequestException("No registered account found with email: " + cleanEmail));

        if (user.isEmailVerified()) {
            throw new BadRequestException("This account email is already verified. You can log in directly.");
        }

        String token = UUID.randomUUID().toString();
        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);

        emailVerificationDao.create(user.getId(), token, otp, expiresAt);
        emailService.sendVerificationEmail(cleanEmail, user.getFullName(), token, otp);

        Map<String, Object> response = new HashMap<>();
        response.put("email", cleanEmail);
        response.put("message", "A new 6-digit verification code has been dispatched.");
        response.put("verificationCode", otp);
        return response;
    }

    @Transactional
    public Map<String, Object> verifyEmail(VerifyEmailRequest req) {
        String input = req.getToken().trim();
        Map<String, Object> record = null;

        // Try as token
        var byToken = emailVerificationDao.findByToken(input);
        if (byToken.isPresent()) {
            record = byToken.get();
        } else if (req.getEmail() != null && !req.getEmail().isBlank()) {
            // Try as OTP + email
            var userOpt = userDao.findByEmail(req.getEmail().trim().toLowerCase());
            if (userOpt.isPresent()) {
                var byOtp = emailVerificationDao.findByUserIdAndOtp(userOpt.get().getId(), input);
                if (byOtp.isPresent()) {
                    record = byOtp.get();
                }
            }
        }

        if (record == null) {
            throw new BadRequestException("Invalid or expired verification code. Please check your email or request a new code.");
        }

        Long userId = ((Number) record.get("user_id")).longValue();
        userDao.verifyEmail(userId);
        emailVerificationDao.deleteByUserId(userId);

        User verifiedUser = userDao.findById(userId)
                .orElseThrow(() -> new BadRequestException("User record not found"));

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Email verified successfully! You may now log into your account.");
        result.put("role", verifiedUser.getRole());
        return result;
    }

    public Map<String, Object> forgotPassword(ForgotPasswordRequest req) {
        String cleanEmail = req.getEmail().trim().toLowerCase();
        userDao.findByEmail(cleanEmail).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);
            passwordResetDao.create(user.getId(), token, expiresAt);
            emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), token);
        });

        // Always return positive response to prevent user enumeration
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "If an account exists with this email address, a password reset link has been dispatched to your inbox.");
        return res;
    }

    @Transactional
    public Map<String, Object> resetPassword(ResetPasswordRequest req) {
        String token = req.getToken().trim();
        var recordOpt = passwordResetDao.findByToken(token);
        if (recordOpt.isEmpty()) {
            throw new BadRequestException("Invalid, expired, or already used password reset link. Please request a new one.");
        }

        if (req.getNewPassword() == null || req.getNewPassword().length() < 8) {
            throw new BadRequestException("New password must be at least 8 characters long.");
        }

        Long userId = ((Number) recordOpt.get().get("user_id")).longValue();
        String hashed = passwordEncoder.encode(req.getNewPassword());

        userDao.updatePassword(userId, hashed);
        passwordResetDao.markUsed(token);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Password has been reset successfully. You can now log in with your new password.");
        return res;
    }
}
