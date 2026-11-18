-- =============================================================================
-- ServiceConnect - Sample Seed Data
-- =============================================================================

USE serviceconnect_db;

-- -----------------------------------------------------------------------------
-- 1. SEED USERS
-- Password for all seed users is 'password123'
-- BCrypt Hash: $2a$10$7Z8lqH5hQ8s2z1Y0X7e6cuA4Q1q2w3e4r5t6y7u8i9o0p1a2s3d4f
-- -----------------------------------------------------------------------------
INSERT INTO users (id, email, password_hash, full_name, phone, role, status) VALUES
(1, 'anita@example.com', '$2a$10$e8p2c6/7qZ8k1s1X0e5cuA4Q1q2w3e4r5t6y7u8i9o0p1a2s3d4f', 'Anita Desai', '+91 98765 43210', 'CUSTOMER', 'ACTIVE'),
(2, 'rajesh@example.com', '$2a$10$e8p2c6/7qZ8k1s1X0e5cuA4Q1q2w3e4r5t6y7u8i9o0p1a2s3d4f', 'Rajesh Sharma', '+91 98765 11111', 'PROVIDER', 'ACTIVE'),
(3, 'david@example.com', '$2a$10$e8p2c6/7qZ8k1s1X0e5cuA4Q1q2w3e4r5t6y7u8i9o0p1a2s3d4f', 'David Miller', '+91 98765 22222', 'PROVIDER', 'ACTIVE'),
(4, 'sunil@example.com', '$2a$10$e8p2c6/7qZ8k1s1X0e5cuA4Q1q2w3e4r5t6y7u8i9o0p1a2s3d4f', 'Sunil Verma', '+91 98765 33333', 'PROVIDER', 'ACTIVE'),
(5, 'maria@example.com', '$2a$10$e8p2c6/7qZ8k1s1X0e5cuA4Q1q2w3e4r5t6y7u8i9o0p1a2s3d4f', 'Maria Santos', '+91 98765 44444', 'PROVIDER', 'ACTIVE'),
(6, 'arun@example.com', '$2a$10$e8p2c6/7qZ8k1s1X0e5cuA4Q1q2w3e4r5t6y7u8i9o0p1a2s3d4f', 'Arun Kumar', '+91 98765 55555', 'PROVIDER', 'ACTIVE'),
(7, 'kiran@example.com', '$2a$10$e8p2c6/7qZ8k1s1X0e5cuA4Q1q2w3e4r5t6y7u8i9o0p1a2s3d4f', 'Kiran Rao', '+91 98765 66666', 'PROVIDER', 'ACTIVE'),
(8, 'vikram@example.com', '$2a$10$e8p2c6/7qZ8k1s1X0e5cuA4Q1q2w3e4r5t6y7u8i9o0p1a2s3d4f', 'Vikram Singh', '+91 98765 77777', 'CUSTOMER', 'ACTIVE'),
(9, 'kavita@example.com', '$2a$10$e8p2c6/7qZ8k1s1X0e5cuA4Q1q2w3e4r5t6y7u8i9o0p1a2s3d4f', 'Kavita Patel', '+91 98765 88888', 'CUSTOMER', 'ACTIVE'),
(10, 'admin@serviceconnect.com', '$2a$10$e8p2c6/7qZ8k1s1X0e5cuA4Q1q2w3e4r5t6y7u8i9o0p1a2s3d4f', 'System Administrator', '+91 98765 99999', 'ADMIN', 'ACTIVE');

-- -----------------------------------------------------------------------------
-- 2. SEED CUSTOMER ADDRESSES
-- -----------------------------------------------------------------------------
INSERT INTO customer_addresses (id, user_id, label, address_line1, address_line2, city, state, postal_code, is_default) VALUES
(1, 1, 'Home', 'Flat 402, Green Valley Apartments', 'Oak Street', 'Downtown', 'Metro State', '500001', TRUE),
(2, 1, 'Office', 'Suite 305, Tech Hub Tower', 'IT Corridor', 'Metro Core', 'Metro State', '500081', FALSE),
(3, 8, 'Home', 'Villa 12, Palm Meadows', 'Airport Road', 'Northside', 'Metro State', '500032', TRUE),
(4, 9, 'Home', 'House 56, Lotus Enclave', 'Lake View', 'Westside', 'Metro State', '500045', TRUE);

