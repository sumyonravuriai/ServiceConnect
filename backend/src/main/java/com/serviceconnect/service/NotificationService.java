package com.serviceconnect.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    // Map: userId -> list of active SseEmitters (user may have multiple open tabs)
    private final Map<Long, List<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long userId) {
        // 30 minute timeout
        SseEmitter emitter = new SseEmitter(1800000L);

        userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(e -> removeEmitter(userId, emitter));

        // Send initial connection confirmation event
        try {
            emitter.send(SseEmitter.event()
                    .name("CONNECTED")
                    .data(Map.of("message", "Live notifications connected for user " + userId, "status", "ONLINE")));
        } catch (IOException e) {
            log.warn("Failed to send initial SSE connect event for user {}", userId);
            removeEmitter(userId, emitter);
        }

        return emitter;
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        List<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                userEmitters.remove(userId);
            }
        }
    }

    public void sendNotification(Long userId, String eventType, Object payload) {
        List<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventType)
                        .data(payload, MediaType.APPLICATION_JSON));
            } catch (Exception ex) {
                deadEmitters.add(emitter);
            }
        }
        for (SseEmitter dead : deadEmitters) {
            removeEmitter(userId, dead);
        }
    }

    public void broadcastBookingStatus(Long customerUserId, Long providerUserId, String bookingReference, String status, String note) {
        Map<String, Object> payload = Map.of(
                "bookingReference", bookingReference != null ? bookingReference : "",
                "status", status != null ? status : "",
                "note", note != null ? note : "",
                "timestamp", System.currentTimeMillis()
        );

        if (customerUserId != null) {
            sendNotification(customerUserId, "BOOKING_UPDATE", payload);
        }
        if (providerUserId != null) {
            sendNotification(providerUserId, "BOOKING_UPDATE", payload);
        }
    }
}
