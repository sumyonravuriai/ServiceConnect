package com.serviceconnect.controller;

import com.serviceconnect.dto.ApiResponse;
import com.serviceconnect.dto.BookingLocationDto;
import com.serviceconnect.dto.BookingRequest;
import com.serviceconnect.dto.StatusUpdateRequest;
import com.serviceconnect.model.Booking;
import com.serviceconnect.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Booking>> createBooking(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long customerId,
            @Valid @RequestBody BookingRequest request) {

        Booking booking = bookingService.createBooking(customerId, request);
        return ResponseEntity.ok(ApiResponse.ok("Booking confirmed successfully", booking));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Booking>> getBookingById(@PathVariable Long id) {
        Booking booking = bookingService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(booking));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Booking>>> getAllBookings(@RequestParam(required = false) String status) {
        List<Booking> bookings = bookingService.getAllBookings(status);
        return ResponseEntity.ok(ApiResponse.ok(bookings));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<Booking>>> getCustomerBookings(@PathVariable Long customerId) {
        List<Booking> bookings = bookingService.getCustomerBookings(customerId);
        return ResponseEntity.ok(ApiResponse.ok(bookings));
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<ApiResponse<List<Booking>>> getProviderBookings(@PathVariable Long providerId) {
        List<Booking> bookings = bookingService.getProviderBookings(providerId);
        return ResponseEntity.ok(ApiResponse.ok(bookings));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Booking>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {

        Booking updated = bookingService.updateStatus(id, request.getStatus(), request.getReason());
        return ResponseEntity.ok(ApiResponse.ok("Status updated to " + request.getStatus(), updated));
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<Booking>> acceptBooking(@PathVariable Long id) {
        Booking updated = bookingService.updateStatus(id, "ACCEPTED", null);
        return ResponseEntity.ok(ApiResponse.ok("Booking accepted", updated));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Booking>> rejectBooking(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Provider unavailable") String reason) {

        Booking updated = bookingService.updateStatus(id, "REJECTED", reason);
        return ResponseEntity.ok(ApiResponse.ok("Booking rejected", updated));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Booking>> cancelBooking(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Cancelled by user") String reason) {

        Booking updated = bookingService.updateStatus(id, "CANCELLED", reason);
        return ResponseEntity.ok(ApiResponse.ok("Booking cancelled", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.ok(ApiResponse.ok("Booking record removed successfully", null));
    }

    @DeleteMapping("/provider/{providerId}/history")
    public ResponseEntity<ApiResponse<Integer>> clearProviderHistory(@PathVariable Long providerId) {
        int cleared = bookingService.clearProviderHistory(providerId);
        return ResponseEntity.ok(ApiResponse.ok("Cleared " + cleared + " past job records from provider history", cleared));
    }

    @DeleteMapping("/customer/{customerId}/history")
    public ResponseEntity<ApiResponse<Integer>> clearCustomerHistory(@PathVariable Long customerId) {
        int cleared = bookingService.clearCustomerHistory(customerId);
        return ResponseEntity.ok(ApiResponse.ok("Cleared " + cleared + " past appointment records from history", cleared));
    }

    @GetMapping("/{id}/location")
    public ResponseEntity<ApiResponse<BookingLocationDto>> getBookingLocation(@PathVariable Long id) {
        BookingLocationDto loc = bookingService.getBookingLocation(id);
        return ResponseEntity.ok(ApiResponse.ok(loc));
    }

    @PostMapping("/{id}/location")
    public ResponseEntity<ApiResponse<BookingLocationDto>> updateBookingLocation(
            @PathVariable Long id,
            @RequestBody BookingLocationDto update) {
        BookingLocationDto updated = bookingService.updateBookingLocation(id, update);
        return ResponseEntity.ok(ApiResponse.ok("Live technician GPS coordinates updated", updated));
    }
}
