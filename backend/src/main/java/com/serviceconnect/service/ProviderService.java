package com.serviceconnect.service;

import com.serviceconnect.dao.ProviderDao;
import com.serviceconnect.dao.ReviewDao;
import com.serviceconnect.exception.ResourceNotFoundException;
import com.serviceconnect.model.ProviderCustomService;
import com.serviceconnect.model.ProviderProfile;
import com.serviceconnect.model.Review;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProviderService {

    private final ProviderDao providerDao;
    private final ReviewDao reviewDao;

    public ProviderService(ProviderDao providerDao, ReviewDao reviewDao) {
        this.providerDao = providerDao;
        this.reviewDao = reviewDao;
    }

    public List<ProviderProfile> searchProviders(String category, String city, BigDecimal maxPrice, Double minRating, String sortBy) {
        return providerDao.search(category, city, maxPrice, minRating, sortBy);
    }

    public Map<String, Object> getProviderDetails(Long id) {
        ProviderProfile p = providerDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with ID " + id));

        List<ProviderCustomService> services = providerDao.getProviderServices(id);
        List<Review> reviews = reviewDao.findByProvider(id);

        Map<String, Object> response = new HashMap<>();
        response.put("id", p.getId());
        response.put("userId", p.getUserId());
        response.put("name", p.getProviderName());
        response.put("businessName", p.getBusinessName());
        response.put("category", p.getCategoryName());
        response.put("categoryId", p.getCategoryId());
        response.put("bio", p.getBio());
        response.put("experience", p.getExperienceYears() + " years");
        response.put("experienceYears", p.getExperienceYears());
        response.put("serviceArea", p.getServiceArea());
        response.put("city", p.getServiceArea().split(",")[0].trim());
        response.put("avatar", p.getProfileImage() != null ? p.getProfileImage() : "https://images.unsplash.com/photo-1540569014015-19a7be504e3a?w=150");
        response.put("isVerified", p.isVerified());
        response.put("isAvailable", p.isAvailable());
        response.put("rating", p.getRatingAvg() != null ? p.getRatingAvg().doubleValue() : 0.0);
        response.put("reviewsCount", p.getRatingCount());
        response.put("startingPrice", p.getStartingPrice());
        response.put("workingHours", "Mon - Sat: 08:00 AM - 08:00 PM");
        response.put("availableSlots", List.of("09:00 AM", "11:00 AM", "02:00 PM", "04:30 PM", "06:00 PM"));
        response.put("services", services);
        response.put("reviews", reviews);

        return response;
    }

    public void approveProvider(Long id) {
        providerDao.updateVerification(id, true);
    }

    public void rejectProvider(Long id) {
        providerDao.updateVerification(id, false);
    }
}