-- -----------------------------------------------------------------------------
-- 3. SEED SERVICE CATEGORIES
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
(10, 'Pest Control', 'pest-control', '🐜', 'Termite control, cockroach treatment, bed bugs eradication & organic pest care');

-- -----------------------------------------------------------------------------
-- 4. SEED PROVIDER PROFILES
-- -----------------------------------------------------------------------------
INSERT INTO provider_profiles (id, user_id, business_name, category_id, bio, experience_years, service_area, profile_image, is_verified, is_available, rating_avg, rating_count, starting_price) VALUES
(1, 2, 'Sharma Spark Electricals', 1, 'Licensed master electrician with over 8 years experience in residential wiring, appliance setups, switchboard repairs and LED installations.', 8, 'Downtown, Westside, Metro Core', 'https://images.unsplash.com/photo-1540569014015-19a7be504e3a?w=150', TRUE, TRUE, 4.90, 38, 350.00),
(2, 3, 'Miller Pro Plumbing Solutions', 2, 'Certified plumbing specialist addressing pipe leakages, bathroom fixture installations, geyser plumbing, and water tank blockages.', 6, 'Northside, Suburbia, Metro Core', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150', TRUE, TRUE, 4.80, 42, 400.00),
(3, 4, 'FrostAir AC Technicians', 6, 'Dedicated HVAC technician specializing in split, window and inverter AC servicing, gas charging, filter sanitization and PCB repairs.', 10, 'Eastside, Central, Downtown', 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150', TRUE, TRUE, 4.95, 64, 500.00),
(4, 5, 'SparkleClean Home Care', 4, 'Residential deep cleaning specialist offering kitchen sanitization, sofa shampooing, bathroom scrubbing, and post-renovation cleanup.', 5, 'Westside, Downtown, Bayview', 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150', TRUE, TRUE, 4.75, 29, 450.00),
(5, 6, 'MasterCraft Woodworks', 5, 'Expert carpenter for furniture restoration, door hinge repairs, modular wardrobe assemblies, sliding door fixes and custom shelves.', 12, 'Downtown, Northside, Eastside', 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150', TRUE, TRUE, 4.85, 51, 400.00),
(6, 7, 'Precision Auto Diagnostics', 3, 'Doorstep automobile mechanic for car battery diagnostics, brake pad inspection, engine oil swap, and emergency roadside inspection.', 7, 'Eastside, Highway Belt, South Core', 'https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=150', TRUE, TRUE, 4.70, 22, 450.00);

-- -----------------------------------------------------------------------------
-- 5. SEED SERVICES
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
(10, 3, 'Doorstep Battery Jump & Health Check', '12V battery terminal check, voltage alternator reading, jump start', 450.00, '30 mins');

-- -----------------------------------------------------------------------------
-- 6. SEED PROVIDER SERVICES
-- -----------------------------------------------------------------------------
INSERT INTO provider_services (id, provider_id, service_id, custom_price, custom_description, is_available) VALUES
(1, 1, 1, 350.00, 'Sharma Spark electrical switchboard servicing', TRUE),
(2, 1, 2, 250.00, 'Ceiling fan mounting with high-speed testing', TRUE),
(3, 1, 3, 950.00, 'Safe inverter installation with copper earthing check', TRUE),
(4, 2, 4, 400.00, 'Pipe joint welding and leak stoppage', TRUE),
(5, 2, 5, 300.00, 'Bathroom tap replacement with warranty fitment', TRUE),
(6, 3, 6, 550.00, 'High pressure jet cleaning with antibacterial spray', TRUE),
(7, 3, 7, 1800.00, 'Genuine Freon / R32 gas refill with leak test', TRUE),
(8, 4, 8, 450.00, 'Complete bathroom stain removal', TRUE),
(9, 5, 9, 400.00, 'Cylinder lock installation with 3 keys tested', TRUE),
(10, 6, 10, 450.00, 'Instant vehicle booster start with battery check', TRUE);

-- -----------------------------------------------------------------------------
-- 7. SEED PROVIDER AVAILABILITY (Working Days & Shifts)
-- -----------------------------------------------------------------------------
INSERT INTO provider_availability (provider_id, day_of_week, start_time, end_time, slot_duration_minutes, is_active) VALUES
(1, 1, '08:00:00', '20:00:00', 60, TRUE),
(1, 2, '08:00:00', '20:00:00', 60, TRUE),
(1, 3, '08:00:00', '20:00:00', 60, TRUE),
(1, 4, '08:00:00', '20:00:00', 60, TRUE),
(1, 5, '08:00:00', '20:00:00', 60, TRUE),
(1, 6, '08:00:00', '20:00:00', 60, TRUE),
(2, 1, '07:30:00', '19:30:00', 60, TRUE),
(2, 2, '07:30:00', '19:30:00', 60, TRUE),
(2, 3, '07:30:00', '19:30:00', 60, TRUE),
(2, 4, '07:30:00', '19:30:00', 60, TRUE),
(2, 5, '07:30:00', '19:30:00', 60, TRUE),
(2, 6, '07:30:00', '19:30:00', 60, TRUE),
(3, 1, '09:00:00', '21:00:00', 60, TRUE),
(3, 2, '09:00:00', '21:00:00', 60, TRUE),
(3, 3, '09:00:00', '21:00:00', 60, TRUE),
(3, 4, '09:00:00', '21:00:00', 60, TRUE),
(3, 5, '09:00:00', '21:00:00', 60, TRUE);

-- -----------------------------------------------------------------------------
-- 8. SEED BOOKINGS
-- -----------------------------------------------------------------------------
INSERT INTO bookings (id, booking_reference, customer_id, provider_id, service_id, booking_date, time_slot, status, customer_name, customer_phone, customer_address, problem_description, price) VALUES
(1, 'SC-89214', 1, 1, 1, '2026-11-20', '11:00 AM', 'ACCEPTED', 'Anita Desai', '+91 98765 43210', 'Flat 402, Green Valley Apartments, Downtown', 'Master bedroom main switch is sparking when turning on AC.', 350.00),
(2, 'SC-67312', 1, 2, 4, '2026-11-19', '03:30 PM', 'PENDING', 'Anita Desai', '+91 98765 43210', 'Flat 402, Green Valley Apartments, Downtown', 'Continuous dripping under the kitchen sink basin.', 400.00),
(3, 'SC-44109', 1, 3, 6, '2026-11-10', '02:00 PM', 'COMPLETED', 'Anita Desai', '+91 98765 43210', 'Flat 402, Green Valley Apartments, Downtown', 'Pre-summer deep cleaning for living room split AC.', 550.00),
(4, 'SC-33108', 8, 1, 2, '2026-11-12', '10:00 AM', 'COMPLETED', 'Vikram Singh', '+91 98765 77777', 'Villa 12, Palm Meadows, Northside', 'New ceiling fan installation in guest room.', 250.00),
(5, 'SC-12095', 9, 2, 5, '2026-11-04', '01:00 PM', 'COMPLETED', 'Kavita Patel', '+91 98765 88888', 'House 56, Lotus Enclave, Westside', 'Bathroom shower mixer replacement.', 300.00);

-- -----------------------------------------------------------------------------
-- 9. SEED REVIEWS (Tied to Completed Bookings)
-- -----------------------------------------------------------------------------
INSERT INTO reviews (id, booking_id, customer_id, provider_id, rating, comment, created_at) VALUES
(1, 3, 1, 3, 5, 'Cooling was instantly restored. Sunil brought pressure washer tools and kept the room spotless.', '2026-11-10 16:30:00'),
(2, 4, 8, 1, 5, 'Very professional electrical installation. Clean work, verified wiring safety before leaving.', '2026-11-12 11:45:00'),
(3, 5, 9, 5, 5, 'Saved our bathroom from a severe pipe burst. Came quickly on a Sunday!', '2026-11-04 15:00:00');

-- -----------------------------------------------------------------------------
-- 10. SEED FAVOURITES
-- -----------------------------------------------------------------------------
INSERT INTO favourites (customer_id, provider_id) VALUES
(1, 1),
(1, 3),
(8, 1);
