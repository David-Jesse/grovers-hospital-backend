# Grover Hospital — Backend

A production REST API backend for a multi-tenant hospital operations platform, covering a public-facing website, a patient self-service portal, and an internal admin panel. Built with **Spring Boot 4**, **Java 17**, **PostgreSQL 17**, and **JWT-based** authentication. Deployed on **Google Cloud Run** with **Cloud SQL** and **Cloud Storage**.

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
9. [Department Scheduling](#department-scheduling)
10. [Booking Lifecycle](#booking-lifecycle)
11. [Project Structure](#project-structure)
12. [Getting Started](#getting-started)
13. [Configuration](#configuration)
14. [Database Migrations](#database-migrations)
15. [Deployment](#deployment)
16. [API Documentation](#api-documentation)

---

## Overview

Grover Hospital's backend is organized around three audiences, each served by a dedicated set of controllers:

- **Public** — anonymous endpoints powering the marketing site: departments, department schedules, team members, health packages, promotions, blog posts, testimonials, and public feedback submission.
- **Patient portal** — authenticated endpoints (`/portal/...`) where registered patients manage their profile, place and reschedule bookings, view test results, download encrypted result files, manage notification preferences, and submit feedback.
- **Admin panel** — privileged endpoints (`/admin/...`) where staff manage every resource: bookings (confirm with assigned time, update appointment time, reschedule), departments and their weekly schedules, consultant schedules, packages, results, team members, blog posts, testimonials, promotions, and feedback.

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
│  ─ Department schedule validation, booking enforcement   │
└──────────────────────────────────────────────────────────┘
                          │
┌──────────────────────────────────────────────────────────┐
│  Repositories (Spring Data JPA)                          │
│  ─ Persistence, query methods                            │
└──────────────────────────────────────────────────────────┘
                          │
┌──────────────────────────────────────────────────────────┐
│  PostgreSQL 17 (Cloud SQL) + Cloud Storage (file uploads)│
└──────────────────────────────────────────────────────────┘
```

Cross-cutting concerns sit in dedicated packages:

- `security/` — JWT filter, token provider, custom `UserDetailsService`, `SecurityUtils` for principal lookup.
- `config/` — `SecurityConfig`, `JwtConfig`, `WebMvcConfig`, `OpenApiConfig`, `DataSeeder` for development bootstrap.
- `exception/` — typed exceptions (`ResourceNotFoundException`, `BadRequestException`, `UnauthorizedException`, `FileStorageException`) and a `@RestControllerAdvice` `GlobalExceptionHandler` for consistent error responses.
- `notification/` — channel-agnostic notification dispatch with pluggable senders per channel.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.0.6 |
| Web | Spring Web MVC |
| Security | Spring Security + JJWT 0.12.x (HS512) |
| Persistence | Spring Data JPA, Hibernate (validate mode), PostgreSQL 17 |
| Schema management | Hand-run SQL migrations (Hibernate validate — schema must exist before startup) |
| API Docs | springdoc-openapi (Swagger UI, JWT bearer scheme) |
| Build | Maven |
| Boilerplate | Lombok |
| File storage | Google Cloud Storage (`gs://grovers-hospital-uploads`), volume-mounted at `/app/uploads` |
| Encryption | AES-256-GCM for result files at rest |
| Rate limiting | Bucket4j 8.19.0 |
| HTTP client | Spring `RestClient` |
| Notifications | SMTP (email), YourNotify REST API (SMS), Meta WhatsApp Cloud API (WhatsApp — outbound only, provider-switchable) |
| Deployment | Google Cloud Run (`grover-hospital`, `europe-west2`) |
| Database | Cloud SQL PostgreSQL (`grovers-db`, `europe-west2`) |
| Secrets | Google Secret Manager (4 secrets: `db-password`, `jwt-secret`, `encryption-master-key`, `smtp-password`) |

---

## Domain Model

| Entity | Table | Purpose |
|---|---|---|
| `Patient` | `patients` | End users of the patient portal. Authenticates with email + password (BCrypt 12). |
| `Admin` | `admins` | Internal staff. Single seeded admin; role-gated with `can('section')` helper for future RBAC. |
| `Department` | `departments` | 19 clinical departments. Slug-addressable. |
| `DepartmentSchedule` | `department_schedule` | Per-department weekly availability — one row per time range per day. Multiple ranges per day supported (e.g. split shifts). Used to validate and constrain patient bookings. |
| `TeamMember` | `team_members` | Doctors and consultants displayed on the public site. |
| `ConsultantSchedule` | `consultant_schedules` | Doctor-level availability (separate from department schedules). |
| `HealthPackage` | `health_packages` | Bundled service offerings. Slug-addressable. |
| `PackageTier` | `package_tiers` | Pricing/feature tiers within a `HealthPackage`. |
| `Booking` | `bookings` | Patient appointment or package booking. Drives the full booking lifecycle including admin-assigned `appointmentTime`. |
| `Result` | `results` | Diagnostic results owned by a patient; status-tracked. |
| `ResultFile` | `result_files` | AES-256-GCM encrypted attachments (PDFs, scans) linked to a `Result`. |
| `MedicalDocument` | `medical_documents` | AES-256-GCM encrypted patient-uploaded medical documents. |
| `BlogPost` | `blog_posts` | CMS articles drafted by admins, published via toggle. |
| `Testimonial` | `testimonials` | Patient testimonials, admin-approved before going public. |
| `Promotion` | `promotions` | Time-bound marketing campaigns; admin-toggleable. |
| `Feedback` | `feedback` | Submissions from public visitors or authenticated patients. |
| `Notification` | `notifications` | In-portal bell notifications for patients and admins. |
| `NotificationPreference` | `notification_preferences` | Per-patient opt-out settings per event + channel. |
| `NotificationDeliveryLog` | `notification_delivery_logs` | Delivery audit trail per channel send (success/failure + provider message ID). |
| `AccessLog` | `access_logs` | Audit trail of sensitive admin operations. |

### Enums

`Role`, `BookingStatus`, `BookingType`, `ResultStatus`, `FeedbackSource`, `DeliveryChannel`, `PortalNotificationType`

---

## API Surface

All endpoints are JSON and sit under the `/api/v1` context path. Authenticated endpoints require `Authorization: Bearer <jwt>`.

### Authentication — `/auth`

| Method | Path | Description |
|---|---|---|
| POST | `/auth/register` | Register a new patient. |
| POST | `/auth/login` | Patient login → access + refresh tokens. |
| POST | `/auth/admin/login` | Admin login → access + refresh tokens. |
| POST | `/auth/refresh` | Rotate access token using refresh token. |
| POST | `/auth/forgot-password` | Request a password reset link (rate-limited). |
| POST | `/auth/reset-password` | Complete password reset with token. |

### Public — anonymous

| Resource | Endpoints |
|---|---|
| Departments | `GET /departments`, `GET /departments/{id}` |
| Department schedules | `GET /public/departments/{id}/schedule` |
| Team | `GET /team` |
| Consultant schedules | `GET /consultant-schedule`, `GET /consultant-schedule/department/{id}` |
| Packages | `GET /packages`, `GET /packages/{slug}` |
| Promotions | `GET /promotions` |
| Blog | `GET /public/blog`, `GET /public/blog/{id}` |
| Testimonials | `GET /testimonials` |
| Feedback | `POST /feedback` |
| Health check | `GET /health` |

### Patient Portal — `/portal/...`

| Resource | Endpoints |
|---|---|
| Profile | `GET /portal/profile`, `PUT /portal/profile`, `PUT /portal/profile/password` |
| Profile update requests | `POST /portal/profile/update-request`, `GET /portal/profile/update-request/pending` |
| Bookings | `POST /portal/bookings`, `GET /portal/bookings`, `GET /portal/bookings/{id}`, `PUT /portal/bookings/{id}/cancel`, `PUT /portal/bookings/{id}/reschedule` |
| Results | `GET /portal/results`, `GET /portal/results/{id}`, `GET /portal/results/{resultId}/files/{fileId}/download`, `POST /portal/results/{resultId}/files/{fileId}/request-download-link` |
| Medical documents | `GET /portal/documents`, `POST /portal/documents`, `GET /portal/documents/{id}/download`, `DELETE /portal/documents/{id}` |
| Health profile | `GET /portal/health-profile`, `PUT /portal/health-profile` |
| Medications | `GET /portal/medications`, `POST /portal/medications`, `PUT /portal/medications/{id}`, `DELETE /portal/medications/{id}` |
| Chronic conditions | `GET /portal/conditions`, `POST /portal/conditions`, `DELETE /portal/conditions/{id}` |
| Visits | `GET /portal/visits` |
| Feedback | `POST /portal/feedback` |
| Notifications | `GET /portal/notifications`, `PATCH /portal/notifications/{id}/read`, `PATCH /portal/notifications/read-all` |
| Notification preferences | `GET /portal/notification-preferences`, `PUT /portal/notification-preferences` |
| Account | `GET /portal/account/export-data/{token}`, `DELETE /portal/account` |

### Admin Panel — `/admin/...`

| Resource | Endpoints |
|---|---|
| Bookings | `GET /admin/bookings`, `GET /admin/bookings/{id}`, `PUT /admin/bookings/{id}/confirm` (assigns time), `PUT /admin/bookings/{id}/appointment-time` (update confirmed time), `PUT /admin/bookings/{id}/status`, `PUT /admin/bookings/{id}/notes` |
| Departments | Full CRUD under `/admin/departments` |
| Department schedules | `GET /admin/departments/{id}/schedule`, `PUT /admin/departments/{id}/schedule` (bulk replace) |
| Team | Full CRUD under `/admin/team` |
| Consultant schedules | Full CRUD under `/admin/consultant-schedule` |
| Packages | Full CRUD under `/admin/packages`, tier management at `/admin/packages/{id}/tiers` |
| Promotions | Full CRUD + `PATCH /admin/promotions/{id}/toggle` |
| Blog | Full CRUD + `PUT /admin/blog/{id}/publish` |
| Testimonials | Full CRUD + `PATCH /admin/testimonials/{id}/approve` |
| Results | `POST /admin/results` (multipart), list, detail, `POST /admin/results/{id}/notify`, file download, delete |
| Feedback | List, detail, stats, mark-read, respond, delete |
| Profile update requests | `GET /admin/profile-update-requests`, `PUT /admin/profile-update-requests/{id}/approve`, `PUT /admin/profile-update-requests/{id}/reject` |
| Image upload | `POST /admin/upload/image` (multipart) |

---

## Authentication & Authorization

- **Patients** register at `/auth/register`, authenticate at `/auth/login`.
- **Admins** are seeded via `DataSeeder` and authenticate at `/auth/admin/login`.
- Both flows return a short-lived **access token** (HS512) and a **refresh token**; `/auth/refresh` rotates without re-authentication.
- `JwtAuthenticationFilter` runs once per request, validates the bearer token, and resolves the principal via `CustomUserDetailsService.loadUserByIdAndRole(userId, role)` — routing to `PatientRepository` or `AdminRepository` based on the `Role` claim in the token.
- `SecurityConfig` locks `/admin/**` to `ROLE_ADMIN` and `/portal/**` to `ROLE_PATIENT`, with `/public/**`, `/auth/**`, and a handful of specific GET paths open to anonymous access.
- Patient-owned resources are additionally ownership-checked at the service layer.
- Forgot-password requests are rate-limited via Bucket4j.

---

## File Storage & Encryption

Result files and medical documents are treated as PHI and encrypted at rest:

- **`EncryptionService`** encrypts on upload and decrypts on download using AES-256-GCM, with a master key from Secret Manager.
- **`FileStorageService`** writes ciphertext to the Cloud Storage bucket, mounted at `/app/uploads` on Cloud Run.
- Downloads stream through a decrypting wrapper — files are never written to disk in plaintext.
- Every download is recorded via **`AuditService`** → `AccessLog`.
- **Signed download links** (for patient-requested result links) use a short-lived JWT that encodes the file reference; the `/portal/account/export-data/{token}` endpoint is public but token-gated.

Public image uploads (team photos, blog images) use the same storage layer without encryption.

---

## Notifications

A multi-channel notification subsystem dispatches transactional messages across email, SMS, WhatsApp, and in-portal bell notifications.

```
NotificationService (interface)
       │
       ▼
DefaultNotificationService
       │
       ├─ EmailSender      ← SendchampEmailSender  (production)
       ├─ SmsSender        ← YourNotifySmsSender    (production, YourNotify REST API)
       └─ WhatsappSender   ← MetaWhatsappSender     (production, Meta Cloud API)
                             ConsoleWhatsappSender  (default/dev, provider=console)
```

- Each channel is interface-defined and property-switched: `app.notification.whatsapp.provider=console|meta`.
- Every patient-bound send respects a two-layer opt-out: app-level toggle in `application.properties` + per-patient `NotificationPreference` row.
- All sends are wrapped in `sendSafely` / `sendAndLog` — a single channel failure never blocks other channels or the calling transaction.
- Every send is recorded in `notification_delivery_logs` with provider message ID, status, and channel.
- **WhatsApp outbound** uses Meta's Cloud API (Graph API `/{phone-number-id}/messages`). Template messages are used for business-initiated notifications; free-form text is available within the 24-hour customer service window. Credentials (`WHATSAPP_ACCESS_TOKEN`, `WHATSAPP_PHONE_NUMBER_ID`) are stored in Secret Manager and injected via Cloud Run env vars.

### Notification events

| Event | Channels |
|---|---|
| Booking received (patient acknowledgment) | Email, SMS, WhatsApp, Portal bell |
| New booking alert (hospital staff) | Email, SMS, Portal bell (all admins) |
| Booking confirmed with assigned time | Email, SMS, WhatsApp, Portal bell |
| Appointment time changed | Email, SMS, WhatsApp, Portal bell |
| Appointment reminder (24h before) | Email, SMS, WhatsApp, Portal bell |
| Result ready | Email, SMS, Portal bell |
| Result download link | Email only |
| Feedback received (patient acknowledgment) | Email, Portal bell |
| Feedback response sent | Email, Portal bell |
| Password reset link | Email only |

---

## Department Scheduling

Each department has a weekly schedule of available time windows stored in `department_schedule`. Multiple ranges per day are supported (e.g. a split shift).

**Booking validation is split by responsibility:**

- At **booking create** — only the day is validated (department must be open at all on that day-of-week). No time required at this stage.
- At **admin confirm** — the assigned `appointmentTime` is validated against the department's windows for that specific day (start inclusive, end exclusive).

The admin bulk-replace endpoint (`PUT /admin/departments/{id}/schedule`) validates no overlapping ranges per day before replacing. The public endpoint (`GET /public/departments/{id}/schedule`) exposes availability for the frontend booking picker to grey out unavailable days and constrain the time picker shown to admins on confirmation.

---

## Booking Lifecycle

```
Patient submits booking (preferredDate only)
       │
       ▼  status=PENDING, appointmentTime=null
Admin reviews in dashboard
       │
       ▼  PUT /admin/bookings/{id}/confirm { appointmentTime, adminNotes }
         ─ validates time against department schedule
         ─ status=CONFIRMED, appointmentTime set
         ─ fires booking-confirmed notification with time
       │
       ├─ Admin updates time later → PUT /admin/bookings/{id}/appointment-time
       │   ─ fires appointment-time-changed notification
       │
       └─ Patient reschedules → appointmentTime cleared, status=PENDING
           ─ admin re-confirms with new time
```

Package bookings (`BookingType.PACKAGE`) bypass department schedule validation — no department is attached.

---

## Project Structure

```
src/main/java/com/djio/grover_hospital/
├── GroverHospitalApplication.java
├── config/                    # SecurityConfig, OpenApiConfig, WebMvcConfig, DataSeeder
├── controller/                # ~30 controllers across public / portal / admin
├── exception/                 # Typed exceptions + GlobalExceptionHandler
├── model/
│   ├── dto/
│   │   ├── request/           # Inbound DTOs (Bean Validation)
│   │   └── response/          # Outbound DTOs, ApiResponse<T>, PageResponse<T>
│   ├── entity/                # JPA entities
│   └── enums/
├── notification/
│   ├── channel/               # EmailMessage, SmsMessage, WhatsappMessage
│   ├── core/                  # SendResult
│   ├── sender/                # EmailSender, SmsSender, WhatsappSender interfaces + impls
│   └── whatsapp/              # WhatsAppConfig, WhatsAppCloudClient, PhoneNumberFormatter
├── repository/                # Spring Data JPA repositories
├── security/                  # JwtAuthenticationFilter, JwtTokenProvider,
│                              # CustomUserDetailsService, UserPrincipal, SecurityUtils
└── service/                   # Domain services + NotificationDeliveryLogService,
                               # PortalNotificationService, NotificationPreferenceService,
                               # DepartmentScheduleService, AuditService, EncryptionService,
                               # FileStorageService, BucketRateLimiterService

src/main/resources/
└── application.properties
```

---

## Getting Started

### Prerequisites

- JDK 17+
- Maven 3.9+ (or bundled `mvnw`)
- PostgreSQL 17 running locally

### Clone & build

```bash
git clone https://github.com/grovershospital/grovers-backend.git
cd grovers-backend
mvnw.cmd clean install -DskipTests   # Windows
```

### Set up the database

```sql
CREATE DATABASE grover_hospital;
```

Hibernate is on `validate` mode — **the schema must exist before the app starts.** Run the hand-written migration SQL files in `sql/` against the database first. There is no auto-DDL.

### Run

```bash
mvnw.cmd spring-boot:run
```

The API starts on `http://localhost:8080/api/v1`. Swagger UI is at `http://localhost:8080/api/v1/swagger-ui.html`.

---

## Configuration

All configuration lives in `application.properties`. In production, sensitive values are injected as environment variables backed by Google Secret Manager.

| Property | Purpose |
|---|---|
| `spring.datasource.url` | PostgreSQL JDBC URL |
| `spring.datasource.username` / `password` | Database credentials |
| `spring.jpa.hibernate.ddl-auto=validate` | Hibernate validates schema only — never modifies it |
| `app.jwt.secret` | HS512 signing key |
| `app.jwt.access-token-expiration` | Access token lifetime (ms) |
| `app.jwt.refresh-token-expiration` | Refresh token lifetime (ms) |
| `app.upload.dir` | File storage root (`/app/uploads` on Cloud Run, Cloud Storage mounted) |
| `app.encryption.master-key` | AES-256-GCM master key for file encryption |
| `app.cors.allowed-origins` | Comma-separated frontend origins |
| `app.hospital.name` | Used in notification templates |
| `app.hospital.notification-email` | Hospital inbox for admin alerts |
| `app.hospital.contact-phone` | Shown in patient-facing notifications |
| `app.hospital.contact-email` | Shown in patient-facing notifications |
| `app.frontend.base-url` | Frontend root URL (used in reset links, portal links) |
| `app.notification.whatsapp.provider` | `console` (dev) or `meta` (production) |
| `app.whatsapp.access-token` | Meta Cloud API access token |
| `app.whatsapp.phone-number-id` | Meta phone number ID |
| `app.whatsapp.waba-id` | WhatsApp Business Account ID |
| `app.notification.events.*` | Per-event per-channel toggle flags |

> **Never commit real secrets.** Use environment variables for all credentials in any deployed environment.

---

## Database Migrations

Hibernate is set to `validate` — it checks that the schema matches the entities on startup and refuses to start if anything is missing. This means:

- Schema changes **must be applied manually** via psql before deploying a new JAR.
- Migration SQL files live in `sql/` and are numbered (`V18__department_schedule.sql`, `V19__booking_appointment_time.sql`, etc.).
- The Cloud SQL Auth Proxy is used to connect to production Cloud SQL from a local machine.

**Never deploy a new JAR without running the corresponding SQL migration first.**

---

## Deployment

The backend is deployed to **Google Cloud Run** as a container built from source by Cloud Build.

### Deploy command (PowerShell — single line)

```powershell
gcloud run deploy grover-hospital --source . --region europe-west2 --project scrapper-464819
```

### Infrastructure

| Resource | Details |
|---|---|
| Cloud Run service | `grover-hospital`, region `europe-west2` |
| Cloud SQL | `grovers-db`, PostgreSQL 17, region `europe-west2` |
| Cloud Storage | `gs://grovers-hospital-uploads` (volume-mounted at `/app/uploads`) |
| Artifact Registry | `europe-west2-docker.pkg.dev/scrapper-464819/grovers-images/grover-hospital` |
| Secret Manager | `db-password`, `jwt-secret`, `encryption-master-key`, `sendchamp-access-key`, `smtp-password` (rollback), `whatsapp-access-token`, `whatsapp-phone-number-id`, `whatsapp-waba-id` |

### Deployment checklist

1. Run any new SQL migration files against Cloud SQL via the Auth Proxy
2. Confirm `mvn clean package` returns `BUILD SUCCESS` locally
3. Run the deploy command above
4. Verify new endpoints appear in Swagger UI on the live URL
5. Smoke-test critical paths in Postman

For the SendChamp email rollout, secret creation, provider settings, smoke tests, and rollback are documented in [`docs/SENDCHAMP_PRODUCTION.md`](docs/SENDCHAMP_PRODUCTION.md).

---

## API Documentation

springdoc-openapi generates an OpenAPI 3 spec automatically from the controller layer. A JWT bearer security scheme is configured via `OpenApiConfig`.

- **Interactive UI:** `https://<service-url>/api/v1/swagger-ui.html`
- **Raw spec:** `https://<service-url>/api/v1/api-docs`

---

*Maintainer:* [David-Jesse](https://github.com/David-Jesse)  
*Client:* Grover's Hospital and Lifestyle Clinic, 81A Younis Bashorun Street, Victoria Island, Lagos
