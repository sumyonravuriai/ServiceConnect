package com.serviceconnect.service;

import com.serviceconnect.model.Booking;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${resend.api-key:}")
    private String resendApiKey;

    @Value("${brevo.api-key:}")
    private String brevoApiKey;

    @Value("${brevo.sender-email:}")
    private String brevoSenderEmail;

    @Value("${app.mail.from:onboarding@resend.dev}")
    private String fromEmail;

    @Value("${app.base-url:https://serviceconnect-web.vercel.app}")
    private String appBaseUrl;

    public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String toEmail, String fullName, String token, String otp) {
        String verifyUrl = appBaseUrl + "/pages/verify-email.html?token=" + token + "&email=" + toEmail;
        String subject = "Verify Your ServiceConnect Account - Code: " + otp;

        String htmlContent = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 8px;">
                <h2 style="color: #0f172a; margin-top: 0;">Welcome to ServiceConnect, %s!</h2>
                <p style="color: #475569; font-size: 15px;">Thank you for registering. To activate your account and start booking trusted local professionals, please verify your email address.</p>
                <div style="margin: 25px 0; padding: 15px; background-color: #f1f5f9; border-radius: 6px; text-align: center;">
                    <span style="font-size: 14px; color: #64748b; display: block; margin-bottom: 5px;">Your 6-Digit Verification Code</span>
                    <strong style="font-size: 28px; letter-spacing: 5px; color: #2563eb;">%s</strong>
                </div>
                <div style="text-align: center; margin: 25px 0;">
                    <a href="%s" style="background-color: #2563eb; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block;">Verify Email Online</a>
                </div>
                <p style="color: #94a3b8; font-size: 13px;">This verification code and link will expire in 30 minutes. If you did not create an account, you can safely ignore this email.</p>
                <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 20px 0;" />
                <p style="color: #94a3b8; font-size: 12px; text-align: center;">&copy; ServiceConnect Technologies Ltd. All rights reserved.</p>
            </div>
        """.formatted(fullName, otp, verifyUrl);

        sendEmail(toEmail, subject, htmlContent);
    }

    public void sendPasswordResetEmail(String toEmail, String fullName, String token) {
        String resetUrl = appBaseUrl + "/pages/forgot-password.html?token=" + token;
        String subject = "Reset Your ServiceConnect Password";

        String htmlContent = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 8px;">
                <h2 style="color: #0f172a; margin-top: 0;">Password Reset Request</h2>
                <p style="color: #475569; font-size: 15px;">Hello %s, we received a request to reset the password for your ServiceConnect account.</p>
                <div style="text-align: center; margin: 30px 0;">
                    <a href="%s" style="background-color: #0f172a; color: #ffffff; padding: 12px 26px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block;">Reset Password</a>
                </div>
                <p style="color: #64748b; font-size: 13px;">Or copy and paste this link in your browser:<br/><a href="%s">%s</a></p>
                <p style="color: #94a3b8; font-size: 13px;">This link is valid for 15 minutes. If you did not request this, please ignore this email and your password will remain unchanged.</p>
            </div>
        """.formatted(fullName, resetUrl, resetUrl, resetUrl);

        sendEmail(toEmail, subject, htmlContent);
    }

    public void sendBookingConfirmation(String toEmail, String customerName, Booking booking) {
        String subject = "Booking Confirmed: " + booking.getBookingReference() + " - " + booking.getServiceName();
        String htmlContent = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 8px;">
                <h2 style="color: #0f172a; margin-top: 0;">Booking Confirmed!</h2>
                <p style="color: #475569;">Hello %s, your appointment request has been scheduled.</p>
                <div style="background-color: #f8fafc; padding: 15px; border-radius: 6px; margin: 20px 0;">
                    <p style="margin: 5px 0;"><strong>Reference ID:</strong> %s</p>
                    <p style="margin: 5px 0;"><strong>Service:</strong> %s</p>
                    <p style="margin: 5px 0;"><strong>Specialist:</strong> %s (%s)</p>
                    <p style="margin: 5px 0;"><strong>Scheduled Date & Slot:</strong> %s at %s</p>
                    <p style="margin: 5px 0;"><strong>Doorstep Address:</strong> %s</p>
                    <p style="margin: 5px 0;"><strong>Fee:</strong> ₹%s</p>
                </div>
                <p style="color: #475569;">You can track real-time progress and message your provider from your customer dashboard.</p>
            </div>
        """.formatted(customerName, booking.getBookingReference(), booking.getServiceName(),
                booking.getProviderName(), booking.getProviderBusiness(),
                booking.getBookingDate(), booking.getTimeSlot(), booking.getCustomerAddress(), booking.getPrice());

        sendEmail(toEmail, subject, htmlContent);
    }

    public void sendNewBookingRequestToProvider(String toEmail, String providerName, Booking booking) {
        String subject = "New Booking Request: Ref " + booking.getBookingReference() + " (" + booking.getTimeSlot() + ")";
        String htmlContent = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 8px;">
                <h2 style="color: #0f172a; margin-top: 0;">New Service Request Received</h2>
                <p style="color: #475569;">Hello %s, a customer has booked your service.</p>
                <div style="background-color: #f8fafc; padding: 15px; border-radius: 6px; margin: 20px 0;">
                    <p style="margin: 5px 0;"><strong>Reference:</strong> %s</p>
                    <p style="margin: 5px 0;"><strong>Client Name:</strong> %s</p>
                    <p style="margin: 5px 0;"><strong>Phone:</strong> %s</p>
                    <p style="margin: 5px 0;"><strong>Service:</strong> %s</p>
                    <p style="margin: 5px 0;"><strong>Date & Slot:</strong> %s at %s</p>
                    <p style="margin: 5px 0;"><strong>Location:</strong> %s</p>
                    <p style="margin: 5px 0;"><strong>Problem Notes:</strong> %s</p>
                </div>
                <p style="color: #2563eb; font-weight: bold;">Please log into your Provider Dashboard to Accept or update this booking.</p>
            </div>
        """.formatted(providerName, booking.getBookingReference(), booking.getCustomerName(),
                booking.getCustomerPhone(), booking.getServiceName(), booking.getBookingDate(),
                booking.getTimeSlot(), booking.getCustomerAddress(),
                booking.getProblemDescription() != null ? booking.getProblemDescription() : "Standard service");

        sendEmail(toEmail, subject, htmlContent);
    }

    public void sendBookingStatusUpdate(String toEmail, String recipientName, Booking booking, String newStatus, String note) {
        String subject = "Booking " + booking.getBookingReference() + " Status: " + newStatus.replace('_', ' ');
        String htmlContent = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 8px;">
                <h2 style="color: #0f172a; margin-top: 0;">Booking Status Updated</h2>
                <p style="color: #475569;">Hello %s, your appointment status has been updated to: <strong style="color: #2563eb;">%s</strong></p>
                <div style="background-color: #f8fafc; padding: 15px; border-radius: 6px; margin: 20px 0;">
                    <p style="margin: 5px 0;"><strong>Booking Ref:</strong> %s</p>
                    <p style="margin: 5px 0;"><strong>Service:</strong> %s</p>
                    <p style="margin: 5px 0;"><strong>Date & Time:</strong> %s at %s</p>
                    %s
                </div>
            </div>
        """.formatted(recipientName, newStatus.replace('_', ' '), booking.getBookingReference(),
                booking.getServiceName(), booking.getBookingDate(), booking.getTimeSlot(),
                note != null && !note.isBlank() ? "<p style='margin: 5px 0;'><strong>Note:</strong> " + note + "</p>" : "");

        sendEmail(toEmail, subject, htmlContent);
    }

    public void sendProviderApprovalNotification(String toEmail, String providerName, boolean approved, String reason) {
        String subject = approved ? "Congratulations! Your ServiceConnect Provider Account is Approved"
                : "ServiceConnect Provider Application Status Update";
        String htmlContent = approved ? """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 8px;">
                <h2 style="color: #10b981; margin-top: 0;">🎉 You are Officially Approved!</h2>
                <p style="color: #475569;">Hello %s, your service provider profile has been verified and approved by the ServiceConnect administration.</p>
                <p style="color: #475569;">Your profile and services are now publicly visible to local customers in your coverage areas.</p>
                <div style="text-align: center; margin: 25px 0;">
                    <a href="%s/pages/login.html" style="background-color: #2563eb; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block;">Go to Provider Dashboard</a>
                </div>
            </div>
        """.formatted(providerName, appBaseUrl) : """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 8px;">
                <h2 style="color: #b91c1c; margin-top: 0;">Provider Application Update</h2>
                <p style="color: #475569;">Hello %s, we were unable to approve your provider profile at this time.</p>
                <p style="color: #475569;"><strong>Reason:</strong> %s</p>
                <p style="color: #64748b; font-size: 13px;">Please feel free to update your documentation and reapply or contact platform support.</p>
            </div>
        """.formatted(providerName, reason != null ? reason : "Documentation review criteria not met.");

        sendEmail(toEmail, subject, htmlContent);
    }

    private void sendEmail(String toEmail, String subject, String htmlBody) {
        // Log to console for real-time visibility in Render deployment logs
        log.info("\n==================== [OUTGOING EMAIL DISPATCH] ====================" +
                "\nTo: {}\nSubject: {}\n--------------------------------------------------------------------\n{}\n" +
                "===================================================================", toEmail, subject, htmlBody);

        // 1. Prioritize Resend HTTP REST API (Over standard HTTPS port 443 - Never blocked on Render)
        if (resendApiKey != null && !resendApiKey.isBlank()) {
            boolean sent = sendViaResendHttp(toEmail, subject, htmlBody);
            if (sent) return;
        }

        // 2. Brevo HTTP REST API (Over standard HTTPS port 443 - Never blocked on Render)
        if (brevoApiKey != null && !brevoApiKey.isBlank()) {
            boolean sent = sendViaBrevoHttp(toEmail, subject, htmlBody);
            if (sent) return;
        }

        // 3. Fallback to JavaMailSender SMTP (Note: Free cloud hosts like Render block outbound ports 25, 465, and 587)
        if (mailSender != null) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom(fromEmail);
                helper.setTo(toEmail);
                helper.setSubject(subject);
                helper.setText(htmlBody, true);
                mailSender.send(message);
                log.info("✓ Email successfully sent via SMTP to {}", toEmail);
            } catch (Exception ex) {
                log.warn("SMTP delivery attempt to {} failed: {}. (Render blocks outbound SMTP ports 25/465/587 by default; set RESEND_API_KEY in Render Environment to deliver via HTTPS port 443).", toEmail, ex.getMessage());
            }
        }
    }

    private boolean sendViaResendHttp(String toEmail, String subject, String htmlBody) {
        try {
            String sender = fromEmail;
            if (sender == null || sender.isBlank() || sender.contains("localhost") || sender.contains("noreply@serviceconnect.com")) {
                sender = "ServiceConnect <onboarding@resend.dev>";
            } else if (!sender.contains("<")) {
                sender = "ServiceConnect <" + sender + ">";
            }

            String jsonPayload = """
                {
                    "from": "%s",
                    "to": ["%s"],
                    "subject": "%s",
                    "html": "%s"
                }
                """.formatted(
                    escapeJson(sender),
                    escapeJson(toEmail),
                    escapeJson(subject),
                    escapeJson(htmlBody)
                );

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + resendApiKey.trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("✓ Email successfully delivered to {} via Resend HTTPS API (HTTP {})", toEmail, response.statusCode());
                return true;
            } else {
                log.warn("Resend API delivery failed with status {}: {}. Note: Resend's free test tier (onboarding@resend.dev) delivers only to your own registered account email. To send to any recipient, verify your domain on resend.com or configure BREVO_API_KEY in Render.", response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            log.warn("Error calling Resend HTTPS API: {}", e.getMessage());
            return false;
        }
    }

    private boolean sendViaBrevoHttp(String toEmail, String subject, String htmlBody) {
        try {
            String senderEmail = brevoSenderEmail;
            if (senderEmail == null || senderEmail.isBlank()) {
                senderEmail = fromEmail;
            }
            if (senderEmail == null || senderEmail.isBlank() || senderEmail.contains("noreply@serviceconnect.com") || senderEmail.contains("localhost") || senderEmail.contains("onboarding@resend.dev")) {
                senderEmail = "serviceconnect@resend.dev";
            }
            if (senderEmail.contains("<")) {
                senderEmail = senderEmail.substring(senderEmail.indexOf('<') + 1, senderEmail.indexOf('>')).trim();
            }

            String jsonPayload = """
                {
                    "sender": { "name": "ServiceConnect", "email": "%s" },
                    "to": [{ "email": "%s" }],
                    "subject": "%s",
                    "htmlContent": "%s"
                }
                """.formatted(
                    escapeJson(senderEmail),
                    escapeJson(toEmail),
                    escapeJson(subject),
                    escapeJson(htmlBody)
                );

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .header("api-key", brevoApiKey.trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("✓ Email successfully delivered to {} via Brevo HTTPS API (HTTP {})", toEmail, response.statusCode());
                return true;
            } else {
                log.warn("Brevo API delivery failed with status {}: {}", response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            log.warn("Error calling Brevo HTTPS API: {}", e.getMessage());
            return false;
        }
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < ' ') {
                        String t = "000" + Integer.toHexString(c);
                        sb.append("\\u").append(t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}
