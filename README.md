# ServiceConnect – Local Service Provider Discovery & Booking Platform

> **"Find • Compare • Book • Relax"**  
> *Smart Services. Stronger Community.*

ServiceConnect is a full-stack web application and DBMS project that connects homeowners and businesses with trusted, verified local service professionals (Electricians, Plumbers, Mechanics, Cleaners, Carpenters, AC Repair Technicians, Painters, Tutors, Cooks, and more).

---

## 📑 Table of Contents
1. [Platform Overview & Architecture](#-platform-overview--architecture)
2. [Technology Stack](#-technology-stack)
3. [User Roles & Key Features](#-user-roles--key-features)
4. [Relational Database Design (MySQL)](#-relational-database-design-mysql)
5. [Spring Boot REST API Documentation](#-spring-boot-rest-api-documentation)
6. [Directory Structure](#-directory-structure)
7. [Installation & Setup Instructions](#-installation--setup-instructions)
8. [Sample Workflows & Testing Guide](#-sample-workflows--testing-guide)

---

## 🏛 Platform Overview & Architecture

ServiceConnect bridges the gap between local service technicians and neighborhood customers by offering:
- **Upfront transparent pricing** without hidden charges.
- **Controlled booking state lifecycle** (`PENDING` → `ACCEPTED` → `IN_PROGRESS` → `COMPLETED` or `CANCELLED`).
- **Double-booking prevention** via database constraints and validation.
- **Verified reviews** restricted exclusively to customers who have completed an appointment.
- **Multi-role access control** for Customers, Providers, and Administrators.
- **Real-Time Live Updates (SSE)**: Instant Server-Sent Events notify providers of incoming booking requests and broadcast status transitions live without page reloads.
- **Direct In-App Coordination Chat**: Real-time communication between customer and assigned technician inside the booking tracker.
- **Visual 4-Step Live Tracking Stepper**: `Requested` → `Confirmed` → `In Progress` → `Completed`.
- **Commercial Tax Invoice Generator**: Automatic GST (CGST 9% + SGST 9%) calculation with clean print/PDF layout.
- **GPS Geolocation Auto-Detection**: One-click geolocation lock for instant discovery of neighborhood professionals.

```
+----------------------------------------------------------------+
|             Frontend (HTML5 / Modern CSS3 / Vanilla JS)        |
|  - Minimalist navy design system (--color-primary: #0f172a)    |
|  - 5-step booking wizard with live slot selector               |
|  - Real-time customer, provider & admin management dashboards  |
+----------------------------------------------------------------+
                               |
                        Fetch API (JSON)
                               v
+----------------------------------------------------------------+
|             Backend (Java 21 / Spring Boot 4.x / 3.x)          |
|  - Spring Web MVC REST Controllers                             |
|  - Spring Security (BCrypt Password Hashing, RBAC)             |
|  - Spring JdbcTemplate with Prepared Statements & Transactions |
|  - Centralized Exception Handling (@RestControllerAdvice)      |
+----------------------------------------------------------------+
                               |
                           JDBC Driver
                               v
+----------------------------------------------------------------+
|                 Database (MySQL 8.0 Relational DBMS)           |
|  - 10 Normalized tables with PK, FK, and Unique Constraints    |
|  - Automated rating recalculation queries                      |
|  - Double-booking prevention indexes                           |
+----------------------------------------------------------------+
```

---

## 💻 Technology Stack

### Frontend
- **HTML5**: Semantic document layout with accessible form controls and ARIA attributes.
- **CSS3**: Custom Design System using CSS variables, flexbox, CSS grid, and high contrast typography (Inter).
- **Vanilla JavaScript**: Pure ECMAScript (no heavy frontend frameworks like React or Angular).
- **Fetch API**: Asynchronous REST client communication with graceful fallback.

### Backend
- **Language**: Java 21 LTS
- **Framework**: Spring Boot (`spring-boot-starter-webmvc`, `spring-boot-starter-tomcat`)
- **Data Access**: Spring JDBC (`JdbcTemplate`) using Prepared Statements
- **Security**: Spring Security with `BCryptPasswordEncoder`
- **Build Tool**: Apache Maven 3.9+

### Database
- **Engine**: MySQL Server 8.0+
- **Dialect**: InnoDB with Foreign Key constraints and UTF-8 Unicode encoding (`utf8mb4`).

---

## 👥 User Roles & Key Features

### 1. Customer
- **Discovery**: Search services by category and neighborhood area.
- **Comparison**: Compare hourly/itemized rates, average ratings, and experience.
- **5-Step Booking**:
  1. Service selection
  2. Date picking
  3. Time slot selection (real-time conflict check)
  4. Doorstep address and problem notes
  5. Review & instant confirmation with reference (`SC-XXXXX`)
- **Dashboard**: Track booking states, cancel eligible bookings, manage saved addresses, bookmark favourite providers, and write verified reviews.

### 2. Service Provider
- **Onboarding**: Register trade business, years of experience, and service areas.
- **Booking Management**: Accept or reject incoming requests; transition orders from `ACCEPTED` to `IN_PROGRESS` and `COMPLETED`.
- **Pricing & Schedule**: Manage custom prices for tasks, set active days and shift hours.
- **Reputation**: View authentic customer reviews and aggregate rating scores.

### 3. Administrator
- **Analytics KPI**: View platform metrics (Total Users, Providers, Bookings, Revenue).
- **Verification Queue**: Screen provider applications with One-Click Approve / Reject.
- **User Governance**: Suspend or activate fraudulent accounts.
- **Catalog Management**: Add and update service categories.

---

## 🗄 Relational Database Design (MySQL)

The database schema is fully normalized into 14 relational tables stored under [`database/schema.sql`](file:///c:/Users/SUMYON/OneDrive/Documents/sem%203/SERVICE_CONNECT/database/schema.sql):

| # | Table Name | Description | Key Relationships & Constraints |
|---|---|---|---|
| 1 | `users` | Accounts, credentials (BCrypt), roles (`CUSTOMER`, `PROVIDER`, `ADMIN`), and email verification state | PK: `id`, Unique: `email` |
| 2 | `customer_addresses` | Doorstep addresses saved by customers | FK: `user_id` → `users(id)` |
| 3 | `service_categories` | Trade master list (Electrician, Plumber, AC Repair, etc.) | PK: `id`, Unique: `name`, `slug` |
| 4 | `provider_profiles` | Business details, coverage area, ratings, approval status (`PENDING_APPROVAL`, `APPROVED`, `REJECTED`, `SUSPENDED`) | FK: `user_id` → `users(id)`, `category_id` → `service_categories(id)` |
| 5 | `services` | Master catalog trade service items | FK: `category_id` → `service_categories(id)` |
| 6 | `provider_services` | Custom rates and durations configured by providers | FK: `provider_id`, `service_id` |
| 7 | `provider_availability`| Provider working days and shift hours | FK: `provider_id` |
| 8 | `bookings` | Core transaction table with state machine & unique references (`SC-XXXXX`) | FK: `customer_id`, `provider_id`, `service_id` |
| 9 | `reviews` | Verified feedback (1 review per completed booking) | FK: `booking_id` (Unique), `customer_id`, `provider_id` |
| 10| `favourites` | Customer bookmarked preferred service specialists | FK: `customer_id`, `provider_id` |
| 11| `chat_messages` | Real-time direct customer-technician coordination messages | FK: `booking_id`, `sender_id` |
| 12| `email_verifications`| Time-limited tokens and 6-digit OTP codes for account activation | FK: `user_id`, Unique: `token` |
| 13| `password_resets` | Time-limited password recovery tokens (1 hour expiry) | FK: `user_id`, Unique: `token` |
| 14| `audit_logs` | Administrative security and approval action audit logs | FK: `admin_id` |

### Key Constraints & Integrity Rules:
1. **Double-Booking Prevention**: Index on `(provider_id, booking_date, time_slot)` ensures that concurrent queries prevent overlapping appointment reservations.
2. **Review Integrity**: `UNIQUE KEY (booking_id)` in the `reviews` table strictly guarantees that only completed bookings can have exactly one review.
3. **Approved Providers Only**: Public search queries enforce `WHERE u.status = 'ACTIVE' AND p.approval_status = 'APPROVED'`, ensuring unapproved or suspended providers never appear in search results.
4. **Audit Accountability**: Comprehensive tracking of administrator approvals, rejections, and user account status updates.

---

## 🚀 Spring Boot REST API Documentation

### Authentication & Account Security (`/api/auth`)
- `POST /api/auth/register/customer`: Register real customer account and dispatches 6-digit verification OTP.
- `POST /api/auth/register/provider`: Register service provider application (`PENDING_APPROVAL` status).
- `POST /api/auth/verify-email`: Activate account using 6-digit OTP or token verification link.
- `POST /api/auth/login`: Authenticate with email/password; verifies BCrypt hash, checks email activation, and issues HMAC-SHA256 JWT.
- `POST /api/auth/forgot-password`: Generates secure password reset token and sends recovery email.
- `POST /api/auth/reset-password`: Resets password using valid token and new BCrypt hash.
- `POST /api/auth/logout`: Clears security context.

### Categories & Providers (`/api`)
- `GET /api/categories`: List active trade categories.
- `GET /api/providers`: Search and filter approved providers (`?category=&city=&maxPrice=&minRating=&sortBy=`).
- `GET /api/providers/{id}`: Detailed profile with custom service prices and customer reviews.
- `GET /api/providers/user/{userId}`: Provider profile lookup for logged-in provider dashboard.

### Bookings (`/api/bookings`)
- `POST /api/bookings`: Create a new appointment booking with double-booking validation.
- `GET /api/bookings/{id}`: Retrieve booking by ID.
- `GET /api/bookings/customer/{customerId}`: List bookings for customer dashboard.
- `GET /api/bookings/provider/{providerId}`: List job queue for provider dashboard.
- `PUT /api/bookings/{id}/status`: Update status (`ACCEPTED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`, `REJECTED`).

### Reviews (`/api/reviews`)
- `POST /api/reviews`: Submit review for completed booking (automatically recalculates provider rating average).
- `GET /api/reviews/provider/{providerId}`: Retrieve all reviews for a provider.

### Administrator Controls (`/api/admin`)
- `GET /api/admin/stats`: KPI dashboard stats (customers, providers, bookings, pending approvals).
- `GET /api/admin/users`: List all platform users.
- `PUT /api/admin/users/{id}/status`: Suspend or reactivate user account.
- `GET /api/admin/providers/pending`: List all provider applications awaiting review.
- `PUT /api/admin/providers/{id}/approve`: Approve provider profile and make live in search.
- `PUT /api/admin/providers/{id}/reject`: Reject provider application with structured reason.
- `GET /api/admin/audit-logs`: Review platform governance actions.

---

## 📁 Directory Structure

```
SERVICE_CONNECT/
├── frontend/
│   ├── index.html                   # Professional Landing / Homepage
│   ├── pages/
│   │   ├── categories.html          # All Service Categories Directory
│   │   ├── providers.html           # Provider Listing, Filter & Search
│   │   ├── provider-profile.html    # Provider Profile, Services & Reviews
│   │   ├── booking.html             # 5-Step Appointment Booking Wizard
│   │   ├── customer-dashboard.html  # Customer Appointments & Reviews
│   │   ├── provider-dashboard.html  # Provider Control Center & Approval Status
│   │   ├── admin-dashboard.html     # Administrator Analytics & Approvals Queue
│   │   ├── login.html               # Secure Sign In with JWT Auth
│   │   ├── register.html            # Registration (Customer & Provider)
│   │   ├── verify-email.html        # 6-Digit OTP & One-Click Email Activation
│   │   ├── forgot-password.html     # Two-Phase Password Recovery
│   │   ├── privacy.html             # DPDP Act 2023 Compliant Privacy Policy
│   │   └── terms.html               # Platform Terms of Service
│   ├── css/
│   │   ├── styles.css               # Main Design System & Color Tokens
│   │   ├── dashboard.css            # Sidebar Layout, Tables & Badges
│   │   └── booking.css              # Step Wizard & Slot Selector
│   └── js/
│       ├── api.js                   # Central REST API Client
│       ├── auth.js                  # JWT Token & Role Authorization Manager
│       └── main.js                  # Utilities, Toast Alerts & Geolocation
├── backend/
│   ├── pom.xml                      # Maven Build Configuration (Java 21 LTS)
│   └── src/
│       ├── main/java/com/serviceconnect/
│       │   ├── ServiceConnectApplication.java
│       │   ├── config/              # SecurityConfig, JwtUtil, JwtAuthenticationFilter
│       │   ├── controller/          # REST Controllers (Auth, Admin, Booking, Provider)
│       │   ├── dao/                 # JdbcTemplate DAO Implementations
│       │   ├── dto/                 # Request/Response DTOs
│       │   ├── exception/           # Global Exception Handling
│       │   ├── model/               # Domain Models / Entities
│       │   └── service/             # Business Logic, EmailService & State Machines
│       └── main/resources/
│           └── application.properties # MySQL, SMTP, JWT & Port Settings
├── database/
│   ├── schema.sql                   # 14 Normalized MySQL Tables
│   ├── production_seed.sql          # Clean Master Seed (ZERO fake accounts)
│   ├── sample_data.sql              # Development / Testing Mock Seed
│   └── queries.sql                  # Comprehensive Evaluation Queries
├── .env.example                     # Production Environment Configuration Template
└── README.md
```

---

## ⚙️ Installation & Setup Instructions

### 1. Database Setup (MySQL 8.0+)
1. Open MySQL Command Line Client or MySQL Workbench:
   ```cmd
   mysql -u root -p
   ```
2. Initialize the schema:
   ```sql
   SOURCE database/schema.sql;
   ```
3. Initialize the seed data:
   - **For Real-Time Live Production (Clean start)**:
     ```sql
     SOURCE database/production_seed.sql;
     ```
     *(Seeds ONLY master categories, standard trade services catalog, and system admin `admin@serviceconnect.com`. Zero dummy customers, zero fake providers, zero fake bookings!)*
   - **For Local Development Preview**:
     ```sql
     SOURCE database/sample_data.sql;
     ```

### 2. Backend Setup (Java Spring Boot)
1. Ensure Java 21 LTS is active:
   ```cmd
   set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.8-hotspot
   ```
2. Configure credentials in `backend/src/main/resources/application.properties` or via environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `MAIL_HOST`, `MAIL_USERNAME`, `MAIL_PASSWORD`).
3. Compile and launch the Spring Boot application:
   ```cmd
   cd backend
   mvn spring-boot:run
   ```
   The backend API starts on **`http://localhost:8080/api`**.

### 3. Frontend Setup
- Visit **`http://localhost:8000/`** (or open `frontend/index.html`).

---

## 🛡️ Security & Public Launch Checklist

- [x] **Password Protection**: BCrypt hashing with standard salt rounds. Plaintext passwords never stored or logged.
- [x] **Session Security**: Stateless HMAC-SHA256 JWT tokens with automatic expiry.
- [x] **Email Verification**: Mandatory 6-digit OTP / token verification before account activation.
- [x] **Provider Vetting**: Multi-step onboarding workflow; only `APPROVED` providers appear in search.
- [x] **SQL Injection Defense**: 100% parameterized Spring JDBC Prepared Statements.
- [x] **Booking Guard**: Real-time slot conflict checking prevents double bookings.
- [x] **Authentic Reviews**: Reviews restricted to confirmed `COMPLETED` bookings (1 per booking).
- [x] **Legal Compliance**: Privacy Policy compliant with the Indian Digital Personal Data Protection (DPDP) Act, 2023, and comprehensive Terms of Service.
- [x] **Zero Fake Accounts in Production**: Database contains clean master seed data for genuine user onboarding.
