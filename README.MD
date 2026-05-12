# Grover Hospital — Backend

A REST API backend for a multi-tenant hospital operations platform, covering a public-facing website, a patient self-service portal, and an internal admin panel. Built with **Spring Boot 4**, **Java 17**, **PostgreSQL**, and **JWT-based** authentication.

> **Status:** Active development. Not yet deployed. Domain modelling, persistence, security, and the bulk of REST endpoints are in place; notification delivery currently runs through console-only sender implementations pending production wiring.

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Tech Stack](#tech-stack)
4. [Domain Model](#domain-model)
5. [API Surface](#api-surface)
6. [Authentication & Authorization](#authentication--authorization)
7. [File Storage & Encryption](#file-storage--encryption)
8. [Notifications](#notifications)
9. [Project Structure](#project-structure)
10. [Getting Started](#getting-started)
11. [Configuration](#configuration)
12. [Database Migrations](#database-migrations)
13. [Running Tests](#running-tests)
14. [API Documentation](#api-documentation)
15. [Roadmap](#roadmap)
16. [License](#license)

---

## Overview

Grover Hospital's backend is organized around three audiences, each served by a dedicated set of controllers:

- **Public** — anonymous endpoints powering the marketing site: departments, team members, health packages, promotions, blog posts, testimonials, and public feedback submission.
- **Patient portal** — authenticated endpoints (`/portal/...`) where registered patients manage their profile, place and cancel bookings, view test results, download encrypted result files, and submit feedback.
- **Admin panel** — privileged endpoints (`/admin/...`) where staff manage every resource: bookings, departments, consultant schedules, packages, results, team members, blog posts, testimonials, promotions, and feedback — plus image uploads.

The system treats `Patient` and `Admin` as distinct principal types, authenticated through separate login endpoints and authorized through a shared `Role` enum.

---

## Architecture

The codebase follows a conventional layered Spring Boot architecture:

```
┌──────────────────────────────────────────────────────────┐
│  Controllers (public · /portal · /admin)                 │
│  ─ DTO validation, request shaping, response mapping     │
└──────────────────────────────────────────────────────────┘
                          │
┌──────────────────────────────────────────────────────────┐
│  Services                                                │
│  ─ Domain logic, transactions, cross-entity orchestration│
│  ─ Notification, encryption, audit, file storage         │
└──────────────────────────────────────────────────────────┘
                          │
┌──────────────────────────────────────────────────────────┐
│  Repositories (Spring Data JPA)                          │
│  ─ Persistence, query methods                            │
└──────────────────────────────────────────────────────────┘
                          │
┌──────────────────────────────────────────────────────────┐
│  PostgreSQL  +  Encrypted file storage (filesystem)      │
└──────────────────────────────────────────────────────────┘
```

Cross-cutting concerns sit in dedicated packages:

- `security/` — JWT filter, token provider, custom `UserDetailsService`, `SecurityUtils` for principal lookup.
- `config/` — `SecurityConfig`, `CorsConfig`, `JwtConfig`, `WebMvcConfig`, `FileStorageConfig`, `AuditConfig`, and a `DataSeeder` for development bootstrap.
- `exception/` — typed exceptions (`ResourceNotFoundException`, `BadRequestException`, `UnauthorizedException`, `FileStorageException`) and a `@RestControllerAdvice` `GlobalExceptionHandler` for consistent error responses.
- `service/notification/` — channel-agnostic notification dispatch with pluggable senders.

---

## Tech Stack

| Layer            | Technology                                                          |
|------------------|---------------------------------------------------------------------|
| Language         | Java 17                                                             |
| Framework        | Spring Boot 4.0.6                                                   |
| Web              | Spring Web MVC                                                      |
| Security         | Spring Security + JJWT (jjwt-api / impl / jackson 0.12.x)           |
| Persistence      | Spring Data JPA, Hibernate, PostgreSQL                              |
| Migrations       | Flyway (`db/migration/V1__create_users_and_auth.sql`)               |
| API Docs         | springdoc-openapi (Swagger UI)                                      |
| Build            | Maven (wrapper included)                                            |
| Boilerplate      | Lombok                                                              |
| File storage     | Local filesystem, AES-encrypted at rest                             |

---

## Domain Model

The system models 15 persistent entities. Tables are explicit via `@Table` annotations and several use composite indexes for query-heavy paths (`access_logs`, `bookings`, `feedback`, `results`).

| Entity              | Table              | Purpose                                                                  |
|---------------------|--------------------|--------------------------------------------------------------------------|
| `Patient`           | `patients`         | End users of the patient portal. Authenticates with email + password.    |
| `Admin`             | `admins`           | Internal staff users with role-scoped privileges.                        |
| `Department`        | `departments`      | Clinical departments (cardiology, paediatrics, etc.). Slug-addressable.  |
| `TeamMember`        | `team_members`     | Doctors, nurses, and consultants displayed on the public site.           |
| `ConsultantSchedule`| `consultant_schedules` | Per-consultant availability, scoped by department.                   |
| `HealthPackage`     | `health_packages`  | Bundled service offerings (e.g. "Annual Checkup"). Slug-addressable.     |
| `PackageTier`       | `package_tiers`    | Pricing/feature tiers within a `HealthPackage`.                          |
| `Booking`           | `bookings`         | Patient appointment or package booking. Drives the booking lifecycle.    |
| `Result`            | `results`          | Test/diagnostic results owned by a patient; status-tracked.              |
| `ResultFile`        | `results_files`    | Encrypted attachments (PDFs, scans, lab reports) linked to a `Result`.   |
| `BlogPost`          | `blog_posts`       | CMS-style articles. Drafted by admins, slug-addressable when published.  |
| `Testimonial`       | `testimonials`     | Patient testimonials, admin-approved before going public.                |
| `Promotion`         | `promotions`       | Time-bound marketing campaigns; admin-toggleable.                        |
| `Feedback`          | `feedback`         | Submissions from public visitors or authenticated patients.              |
| `AccessLog`         | `access_logs`      | Audit trail of sensitive operations (e.g. result-file access).           |

### Enums

- `Role` — patient vs. admin authorities.
- `BookingStatus` — booking lifecycle states.
- `BookingType` — appointment vs. package booking.
- `ResultStatus` — result lifecycle (pending / ready / notified).
- `FeedbackSource` — distinguishes public-form feedback from portal-authenticated feedback.

---

## API Surface

All endpoints are JSON. Authenticated endpoints require an `Authorization: Bearer <jwt>` header.

### Authentication — `/auth`

| Method | Path              | Description                              |
|--------|-------------------|------------------------------------------|
| POST   | `/auth/register`  | Register a new patient.                  |
| POST   | `/auth/login`     | Patient login → access + refresh tokens. |
| POST   | `/auth/admin/login` | Admin login → access + refresh tokens. |
| POST   | `/auth/refresh`   | Exchange a refresh token for a new access token. |

### Public — anonymous

| Resource         | Endpoints                                                                 |
|------------------|---------------------------------------------------------------------------|
| Departments      | `GET /departments`, `GET /departments/{slug}`                             |
| Team             | `GET /team`                                                               |
| Schedules        | `GET /consultant-schedule`, `GET /consultant-schedule/department/{id}`    |
| Packages         | `GET /packages`, `GET /packages/{slug}`                                   |
| Promotions       | `GET /promotions`                                                         |
| Blog             | `GET /blog`, `GET /blog/{slug}`                                           |
| Testimonials     | `GET /testimonials`                                                       |
| Feedback         | `POST /feedback`                                                          |

### Patient Portal — `/portal/...` (patient JWT required)

| Resource | Endpoints                                                                            |
|----------|--------------------------------------------------------------------------------------|
| Profile  | `GET /portal/profile`, `PUT /portal/profile`, `PUT /portal/profile/password`         |
| Bookings | `POST /portal/bookings`, `GET /portal/bookings`, `GET /portal/bookings/{id}`, `PUT /portal/bookings/{id}/cancel` |
| Results  | `GET /portal/results`, `GET /portal/results/{id}`, `GET /portal/results/{resultId}/files/{fileId}/download` |
| Feedback | `POST /portal/feedback`                                                              |

### Admin Panel — `/admin/...` (admin JWT required)

| Resource             | Endpoints                                                                 |
|----------------------|---------------------------------------------------------------------------|
| Bookings             | `GET /admin/bookings`, `GET /admin/bookings/{id}`, `PUT /admin/bookings/{id}/status`, `PUT /admin/bookings/{id}/notes` |
| Departments          | Full CRUD under `/admin/departments`                                      |
| Team                 | Full CRUD under `/admin/team`                                             |
| Consultant Schedules | Full CRUD under `/admin/consultant-schedule`                              |
| Packages             | Full CRUD under `/admin/packages`, plus tier management at `/admin/packages/{packageId}/tiers` and `/admin/packages/tiers/{tierId}` |
| Promotions           | Full CRUD plus `PATCH /admin/promotions/{id}/toggle`                      |
| Blog                 | Full CRUD plus `PUT /admin/blog/{id}/publish`                             |
| Testimonials         | Full CRUD plus `PATCH /admin/testimonials/{id}/approve`                   |
| Results              | `POST /admin/results` (multipart), `GET` list & detail, `POST /admin/results/{id}/notify`, file download, delete |
| Feedback             | `GET /admin/feedback`, `GET /admin/feedback/stats`, `GET /admin/feedback/{id}`, `PATCH /admin/feedback/{id}/read`, `DELETE /admin/feedback/{id}` |
| Image upload         | `POST /admin/upload/image` (multipart)                                    |

A complete, interactive specification is available via Swagger UI once the application is running — see [API Documentation](#api-documentation).

---

## Authentication & Authorization

Authentication is JWT-based, stateless, and bifurcated by principal type:

- **Patients** register through `/auth/register` and authenticate at `/auth/login`.
- **Admins** are seeded (see `DataSeeder`) and authenticate at `/auth/admin/login`.
- Both flows return an **access token** plus a **refresh token**; `/auth/refresh` rotates the access token without re-authenticating.

Under the hood:

- `JwtAuthenticationFilter` runs once per request, extracting the bearer token, validating it through `JwtTokenProvider`, and resolving the principal via `CustomUserDetailsService.loadUserByIdAndRole(userId, role)`. The token carries the user's `Role`, so the filter can route lookups to the correct repository (`PatientRepository` or `AdminRepository`).
- `UserPrincipal` is the unified `UserDetails` implementation exposed to Spring Security.
- `SecurityUtils` provides convenience accessors for the current authenticated principal inside service code.
- `SecurityConfig` (`@EnableWebSecurity`) registers the JWT filter, configures stateless session management, and locks down `/admin/**` and `/portal/**` while leaving public endpoints open.

Patient-owned resources (bookings, results, files) are additionally checked at the service layer: a patient can only read or mutate records that belong to them.

---

## File Storage & Encryption

Result files (`ResultFile`) are uploaded by admins and downloaded by both admins and the owning patient. To keep PHI off the filesystem in plaintext:

- **`EncryptionService`** encrypts the file bytes on upload and decrypts them on download.
- **`FileStorageService`** writes ciphertext to a configured upload directory (see `FileStorageConfig` + `app.upload.dir` in `application.properties`).
- Downloads stream through **`DecryptedFileStream`**, which decrypts on the fly and pipes bytes to the response — files are never written to disk in plaintext.
- Every download is recorded via **`AuditService`** → `AccessLog`, giving an auditable trail of who accessed which result file and when.

Image uploads (team member photos, blog post images) go through `AdminUploadController` and use the same storage layer but are not encrypted, since they're public assets.

---

## Notifications

A small notification subsystem dispatches transactional messages (booking confirmations, "your result is ready", etc.) across email, SMS, and WhatsApp channels.

```
NotificationService
       │
       ▼
DefaultNotificationService ── routes message → matching sender
       │
       ├─ EmailSender   ← ConsoleEmailSender    (dev)
       ├─ SmsSender     ← ConsoleSmsSender      (dev)
       └─ WhatsappSender ← ConsoleWhatsappSender (dev)
```

Each channel is defined by an interface (`EmailSender`, `SmsSender`, `WhatsappSender`) with a message DTO (`EmailMessage`, `SmsMessage`, `WhatsappMessage`). Console implementations are wired in for development — they log the outbound message instead of calling a provider. Production senders (SMTP / Twilio / WhatsApp Cloud API or equivalent) can be dropped in as additional `@Component` implementations of the same interface and selected through configuration, with no caller-side changes.

An `SmtpEmailService` exists under `service/impl/` and is the intended production replacement for `ConsoleEmailSender`.

---

## Project Structure

```
src/main/java/com/djio/grover_hospital/
├── GroverHospitalApplication.java
├── config/                    # SecurityConfig, JwtConfig, CorsConfig, WebMvcConfig,
│                              # FileStorageConfig, AuditConfig, DataSeeder
├── controller/                # 28 controllers across public / portal / admin
├── exception/                 # Typed exceptions + GlobalExceptionHandler
├── model/
│   ├── dto/
│   │   ├── request/           # Inbound DTOs (validated)
│   │   └── response/          # Outbound DTOs + ApiResponse / PageResponse wrappers
│   ├── entity/                # 15 JPA entities
│   └── enums/                 # Role, BookingStatus, BookingType, ResultStatus, FeedbackSource
├── repository/                # Spring Data JPA repositories
├── security/                  # JwtAuthenticationFilter, JwtTokenProvider,
│                              # CustomUserDetailsService, UserPrincipal, SecurityUtils
├── service/                   # Domain services
│   ├── impl/                  # FileStorageService, SmtpEmailService
│   └── notification/          # Notification subsystem (channels + senders)
└── util/                      # DateUtils, SlugUtils

src/main/resources/
├── application.properties
└── db/migration/
    └── V1__create_users_and_auth.sql   # Flyway baseline
```

---

## Getting Started

### Prerequisites

- **JDK 17+**
- **Maven 3.9+** (or use the bundled `mvnw` wrapper)
- **PostgreSQL 14+** running locally or reachable over the network

### Clone & build

```bash
git clone https://github.com/David-Jesse/grovers-hospital-backend.git
cd grovers-hospital-backend
./mvnw clean install            # macOS / Linux
mvnw.cmd clean install          # Windows
```

### Set up the database

```sql
CREATE DATABASE grover_hospital;
CREATE USER grover WITH ENCRYPTED PASSWORD 'change_me';
GRANT ALL PRIVILEGES ON DATABASE grover_hospital TO grover;
```

Flyway will create the schema on first run.

### Run

```bash
./mvnw spring-boot:run
```

The API starts on `http://localhost:8080` by default. Swagger UI is at `http://localhost:8080/swagger-ui.html`.

---

## Configuration

Configuration lives in `src/main/resources/application.properties`. Override via environment variables, a profile-specific properties file, or command-line flags.

Key properties (names may differ slightly — see the file for the source of truth):

| Property                        | Purpose                                                     |
|---------------------------------|-------------------------------------------------------------|
| `spring.datasource.url`         | PostgreSQL JDBC URL                                         |
| `spring.datasource.username`    | Database user                                               |
| `spring.datasource.password`    | Database password                                           |
| `spring.jpa.hibernate.ddl-auto` | Should be `validate` in any shared environment (Flyway owns the schema) |
| `app.jwt.secret`                | HMAC signing key for JWTs — must be a strong, secret value  |
| `app.jwt.access-token-expiration`  | Access token lifetime (ms)                              |
| `app.jwt.refresh-token-expiration` | Refresh token lifetime (ms)                             |
| `app.upload.dir`                | Local directory for encrypted result files and image uploads |
| `app.encryption.secret`         | Symmetric key used by `EncryptionService`                   |
| `spring.servlet.multipart.max-file-size` | Per-file upload cap                                |
| `spring.servlet.multipart.max-request-size` | Per-request upload cap                          |

> **Never commit real secrets.** Use environment variables (`SPRING_DATASOURCE_PASSWORD`, `APP_JWT_SECRET`, `APP_ENCRYPTION_SECRET`, etc.) or a secret manager in any deployed environment.

### CORS

`CorsConfig` controls which origins may call the API. Update it to match your frontend's deployed URL before going live.

---

## Database Migrations

Schema changes are managed by **Flyway**. Migrations live in `src/main/resources/db/migration/` and are applied automatically on application startup.

Convention: `V<n>__<snake_case_description>.sql` (e.g. `V2__add_booking_indexes.sql`). Never edit a migration after it's been applied to a shared environment — add a new one instead.

The current baseline is `V1__create_users_and_auth.sql`, which sets up the patient/admin tables and supporting auth structures. Remaining tables are currently created from JPA entity metadata; future migrations should formalise them as the schema stabilises.

---

## Running Tests

```bash
./mvnw test
```

The current test suite contains the default Spring Boot context-load test (`GroverHospitalApplicationTests`). Test dependencies for `spring-boot-starter-data-jpa-test` and `spring-boot-starter-security-test` are already on the classpath, ready for the planned controller, repository, and security test layers.

---

## API Documentation

`springdoc-openapi` generates an OpenAPI 3 spec automatically from the controller layer.

- **Interactive UI:** `http://localhost:8080/swagger-ui.html`
- **Raw spec:** `http://localhost:8080/v3/api-docs`

---

## Roadmap

Work that's planned or in progress:

- **Test coverage** — repository slice tests (`@DataJpaTest`), controller tests (`@WebMvcTest`), and end-to-end security tests for the JWT filter and role-scoped endpoints.
- **Production notification senders** — replace `Console*` senders with real SMTP, SMS, and WhatsApp implementations behind the same interfaces.
- **Result-ready notifications** — wire `POST /admin/results/{id}/notify` through the production email/SMS path.
- **Flyway migrations for full schema** — move the remaining tables from JPA-managed DDL to versioned migrations.
- **Cleanup** — remove the unused `PackageRepository` and the misspelled `FeedbackRespository` (a duplicate of `FeedbackRepository`).
- **Refresh token storage & revocation** — server-side token tracking for logout / forced revocation.
- **Rate limiting** on `/auth/*` and public form-submission endpoints.

---

## License

Proprietary. All rights reserved. No license is granted for use, copying, modification, or distribution of this codebase without explicit written permission from the author.

---

*Maintainer:* [David-Jesse](https://github.com/David-Jesse)