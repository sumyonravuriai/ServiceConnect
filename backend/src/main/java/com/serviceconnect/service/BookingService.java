package com.serviceconnect.service;

import com.serviceconnect.dao.BookingDao;
import com.serviceconnect.dao.ProviderDao;
import com.serviceconnect.dao.ReviewDao;
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
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class BookingService {

    private final BookingDao bookingDao;
    private final ProviderDao providerDao;
    private final ReviewDao reviewDao;
    private final NotificationService notificationService;

    public BookingService(BookingDao bookingDao, ProviderDao providerDao, ReviewDao reviewDao, NotificationService notificationService) {
        this.bookingDao = bookingDao;
        this.providerDao = providerDao;
        this.reviewDao = reviewDao;
        this.notificationService = notificationService;
    }

    @Transactional
    public Booking createBooking(Long customerId, BookingRequest req) {
        // 1. Prevent double booking for the same provider time slot
        boolean alreadyBooked = bookingDao.isSlotBooked(req.getProviderId(), req.getBookingDate(), req.getTimeSlot());
        if (alreadyBooked) {
            throw new BadRequestException("The selected time slot (" + req.getTimeSlot() + ") is already booked on this date. Please pick another available time slot.");
        }

        // 2. Fetch provider starting price or given price
        ProviderProfile prov = providerDao.findById(req.getProviderId())
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with ID " + req.getProviderId()));

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
}
