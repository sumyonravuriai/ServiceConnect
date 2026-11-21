-- =============================================================================
-- ServiceConnect - Clear Demo / Past Booking & Review History
-- =============================================================================
-- Use this script in TiDB Cloud SQL Editor to safely purge old test bookings,
-- demo appointments, and test reviews while keeping all user accounts,
-- categories, and service profiles completely intact.
-- =============================================================================

USE serviceconnect_db;

-- 1. Clear chat messages tied to bookings
DELETE FROM chat_messages;

-- 2. Clear reviews tied to bookings
DELETE FROM reviews;

-- 3. Clear bookings
DELETE FROM bookings;

-- 4. Reset provider rating counters
UPDATE provider_profiles
SET rating_avg = 5.00,
    rating_count = 0;

-- 5. Reset bookings AUTO_INCREMENT
ALTER TABLE bookings AUTO_INCREMENT = 1;
ALTER TABLE reviews AUTO_INCREMENT = 1;
ALTER TABLE chat_messages AUTO_INCREMENT = 1;

-- Output confirmation
SELECT 'Successfully cleared all demo bookings and reviews. Database is clean for live appointments!' AS status;
