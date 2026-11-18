-- =============================================================================
-- ServiceConnect - Useful SQL Queries for DBMS Evaluation & Production
-- =============================================================================

USE serviceconnect_db;

-- -----------------------------------------------------------------------------
-- 1. Finding Providers by Service Category
-- Retrieves verified providers matching category name (e.g. 'Electrician')
-- -----------------------------------------------------------------------------
SELECT 
    p.id AS provider_id,
    p.business_name,
    u.full_name AS provider_name,
    u.phone,
    sc.name AS category_name,
    p.experience_years,
    p.service_area,
    p.starting_price,
    p.rating_avg,
    p.rating_count
FROM provider_profiles p
JOIN users u ON p.user_id = u.id
JOIN service_categories sc ON p.category_id = sc.id
WHERE sc.name = 'Electrician' AND p.is_available = TRUE AND u.status = 'ACTIVE'
ORDER BY p.rating_avg DESC;

-- -----------------------------------------------------------------------------
-- 2. Searching Providers by Location
-- Searches providers servicing a particular area or neighborhood (e.g. 'Downtown')
-- -----------------------------------------------------------------------------
SELECT 
    p.id AS provider_id,
    p.business_name,
    u.full_name,
    sc.name AS category,
    p.service_area,
    p.starting_price,
    p.rating_avg
FROM provider_profiles p
JOIN users u ON p.user_id = u.id
JOIN service_categories sc ON p.category_id = sc.id
WHERE p.service_area LIKE '%Downtown%' AND p.is_available = TRUE
ORDER BY p.rating_avg DESC;

-- -----------------------------------------------------------------------------
-- 3. Finding Top-Rated Providers
-- Retrieves providers with highest customer satisfaction and minimum review threshold
-- -----------------------------------------------------------------------------
SELECT 
    p.id,
    p.business_name,
    sc.name AS category,
    p.rating_avg,
    p.rating_count,
    p.experience_years,
    p.starting_price
FROM provider_profiles p
JOIN service_categories sc ON p.category_id = sc.id
WHERE p.rating_avg >= 4.5 AND p.is_verified = TRUE
ORDER BY p.rating_avg DESC, p.rating_count DESC
LIMIT 10;

-- -----------------------------------------------------------------------------
-- 4. Comparing Provider Prices within a Service Category
-- Useful for customer price comparison matrix
-- -----------------------------------------------------------------------------
SELECT 
    p.business_name,
    sc.name AS category,
    s.name AS service_item,
    ps.custom_price AS provider_rate,
    s.base_price AS benchmark_rate,
    (ps.custom_price - s.base_price) AS price_variance,
    p.rating_avg
FROM provider_services ps
JOIN provider_profiles p ON ps.provider_id = p.id
JOIN services s ON ps.service_id = s.id
JOIN service_categories sc ON s.category_id = sc.id
WHERE sc.name = 'Electrician'
ORDER BY ps.custom_price ASC;

-- -----------------------------------------------------------------------------
-- 5. Displaying Customer Booking History
-- Full appointment track record for customer id = 1
-- -----------------------------------------------------------------------------
SELECT 
    b.id,
    b.booking_reference,
    p.business_name AS provider,
    b.booking_date,
    b.time_slot,
    b.status,
    b.price,
    b.customer_address,
    CASE WHEN r.id IS NOT NULL THEN 'Reviewed' ELSE 'Not Reviewed' END AS review_status
FROM bookings b
JOIN provider_profiles p ON b.provider_id = p.id
LEFT JOIN reviews r ON b.id = r.booking_id
WHERE b.customer_id = 1
ORDER BY b.booking_date DESC, b.created_at DESC;

-- -----------------------------------------------------------------------------
-- 6. Displaying Provider Booking Requests
-- Live order queue for provider id = 1 with status sorting
-- -----------------------------------------------------------------------------
SELECT 
    b.id,
    b.booking_reference,
    b.customer_name,
    b.customer_phone,
    b.customer_address,
    b.problem_description,
    b.booking_date,
    b.time_slot,
    b.price,
    b.status,
    b.created_at
FROM bookings b
WHERE b.provider_id = 1
ORDER BY 
    CASE b.status
        WHEN 'PENDING' THEN 1
        WHEN 'ACCEPTED' THEN 2
        WHEN 'IN_PROGRESS' THEN 3
        WHEN 'COMPLETED' THEN 4
        ELSE 5
    END,
    b.booking_date ASC;

-- -----------------------------------------------------------------------------
-- 7. Calculating Average Ratings & Refreshing Provider Metrics
-- Calculates aggregate stars and review counts per provider
-- -----------------------------------------------------------------------------
SELECT 
    p.id AS provider_id,
    p.business_name,
    COUNT(r.id) AS total_reviews,
    COALESCE(ROUND(AVG(r.rating), 2), 0.00) AS computed_average_rating
FROM provider_profiles p
LEFT JOIN reviews r ON p.id = r.provider_id
GROUP BY p.id, p.business_name;

-- -----------------------------------------------------------------------------
-- 8. Monthly Booking & Revenue Statistics (Administrator KPI)
-- Tracks platform volume, completed count, and monthly gross value
-- -----------------------------------------------------------------------------
SELECT 
    DATE_FORMAT(b.booking_date, '%Y-%m') AS booking_month,
    COUNT(b.id) AS total_bookings,
    SUM(CASE WHEN b.status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed_bookings,
    SUM(CASE WHEN b.status = 'CANCELLED' OR b.status = 'REJECTED' THEN 1 ELSE 0 END) AS cancelled_bookings,
    COALESCE(SUM(CASE WHEN b.status = 'COMPLETED' THEN b.price ELSE 0 END), 0) AS gross_revenue
FROM bookings b
GROUP BY DATE_FORMAT(b.booking_date, '%Y-%m')
ORDER BY booking_month DESC;
