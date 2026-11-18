package com.serviceconnect.controller;

import com.serviceconnect.dao.BookingDao;
import com.serviceconnect.dao.ChatDao;
import com.serviceconnect.dao.ProviderDao;
import com.serviceconnect.dto.ApiResponse;
import com.serviceconnect.dto.ChatMessageRequest;
import com.serviceconnect.exception.ResourceNotFoundException;
import com.serviceconnect.model.Booking;
import com.serviceconnect.model.ChatMessage;
import com.serviceconnect.model.ProviderProfile;
import com.serviceconnect.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatDao chatDao;
    private final BookingDao bookingDao;
    private final ProviderDao providerDao;
    private final NotificationService notificationService;

    public ChatController(ChatDao chatDao, BookingDao bookingDao, ProviderDao providerDao, NotificationService notificationService) {
        this.chatDao = chatDao;
        this.bookingDao = bookingDao;
        this.providerDao = providerDao;
        this.notificationService = notificationService;
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<List<ChatMessage>>> getMessages(@PathVariable Long bookingId) {
        List<ChatMessage> list = chatDao.findByBookingId(bookingId);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<ChatMessage>> sendMessage(
            @PathVariable Long bookingId,
            @Valid @RequestBody ChatMessageRequest req
    ) {
        Booking booking = bookingDao.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        Long id = chatDao.create(bookingId, req.getSenderId(), req.getSenderName(), req.getMessage());

        ChatMessage msg = new ChatMessage();
        msg.setId(id);
        msg.setBookingId(bookingId);
        msg.setSenderId(req.getSenderId());
        msg.setSenderName(req.getSenderName());
        msg.setMessage(req.getMessage());

        // Determine recipient to dispatch live push via SSE
        Long recipientUserId = null;
        if (booking.getCustomerId().equals(req.getSenderId())) {
            // Customer sent message -> Provider's user_id is recipient
            ProviderProfile prov = providerDao.findById(booking.getProviderId()).orElse(null);
            if (prov != null) {
                recipientUserId = prov.getUserId();
            }
        } else {
            // Provider sent message -> Customer's user_id is recipient
            recipientUserId = booking.getCustomerId();
        }

        if (recipientUserId != null) {
            notificationService.sendNotification(recipientUserId, "CHAT_MESSAGE", Map.of(
                    "bookingId", bookingId,
                    "bookingReference", booking.getBookingReference(),
                    "senderName", req.getSenderName(),
                    "message", req.getMessage(),
                    "timestamp", System.currentTimeMillis()
            ));
        }

        return ResponseEntity.ok(ApiResponse.ok("Message sent", msg));
    }
}
