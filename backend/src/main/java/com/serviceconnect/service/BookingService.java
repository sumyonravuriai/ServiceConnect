package com.serviceconnect.service;

import com.serviceconnect.dao.BookingDao;
import com.serviceconnect.dao.ProviderDao;
import com.serviceconnect.dao.ReviewDao;
import com.serviceconnect.dao.UserDao;
import com.serviceconnect.dto.BookingLocationDto;
import com.serviceconnect.dto.BookingRequest;
import com.serviceconnect.dto.ReviewRequest;
import com.serviceconnect.exception.BadRequestException;
import com.serviceconnect.exception.ResourceNotFoundException;
import com.serviceconnect.model.Booking;
import com.serviceconnect.model.ProviderProfile;
import com.serviceconnect.model.Review;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BookingService {

    private final BookingDao bookingDao;
    private final ProviderDao providerDao;
    private final ReviewDao reviewDao;
    private final UserDao userDao;
    private final NotificationService notificationService;
    private final EmailService emailService;

    // Concurrent in-memory GPS tracking cache for real-time technician movement
    private final ConcurrentHashMap<Long, BookingLocationDto> trackingMap = new ConcurrentHashMap<>();

    public BookingService(BookingDao bookingDao, ProviderDao providerDao, ReviewDao reviewDao,
                          UserDao userDao, NotificationService notificationService, EmailService emailService) {
        this.bookingDao = bookingDao;
        this.providerDao = providerDao;
        this.reviewDao = reviewDao;
        this.userDao = userDao;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    @Transactional
    public Booking createBooking(Long customerId, BookingRequest req) {
        // 1. Prevent double booking for the same provider time slot
        boolean alreadyBooked = bookingDao.isSlotBooked(req.getProviderId(), req.getBookingDate(), req.getTimeSlot());
        if (alreadyBooked) {
            throw new BadRequestException("The selected time slot (" + req.getTimeSlot() + ") is already booked on this date. Please pick another available time slot.");
        }

        // 2. Fetch provider and ensure approved status
        ProviderProfile prov = providerDao.findById(req.getProviderId())
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with ID " + req.getProviderId()));

        if (!"APPROVED".equalsIgnoreCase(prov.getApprovalStatus()) || !prov.isAvailable()) {
            throw new BadRequestException("This service provider is not currently approved or available for public bookings.");
        }

        BigDecimal price = (req.getPrice() != null && req.getPrice().compareTo(BigDecimal.ZERO) > 0)
                ? req.getPrice()
                : prov.getStartingPrice();

        // 3. Generate unique booking reference (e.g. SC-82741)
        String reference = "SC-" + (10000 + new Random().nextInt(90000));

        Booking b = new Booking();
        b.setBookingReference(reference);
        b.setCustomerId(customerId != null ? customerId : 1L);
        b.setProviderId(req.getProviderId());
        b.setServiceId(req.getServiceId());
        b.setBookingDate(req.getBookingDate());
        b.setTimeSlot(req.getTimeSlot());
        b.setStatus("PENDING");
        b.setCustomerName(req.getCustomerName());
        b.setCustomerPhone(req.getCustomerPhone());
        b.setCustomerAddress(req.getAddress());
        b.setProblemDescription(req.getProblemDescription());
        b.setPrice(price);

        Long bookingId = bookingDao.create(b);
        Booking created = bookingDao.findById(bookingId).orElse(b);

        // Notify provider in real time about incoming booking request
        try {
            notificationService.sendNotification(prov.getUserId(), "NEW_BOOKING_REQUEST", Map.of(
                    "bookingReference", reference,
                    "customerName", b.getCustomerName(),
                    "customerPhone", b.getCustomerPhone(),
                    "bookingDate", b.getBookingDate().toString(),
                    "timeSlot", b.getTimeSlot(),
                    "price", b.getPrice(),
                    "address", b.getCustomerAddress()
            ));

            // Real email notifications
            userDao.findById(created.getCustomerId()).ifPresent(u -> {
                emailService.sendBookingConfirmation(u.getEmail(), created.getCustomerName(), created);
            });
            userDao.findById(prov.getUserId()).ifPresent(pu -> {
                emailService.sendNewBookingRequestToProvider(pu.getEmail(), prov.getProviderName(), created);
            });
        } catch (Exception e) {
            // Log but don't fail booking creation
        }

        return created;
    }

    public Booking getById(Long id) {
        return bookingDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID " + id));
    }

    public List<Booking> getCustomerBookings(Long customerId) {
        return bookingDao.findByCustomer(customerId);
    }

    public List<Booking> getProviderBookings(Long providerId) {
        return bookingDao.findByProvider(providerId);
    }

    public List<Booking> getAllBookings(String status) {
        return bookingDao.findAll(status);
    }

    @Transactional
    public Booking updateStatus(Long bookingId, String newStatus, String reason) {
        Booking booking = getById(bookingId);
        String currentStatus = booking.getStatus();
        String target = newStatus.toUpperCase();

        // State Machine validation
        if ("CANCELLED".equals(target)) {
            if (!"PENDING".equals(currentStatus) && !"ACCEPTED".equals(currentStatus)) {
                throw new BadRequestException("Cannot cancel booking that is already " + currentStatus);
            }
        } else if ("ACCEPTED".equals(target)) {
            if (!"PENDING".equals(currentStatus)) {
                throw new BadRequestException("Only PENDING bookings can be accepted");
            }
        } else if ("REJECTED".equals(target)) {
            if (!"PENDING".equals(currentStatus)) {
                throw new BadRequestException("Only PENDING bookings can be rejected");
            }
        } else if ("IN_PROGRESS".equals(target)) {
            if (!"ACCEPTED".equals(currentStatus)) {
                throw new BadRequestException("Only ACCEPTED bookings can be started");
            }
        } else if ("COMPLETED".equals(target)) {
            if (!"IN_PROGRESS".equals(currentStatus) && !"ACCEPTED".equals(currentStatus)) {
                throw new BadRequestException("Cannot complete booking from current state: " + currentStatus);
            }
        }

        bookingDao.updateStatus(bookingId, target, reason);
        Booking updated = getById(bookingId);

        // Real-time broadcast to both customer and provider
        try {
            ProviderProfile prov = providerDao.findById(updated.getProviderId()).orElse(null);
            Long providerUserId = prov != null ? prov.getUserId() : null;
            notificationService.broadcastBookingStatus(updated.getCustomerId(), providerUserId, updated.getBookingReference(), target, reason);

            // Real email notification to customer
            userDao.findById(updated.getCustomerId()).ifPresent(u -> {
                emailService.sendBookingStatusUpdate(u.getEmail(), updated.getCustomerName(), updated, target, reason);
            });
            // Real email notification to provider
            if (providerUserId != null) {
                userDao.findById(providerUserId).ifPresent(pu -> {
                    emailService.sendBookingStatusUpdate(pu.getEmail(), prov.getProviderName(), updated, target, reason);
                });
            }
        } catch (Exception e) {
            // Non-blocking notification dispatch
        }

        return updated;
    }

    @Transactional
    public Review addReview(Long customerId, ReviewRequest req) {
        Booking booking = getById(req.getBookingId());

        // 1. Only completed bookings can be reviewed
        if (!"COMPLETED".equalsIgnoreCase(booking.getStatus())) {
            throw new BadRequestException("Reviews can only be submitted for completed services");
        }

        // 2. Prevent duplicate reviews
        if (reviewDao.existsByBooking(req.getBookingId())) {
            throw new BadRequestException("A review has already been submitted for this booking");
        }

        Review r = new Review();
        r.setBookingId(req.getBookingId());
        r.setCustomerId(customerId != null ? customerId : booking.getCustomerId());
        r.setProviderId(req.getProviderId());
        r.setRating(req.getRating());
        r.setComment(req.getComment());

        reviewDao.create(r);

        // 3. Recalculate and update provider rating statistics
        providerDao.updateRatings(req.getProviderId());

        return r;
    }

    @Transactional
    public void deleteBooking(Long bookingId) {
        trackingMap.remove(bookingId);
        bookingDao.delete(bookingId);
    }

    @Transactional
    public int clearProviderHistory(Long providerId) {
        return bookingDao.deleteCompletedOrCancelledByProvider(providerId);
    }

    @Transactional
    public int clearCustomerHistory(Long customerId) {
        return bookingDao.deleteCompletedOrCancelledByCustomer(customerId);
    }

    public BookingLocationDto getBookingLocation(Long bookingId) {
        BookingLocationDto existing = trackingMap.get(bookingId);
        if (existing != null) {
            return existing;
        }

        Booking b = getById(bookingId);
        double custLat = 17.3850;
        double custLng = 78.4867;
        double provLat = 17.3995;
        double provLng = 78.4735;

        // Custom offset derived from booking id for distinct visual placement on the map
        double offset = (b.getId() % 10) * 0.005;
        custLat += offset;
        custLng += offset;
        provLat += offset;
        provLng += offset;

        double distance = calculateHaversineDistanceKm(provLat, provLng, custLat, custLng);
        int eta = Math.max(3, (int) Math.round(distance * 3.5));
        String statusText = "EN_ROUTE";

        if ("PENDING".equals(b.getStatus())) {
            statusText = "SCHEDULED";
            distance = 3.5;
            eta = 20;
        } else if ("COMPLETED".equals(b.getStatus())) {
            statusText = "ARRIVED";
            distance = 0.0;
            eta = 0;
            provLat = custLat;
            provLng = custLng;
        } else if ("CANCELLED".equals(b.getStatus()) || "REJECTED".equals(b.getStatus())) {
            statusText = "CANCELLED";
            distance = 0.0;
            eta = 0;
        }

        BookingLocationDto loc = new BookingLocationDto(bookingId, provLat, provLng, custLat, custLng, eta, distance, statusText);
        trackingMap.put(bookingId, loc);
        return loc;
    }

    public BookingLocationDto updateBookingLocation(Long bookingId, BookingLocationDto update) {
        Booking b = getById(bookingId);
        BookingLocationDto current = trackingMap.computeIfAbsent(bookingId, id -> getBookingLocation(id));

        if (update.getProviderLat() != null) current.setProviderLat(update.getProviderLat());
        if (update.getProviderLng() != null) current.setProviderLng(update.getProviderLng());
        if (update.getCustomerLat() != null) current.setCustomerLat(update.getCustomerLat());
        if (update.getCustomerLng() != null) current.setCustomerLng(update.getCustomerLng());

        if (current.getProviderLat() != null && current.getProviderLng() != null
                && current.getCustomerLat() != null && current.getCustomerLng() != null) {
            double dist = calculateHaversineDistanceKm(current.getProviderLat(), current.getProviderLng(),
                    current.getCustomerLat(), current.getCustomerLng());
            current.setDistanceKm(dist);
            current.setEtaMinutes(Math.max(1, (int) Math.round(dist * 3.5)));
        }

        if (update.getStatusText() != null && !update.getStatusText().isBlank()) {
            current.setStatusText(update.getStatusText());
        }
        current.setUpdatedAt(LocalDateTime.now());
        trackingMap.put(bookingId, current);

        // Real-time notification to customer via SSE
        try {
            notificationService.sendNotification(b.getCustomerId(), "LIVE_GPS_UPDATE", Map.of(
                    "bookingId", bookingId,
                    "providerLat", current.getProviderLat(),
                    "providerLng", current.getProviderLng(),
                    "distanceKm", current.getDistanceKm(),
                    "etaMinutes", current.getEtaMinutes(),
                    "statusText", current.getStatusText()
            ));
        } catch (Exception ignored) {}

        return current;
    }

    private double calculateHaversineDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(R * c * 100.0) / 100.0;
    }
}
