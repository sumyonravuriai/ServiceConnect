package com.serviceconnect.dto;

import java.time.LocalDateTime;

public class BookingLocationDto {
    private Long bookingId;
    private Double providerLat;
    private Double providerLng;
    private Double customerLat;
    private Double customerLng;
    private Integer etaMinutes;
    private Double distanceKm;
    private String statusText;
    private LocalDateTime updatedAt;

    public BookingLocationDto() {}

    public BookingLocationDto(Long bookingId, Double providerLat, Double providerLng,
                              Double customerLat, Double customerLng, Integer etaMinutes,
                              Double distanceKm, String statusText) {
        this.bookingId = bookingId;
        this.providerLat = providerLat;
        this.providerLng = providerLng;
        this.customerLat = customerLat;
        this.customerLng = customerLng;
        this.etaMinutes = etaMinutes;
        this.distanceKm = distanceKm;
        this.statusText = statusText;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public Double getProviderLat() { return providerLat; }
    public void setProviderLat(Double providerLat) { this.providerLat = providerLat; }

    public Double getProviderLng() { return providerLng; }
    public void setProviderLng(Double providerLng) { this.providerLng = providerLng; }

    public Double getCustomerLat() { return customerLat; }
    public void setCustomerLat(Double customerLat) { this.customerLat = customerLat; }

    public Double getCustomerLng() { return customerLng; }
    public void setCustomerLng(Double customerLng) { this.customerLng = customerLng; }

    public Integer getEtaMinutes() { return etaMinutes; }
    public void setEtaMinutes(Integer etaMinutes) { this.etaMinutes = etaMinutes; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public String getStatusText() { return statusText; }
    public void setStatusText(String statusText) { this.statusText = statusText; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
