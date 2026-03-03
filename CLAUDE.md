# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

IDMS (Identity Management System) is a multi-tenant OAuth2/OpenID Connect authorization server built with Spring Boot 3.x and Java 21. It uses PostgreSQL schema-based multitenancy, where each tenant gets an isolated schema.

## Build & Development Commands

```bash
# Build all modules
./mvnw clean package

# Run the application (from repo root)
cd idms-web && ../mvnw spring-boot:run

# Run all tests
./mvnw test

# Run integration tests with coverage
./mvnw verify

# Run a single test class
./mvnw test -pl idms-web -Dtest=TenantServiceITests

# Run a single test method
./mvnw test -pl idms-web -Dtest=TenantServiceITests#methodName

# Format code (must pass before committing)
./mvnw spotless:apply

# Check code style (Google Java Style)
./mvnw checkstyle:check
```

## Environment Setup

Copy `.env.example` to `.env` and populate required variables:

| Variable                         | Required | Purpose                 |
| -------------------------------- | -------- | ----------------------- |
| `DATABASE_URL`                   | Yes      | PostgreSQL JDBC URL     |
| `DATABASE_USERNAME`              | Yes      | DB user                 |
| `DATABASE_PASSWORD`              | Yes      | DB password             |
| `REDIS_URL`                      | Yes      | Redis connection URL    |
| `GITHUB_OAUTH2_CLIENT_ID/SECRET` | No       | Social login            |
| `GOOGLE_OAUTH2_CLIENT_ID/SECRET` | No       | Social login            |
| `OTEL_EXPORTER_OTLP_*`           | No       | OpenTelemetry endpoints |

Integration tests use **Testcontainers** — no manual DB setup is needed for tests, only Docker.

## Module Architecture

Three Maven modules with `shared-dependencies` as the parent BOM:

```
shared-dependencies/    ← Parent POM: dependency management, plugin config
idms-multitenancy/      ← Reusable multitenancy library (auto-configured)
idms-web/               ← Spring Boot application
```

### `idms-multitenancy` library

Provides transparent schema-based multitenancy. Key components:

- `TenantContextHolder` — thread-local tenant storage
- `TenantResolver<T>` — strategy interface; default impl is `HttpHeaderTenantResolver` (reads `X-Tenant-ID` header)
- `SchemaAwareDataSource` — wraps the real DataSource; sets `search_path` on borrow from pool, caches the current schema per connection to skip redundant `SET` commands
- `TenantInterceptor` — Spring MVC interceptor that calls the resolver, populates context, clears it after the request

### `idms-web` package structure

```
security/
  configuration/   ← Three security filter chains: OAuth2 AS, JWT API, session-based web
  userdetails/     ← Custom UserDetailsManager backed by Spring Data JDBC
tenant/
  controller/      ← REST endpoints for tenant management
  service/         ← Business logic
  dao/             ← Spring Data JDBC repositories
  entity/          ← JDBC entities
  mapping/         ← MapStruct mappers (DTO ↔ entity)
  dto/             ← API contracts
user/
  dao/             ← EmailAddressRepository, PhoneNumberRepository
  entity/          ← UserAccount, UserAggregate models
common/
  exception/       ← Global exception handling
```

## Security Architecture

Three Spring Security filter chains are layered (ordered):

1. **OAuth2 Authorization Server** — handles `/oauth2/**` and `/.well-known/**`; issues JWT access tokens using keys from `src/main/resources/certs/`
2. **API (stateless JWT)** — secures `/api/**` with Bearer token authentication; no session
3. **Web (session-based)** — default chain for remaining routes; supports social login (GitHub, Google)

## Multitenancy Flow

1. Request arrives → `TenantInterceptor` calls `HttpHeaderTenantResolver` → reads `X-Tenant-ID` header
2. Tenant stored in `TenantContextHolder` (thread-local)
3. Any JDBC operation → `SchemaAwareDataSource` sets `SET search_path TO <tenant_schema>` on the connection
4. After request completes → interceptor clears `TenantContextHolder`

## Database Migrations

Flyway migrations live in `idms-web/src/main/resources/db/migration/`. All migrations are PostgreSQL-specific. Set `SPRING_FLYWAY_ENABLED=false` to disable in environments where migrations are managed externally.

## Code Quality Gates

The following must pass before merging (enforced in the verify phase):

- **Spotless** (googleJavaFormat 1.28.0) — run `./mvnw spotless:apply` to auto-fix
- **Checkstyle** (Google Java Style) — config in `checkstyle.xml`
- **SpotBugs** — static analysis runs during `verify`
- **JaCoCo** — coverage thresholds enforced; currently ~87% line coverage

## Key Technology Versions

- Java 21 (virtual threads enabled: `spring.threads.virtual.enabled=true`)
- Spring Boot 3.5.9 / Spring Security 6.4+
- Spring Authorization Server (OAuth2/OIDC)
- Spring Data JDBC (not JPA)
- Flyway for migrations
- MapStruct 1.6.3 for DTO mapping
- Testcontainers for integration tests
