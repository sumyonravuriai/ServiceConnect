-- =============================================================================
-- ServiceConnect - Relational Database Schema (MySQL 8.0+)
-- Local Service Provider Discovery and Booking Platform
-- =============================================================================

DROP DATABASE IF EXISTS serviceconnect_db;
CREATE DATABASE serviceconnect_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE serviceconnect_db;

-- -----------------------------------------------------------------------------
-- 1. USERS TABLE
-- Stores credentials, role (CUSTOMER, PROVIDER, ADMIN), and account state
-- -----------------------------------------------------------------------------
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL COMMENT 'BCrypt hashed password',
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    role ENUM('CUSTOMER', 'PROVIDER', 'ADMIN') NOT NULL DEFAULT 'CUSTOMER',
    status ENUM('ACTIVE', 'PENDING_VERIFICATION', 'SUSPENDED') NOT NULL DEFAULT 'PENDING_VERIFICATION',
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_role (role),
    INDEX idx_user_status (status),
    INDEX idx_user_email_verif (email, email_verified)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 2. CUSTOMER ADDRESSES TABLE
-- Stores saved doorstep service delivery addresses for customer accounts
-- -----------------------------------------------------------------------------
CREATE TABLE customer_addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    label VARCHAR(50) DEFAULT 'Home',
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    postal_code VARCHAR(20) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_address_user (user_id, is_default)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 3. SERVICE CATEGORIES TABLE
-- Master catalog of trade categories (Electrician, Plumber, AC Repair, etc.)
-- -----------------------------------------------------------------------------
CREATE TABLE service_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    icon_name VARCHAR(50) DEFAULT '⚡',
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 4. PROVIDER PROFILES TABLE
-- Professional business profiles, verification, ratings and coverage areas
-- -----------------------------------------------------------------------------
CREATE TABLE provider_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    business_name VARCHAR(150) NOT NULL,
    category_id BIGINT NOT NULL,
    bio TEXT,
    experience_years INT DEFAULT 1,
    service_area VARCHAR(255) NOT NULL,
    profile_image VARCHAR(500),
    is_verified BOOLEAN DEFAULT FALSE,
    is_available BOOLEAN DEFAULT TRUE,
    approval_status ENUM('PENDING_APPROVAL', 'APPROVED', 'REJECTED', 'SUSPENDED') NOT NULL DEFAULT 'PENDING_APPROVAL',
    rejection_reason VARCHAR(255),
    rating_avg DECIMAL(3, 2) DEFAULT 0.00,
    rating_count INT DEFAULT 0,
    starting_price DECIMAL(10, 2) NOT NULL DEFAULT 299.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES service_categories(id) ON DELETE RESTRICT,
    INDEX idx_provider_cat (category_id, is_verified),
    INDEX idx_provider_approval (approval_status, is_available),
    INDEX idx_provider_rating (rating_avg),
    INDEX idx_provider_price (starting_price)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 5. SERVICES TABLE
-- Standard trade service items categorized under specific trades
-- -----------------------------------------------------------------------------
CREATE TABLE services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    base_price DECIMAL(10, 2) NOT NULL,
    estimated_duration VARCHAR(50) DEFAULT '60 mins',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES service_categories(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 6. PROVIDER SERVICES TABLE
-- Specific services configured and customized by individual providers with rates
-- -----------------------------------------------------------------------------
CREATE TABLE provider_services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    custom_price DECIMAL(10, 2) NOT NULL,
    custom_description TEXT,
    is_available BOOLEAN DEFAULT TRUE,
    UNIQUE KEY uk_provider_service (provider_id, service_id),
    FOREIGN KEY (provider_id) REFERENCES provider_profiles(id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 7. PROVIDER AVAILABILITY TABLE
-- Working days, shift hours, and appointment slot configuration
-- -----------------------------------------------------------------------------
CREATE TABLE provider_availability (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider_id BIGINT NOT NULL,
    day_of_week TINYINT NOT NULL COMMENT '1=Monday, 2=Tuesday, ..., 7=Sunday',
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    slot_duration_minutes INT DEFAULT 60,
    is_active BOOLEAN DEFAULT TRUE,
    UNIQUE KEY uk_prov_day (provider_id, day_of_week),
    FOREIGN KEY (provider_id) REFERENCES provider_profiles(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 8. BOOKINGS TABLE
-- End-to-end appointment lifecycle with controlled status transitions
-- -----------------------------------------------------------------------------
CREATE TABLE bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_reference VARCHAR(30) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    provider_id BIGINT NOT NULL,
    service_id BIGINT,
    booking_date DATE NOT NULL,
    time_slot VARCHAR(30) NOT NULL,
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    customer_name VARCHAR(100) NOT NULL,
    customer_phone VARCHAR(20) NOT NULL,
    customer_address TEXT NOT NULL,
    problem_description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    cancellation_reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (provider_id) REFERENCES provider_profiles(id) ON DELETE RESTRICT,
    FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE SET NULL,
    INDEX idx_booking_slot (provider_id, booking_date, time_slot),
    INDEX idx_booking_cust_status (customer_id, status)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 9. REVIEWS TABLE
-- Customer feedback; UNIQUE KEY on booking_id prevents duplicate reviews
-- -----------------------------------------------------------------------------
CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE COMMENT 'Prevents duplicate reviews for same booking',
    customer_id BIGINT NOT NULL,
    provider_id BIGINT NOT NULL,
    rating TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (provider_id) REFERENCES provider_profiles(id) ON DELETE CASCADE,
    INDEX idx_review_prov (provider_id, rating)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 10. FAVOURITES TABLE
-- Customer bookmarked preferred service providers
-- -----------------------------------------------------------------------------
CREATE TABLE favourites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    provider_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_cust_fav_prov (customer_id, provider_id),
    FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (provider_id) REFERENCES provider_profiles(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 11. CHAT MESSAGES TABLE (Real-Time Communication)
-- Live coordination between customer and assigned technician
-- -----------------------------------------------------------------------------
CREATE TABLE chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    sender_name VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_chat_booking (booking_id, created_at)
) ENGINE=InnoDB;


-- -----------------------------------------------------------------------------
-- 12. EMAIL VERIFICATIONS TABLE (Public Security)
-- -----------------------------------------------------------------------------
CREATE TABLE email_verifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(100) NOT NULL UNIQUE,
    otp_code VARCHAR(10) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_verif_token (token),
    INDEX idx_verif_user (user_id)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 13. PASSWORD RESETS TABLE (Public Security)
-- -----------------------------------------------------------------------------
CREATE TABLE password_resets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(100) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_pwd_token (token)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 14. AUDIT LOGS TABLE (Administrative Accountability)
-- -----------------------------------------------------------------------------
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id BIGINT NOT NULL,
    action VARCHAR(100) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id BIGINT NOT NULL,
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_audit_action (action, created_at)
) ENGINE=InnoDB;
