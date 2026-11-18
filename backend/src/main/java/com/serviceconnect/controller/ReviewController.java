package com.serviceconnect.controller;

import com.serviceconnect.dao.ReviewDao;
import com.serviceconnect.dto.ApiResponse;
import com.serviceconnect.dto.ReviewRequest;
import com.serviceconnect.model.Review;
import com.serviceconnect.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final BookingService bookingService;
    private final ReviewDao reviewDao;

    public ReviewController(BookingService bookingService, ReviewDao reviewDao) {
        this.bookingService = bookingService;
        this.reviewDao = reviewDao;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Review>> addReview(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long customerId,
            @Valid @RequestBody ReviewRequest request) {

        Review review = bookingService.addReview(customerId, request);
        return ResponseEntity.ok(ApiResponse.ok("Review submitted successfully", review));
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<ApiResponse<List<Review>>> getProviderReviews(@PathVariable Long providerId) {
        List<Review> reviews = reviewDao.findByProvider(providerId);
        return ResponseEntity.ok(ApiResponse.ok(reviews));
    }
}
