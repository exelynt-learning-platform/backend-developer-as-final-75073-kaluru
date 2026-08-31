# Resource Booking System

Final Project Assignment — a secure REST API for booking rooms, vehicles, and equipment.

## Project Overview

Users can browse resources and create bookings; administrators manage all resources and reservations. The system is stateless, validates requests, prevents overlapping active bookings, and protects ownership at the backend.

## Features

- JWT authentication, BCrypt password hashing, and ADMIN/USER RBAC
- Resource CRUD, reservation creation/management, status workflow, and conflict detection
- Authenticated reservation ownership, filtering, pagination, sorting, validation, and structured errors
- MySQL persistence, Swagger/OpenAPI, and MockMvc integration tests

## Technology Stack

Java 17, Spring Boot 3, Spring Security, JWT, BCrypt, Spring Data JPA/Hibernate, MySQL 8+, Maven, Swagger/OpenAPI, JUnit 5, Mockito, and MockMvc. PostgreSQL is not used. Docker, Kubernetes, and Testcontainers are not required or used.

## Architecture

`Controller → Service → Repository → MySQL`. DTOs keep JPA entities and password hashes out of API payloads. The security filter validates bearer tokens before protected routes; services enforce business and ownership rules.

## Project Structure

```text
src/main/java/com/exelynt/booking
├── config  controller  dto  entity  exception
├── repository  security  service  specification
src/main/resources/application.yml
src/test/java/com/exelynt/booking
```

## Prerequisites and MySQL Setup

Install Java 17+, Maven, and MySQL 8+, ensure MySQL is running, then create the database:

```sql
CREATE DATABASE resource_booking;
```

## Environment Variables

```powershell
$env:DB_URL='jdbc:mysql://localhost:3306/resource_booking'
$env:DB_USERNAME='root'
$env:DB_PASSWORD='<your-local-mysql-password>'
$env:JWT_SECRET='<a-random-secret-at-least-32-characters-long>'
```

`JWT_SECRET` is required and should be a long random value. Never commit database passwords or secrets.

## Running and Testing

```powershell
mvn clean test
mvn clean package
mvn spring-boot:run
```

Swagger UI: `http://localhost:8080/swagger-ui/index.html`.

## Seed Users

Development/testing credentials only:

| Role | Username | Password |
|---|---|---|
| ADMIN | `admin` | `admin123` |
| USER | `user` | `user123` |

## Authentication

`POST /auth/login`

```json
{"username":"admin","password":"admin123"}
```

Use the returned JWT on protected requests: `Authorization: Bearer <JWT>`.

## API Endpoints and Permissions

| Method | Endpoint | Role | Description |
|---|---|---|---|
| POST | `/auth/login` | Public | Obtain JWT |
| GET | `/api/resources`, `/api/resources/{id}` | ADMIN, USER | Read resources |
| POST/PUT/DELETE | `/api/resources[/{id}]` | ADMIN | Resource CRUD |
| POST | `/api/reservations` | ADMIN, USER | Create booking |
| GET | `/api/reservations`, `/api/reservations/{id}` | ADMIN / own USER records | Read bookings |
| PUT/DELETE | `/api/reservations/{id}` | ADMIN | Manage bookings/status |

USER can read resources, create bookings, and read only their own bookings. ADMIN has full resource CRUD and reservation management.

## Ownership, Status, and Conflicts

Ownership is always `JWT → authenticated principal → user lookup → reservation.user`; the request body cannot select an owner. A malicious `userId` is ignored and can never impersonate another account.

Statuses are `PENDING`, `CONFIRMED`, and `CANCELLED`. PENDING/CONFIRMED bookings for the same resource may not overlap; CANCELLED bookings do not block availability. Prices use `BigDecimal` and are copied from the resource as a booking snapshot.

## Filtering, Pagination, and Sorting

`GET /api/reservations` supports `status`, `minPrice`, `maxPrice`, `page` (default 0), `size` (default 10, max 100), and `sort`.

```text
?status=CONFIRMED&minPrice=100&maxPrice=500&page=0&size=10&sort=price,desc
```

Supported sort fields: `price`, `startTime`, `endTime`, `createdAt`, `status`. USER queries are always scoped to the authenticated user's data.

## Error Handling

Errors return JSON with `timestamp`, `status`, `error`, `message`, and `path`. Main statuses: 400 validation/malformed input, 401 missing/invalid JWT, 403 forbidden action, 404 missing resource/reservation, 409 booking conflict, and 500 unexpected error.

## Swagger and Testing

Authorize Swagger using the bearer token from login. `mvn clean test` uses H2 only as a test-scoped isolated database; production always uses MySQL. Tests cover authentication, JWT/RBAC errors, ownership, validation, filters, pagination/sorting, and conflicts.

## Security and Design Decisions

Passwords are BCrypt hashes; no plaintext passwords, JWTs, database credentials, or JWT secrets are returned or committed. JWT security is stateless, CSRF is disabled for the REST API, and all ownership checks occur server-side. JPA Specification provides database-side filters; `Pageable` provides safe paging/sorting.
