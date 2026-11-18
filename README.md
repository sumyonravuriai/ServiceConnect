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

The database schema is normalized and stored under [`database/schema.sql`](file:///c:/Users/SUMYON/OneDrive/Documents/sem%203/SERVICE_CONNECT/database/schema.sql):

| # | Table Name | Description | Key Relationships |
|---|---|---|---|
| 1 | `users` | Accounts, credentials (BCrypt), and roles | PK: `id`, Unique: `email` |
| 2 | `customer_addresses` | Doorstep addresses saved by customers | FK: `user_id` → `users(id)` |
| 3 | `service_categories` | Trade master list (Electrician, Plumber, etc.) | PK: `id`, Unique: `name`, `slug` |
| 4 | `provider_profiles` | Business details, coverage area, ratings | FK: `user_id` → `users(id)`, `category_id` → `service_categories(id)` |
| 5 | `services` | Standard service items | FK: `category_id` → `service_categories(id)` |
| 6 | `provider_services` | Custom rates and services configured by provider | FK: `provider_id`, `service_id` |
| 7 | `provider_availability`| Provider working days and shift hours | FK: `provider_id` |
| 8 | `bookings` | Core transaction table with state machine | FK: `customer_id`, `provider_id`, `service_id` |
| 9 | `reviews` | Verified feedback (1 review per completed booking) | FK: `booking_id` (Unique), `customer_id`, `provider_id` |
| 10| `favourites` | Customer bookmarked providers | FK: `customer_id`, `provider_id` |

### Key Constraints & Integrity Rules:
1. **Double-Booking Prevention**: Index on `(provider_id, booking_date, time_slot)` ensures that queries quickly verify conflicting reservations.
2. **Review Integrity**: `UNIQUE KEY (booking_id)` in the `reviews` table enforces that only one review can be written per booking.
3. **Audit Trails**: `created_at` and `updated_at` timestamps on all transactions.

---

## 🚀 Spring Boot REST API Documentation

### Authentication (`/api/auth`)
- `POST /api/auth/login`: Authenticate using email and password.
- `POST /api/auth/register/customer`: Register a customer account with address.
- `POST /api/auth/register/provider`: Register a service provider application.

### Categories & Providers (`/api`)
- `GET /api/categories`: List all active trade categories.
- `GET /api/providers`: Search and filter providers (`?category=&city=&maxPrice=&minRating=&sortBy=`).
- `GET /api/providers/{id}`: Detailed profile with custom service prices and customer reviews.

### Bookings (`/api/bookings`)
- `POST /api/bookings`: Create a new appointment booking.
- `GET /api/bookings/{id}`: Retrieve booking by ID.
- `GET /api/bookings/customer/{customerId}`: List bookings for customer dashboard.
- `GET /api/bookings/provider/{providerId}`: List job queue for provider dashboard.
- `PUT /api/bookings/{id}/status`: Update status (`ACCEPTED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`, `REJECTED`).

### Reviews (`/api/reviews`)
- `POST /api/reviews`: Submit review for completed booking (automatically updates provider rating average).
- `GET /api/reviews/provider/{providerId}`: Retrieve all reviews for a provider.

### Admin (`/api/admin`)
- `GET /api/admin/stats`: KPI dashboard stats (customers, providers, bookings, revenue).
- `GET /api/admin/users`: List all platform users.
- `PUT /api/admin/users/{id}/status`: Suspend or activate user.
- `PUT /api/admin/providers/{id}/approve`: Verify and activate provider profile.

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
│   │   ├── provider-dashboard.html  # Provider Job Management & Schedule
│   │   ├── admin-dashboard.html     # Administrator Analytics & Approvals
│   │   ├── login.html               # Sign In (with Quick Demo Logins)
│   │   └── register.html            # Registration (Customer & Provider)
│   ├── css/
│   │   ├── styles.css               # Main Design System & Variables
│   │   ├── dashboard.css            # Sidebar Layout, Tables & Cards
│   │   └── booking.css              # Step Wizard & Slot Selector
│   └── js/
│       ├── api.js                   # REST API Client with Fallback Previews
│       ├── auth.js                  # Session & Role Management
│       └── main.js                  # Utilities, Toast Alerts & Stars
├── backend/
│   ├── pom.xml                      # Maven Build Configuration (Java 21)
│   └── src/
│       ├── main/java/com/serviceconnect/
│       │   ├── ServiceConnectApplication.java
│       │   ├── config/              # SecurityConfig, WebMvcConfig
│       │   ├── controller/          # REST Controllers
│       │   ├── dao/                 # JdbcTemplate DAO Implementations
│       │   ├── dto/                 # Request/Response DTOs
│       │   ├── exception/           # Global Exception Handling
│       │   ├── model/               # Domain Models / Entities
│       │   └── service/             # Business Logic & State Machines
│       └── main/resources/
│           └── application.properties # MySQL & Server Port Settings
├── database/
│   ├── schema.sql                   # 10 Normalized MySQL Tables
│   ├── sample_data.sql              # Realistic Seed Data with Passwords
│   └── queries.sql                  # Comprehensive Evaluation Queries
└── README.md
```

---

## ⚙️ Installation & Setup Instructions

### 🐳 Option A: 1-Click Production Run with Docker (Recommended)
You can launch the entire stack (MySQL 8.0, Java 21 Spring Boot backend, and Nginx frontend) with one command:
```bash
docker-compose up --build
```
- **Frontend App**: `http://localhost:8000`
- **Backend REST API**: `http://localhost:8080/api`
- **MySQL Database**: `localhost:3306` (pre-seeded with schema & sample data)

---

### 💻 Option B: Manual Local Development Setup

#### 1. Database Setup (MySQL 8.0)
1. Open MySQL Command Line Client or MySQL Workbench:
   ```cmd
   mysql -u root -p
   ```
2. Execute the schema script:
   ```sql
   SOURCE database/schema.sql;
   ```
3. Populate with seed sample data:
   ```sql
   SOURCE database/sample_data.sql;
   ```

### 2. Backend Setup (Java Spring Boot)
1. Ensure Java 21 LTS is configured:
   ```powershell
   $env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.8-hotspot"
   ```
2. Adjust database credentials in `backend/src/main/resources/application.properties` (or set environment variables `DB_USERNAME` and `DB_PASSWORD`).
3. Compile and launch the Spring Boot application:
   ```powershell
   cd backend
   mvn spring-boot:run
   ```
   The backend API will start on **`http://localhost:8080/api`**.

### 3. Frontend Setup
- **Integrated mode**: Visiting **`http://localhost:8080/`** directly serves the frontend through Spring Boot's WebMvc resource mapper.
- **Standalone mode**: You can also double click `frontend/index.html` to open it directly in Google Chrome, Microsoft Edge, or any modern browser.

---

## 🧪 Sample Workflows & Testing Guide

### Pre-Configured Test Credentials
| Role | Email | Password | Pre-populated Dashboard Features |
|---|---|---|---|
| **Customer** | `anita@example.com` | `password123` | Active & past bookings, address book, write review |
| **Provider** | `rajesh@example.com` | `password123` | Incoming job requests, Accept/Reject, Services list |
| **Admin** | `admin@serviceconnect.com` | `password123` | Platform metrics, provider approvals, user management |

*Quick shortcut: Click on any of the **"One-Click Quick Logins"** buttons on [`pages/login.html`](file:///c:/Users/SUMYON/OneDrive/Documents/sem%203/SERVICE_CONNECT/frontend/pages/login.html) to instantly switch between Customer, Provider, and Admin accounts without manual typing!*
