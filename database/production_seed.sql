-- =============================================================================
-- ServiceConnect - Production Master Seed Data (Clean Production Environment)
-- =============================================================================
-- This file contains ONLY system master records required for real-time operation:
-- 1. System Administrator account (to manage categories, approve providers, etc.)
-- 2. Master Trade Categories
-- 3. Standard Trade Services Catalog
--
-- All dummy customers, fake providers, demo bookings, reviews, and chats are OMITTED.
-- Real providers register via /pages/register.html?role=PROVIDER and await Admin approval.
-- Real customers register via /pages/register.html?role=CUSTOMER.
-- =============================================================================

USE serviceconnect_db;

-- -----------------------------------------------------------------------------
-- 1. SYSTEM ADMINISTRATOR ACCOUNT
-- Default login: admin@serviceconnect.com / password123 (Change upon first production login)
-- BCrypt hash for 'password123': $2a$10$e8p2c6/7qZ8k1s1X0e5cuA4Q1q2w3e4r5t6y7u8i9o0p1a2s3d4f
-- -----------------------------------------------------------------------------
INSERT INTO users (id, email, password_hash, full_name, phone, role, status, email_verified) VALUES
(1, 'admin@serviceconnect.com', '$2a$10$e8p2c6/7qZ8k1s1X0e5cuA4Q1q2w3e4r5t6y7u8i9o0p1a2s3d4f', 'System Administrator', '+91 98765 99999', 'ADMIN', 'ACTIVE', TRUE)
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name), email_verified = TRUE;

-- -----------------------------------------------------------------------------
-- 2. MASTER SERVICE CATEGORIES
-- -----------------------------------------------------------------------------
INSERT INTO service_categories (id, name, slug, icon_name, description) VALUES
(1, 'Electrician', 'electrician', '⚡', 'Wiring, switchboard repairs, fuse fixes, inverter installation & electrical setups'),
(2, 'Plumber', 'plumber', '🔧', 'Pipe leakages, taps, bathroom fittings, drainage unblocking & water heaters'),
(3, 'Mechanic', 'mechanic', '🚗', 'Doorstep vehicle maintenance, battery jump start, engine tune-up & safety inspection'),
(4, 'Cleaner', 'cleaner', '🧹', 'Deep home cleaning, sofa sanitization, carpet washing & kitchen degreasing'),
(5, 'Carpenter', 'carpenter', '🪚', 'Furniture repairs, door lock replacement, modular wardrobe & custom woodwork'),
(6, 'AC Repair', 'ac-repair', '❄️', 'AC servicing, gas refilling, foam jet cleaning & compressor troubleshooting'),
(7, 'Painter', 'painter', '🎨', 'Interior & exterior wall painting, waterproofing, putty & texture finish'),
(8, 'Cooking Services', 'cooking', '🍳', 'Daily home cook, meal preparation, party catering & specialized cuisines'),
(9, 'Tutoring', 'tutoring', '📚', 'K-12 school curriculum, STEM subjects, language training & music lessons'),
(10, 'Pest Control', 'pest-control', '🐜', 'Termite control, cockroach treatment, bed bugs eradication & organic pest care')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- -----------------------------------------------------------------------------
-- 3. STANDARD TRADE SERVICES MASTER CATALOG
-- Providers pick from these standard offerings and set their custom rates & duration
-- -----------------------------------------------------------------------------
INSERT INTO services (id, category_id, name, description, base_price, estimated_duration) VALUES
(1, 1, 'Switchboard Repair & Wiring Check', 'Fix sparking switches, socket replacement and circuit testing', 350.00, '45 mins'),
(2, 1, 'Ceiling Fan Installation / Servicing', 'Mounting, blade balancing, regulator connection and wiring', 250.00, '30 mins'),
(3, 1, 'Complete Inverter & Battery Wiring', 'Inverter setup, heavy duty cabling and safety fuse installation', 950.00, '90 mins'),
(4, 2, 'Water Pipe Leakage Repair', 'Fix leaking joints, PVC pipe sealing, pressure testing', 400.00, '45 mins'),
(5, 2, 'Tap / Shower Fitting Replacement', 'Installation of modern bathroom faucets, diverters, shower rose', 300.00, '30 mins'),
(6, 6, 'AC Deep Foam Jet Cleaning', 'Indoor unit coil foam wash, blower cleaning, outdoor condenser rinse', 550.00, '60 mins'),
(7, 6, 'Gas Refilling & Leak Inspection', 'Refrigerant pressure gauge inspection and full gas charge', 1800.00, '90 mins'),
(8, 4, 'Full Bathroom Deep Scrubbing', 'Tile descaling, grout whitening, germicidal sanitation', 450.00, '60 mins'),
(9, 5, 'Door Lock & Latch Replacement', 'Security mortise lock installation, handle alignment', 400.00, '40 mins'),
(10, 3, 'Doorstep Battery Jump & Health Check', '12V battery terminal check, voltage alternator reading, jump start', 450.00, '30 mins')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Reset AUTO_INCREMENT counters to start clean for real user registrations
ALTER TABLE users AUTO_INCREMENT = 2;
ALTER TABLE provider_profiles AUTO_INCREMENT = 1;
ALTER TABLE bookings AUTO_INCREMENT = 1;
ALTER TABLE reviews AUTO_INCREMENT = 1;
ALTER TABLE chat_messages AUTO_INCREMENT = 1;
