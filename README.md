# IDMS - Identity Management System

A multi-tenant identity management system built with Spring Boot and React, featuring OAuth2 authorization server capabilities, PostgreSQL schema-based multitenancy, and comprehensive user management.

## 🌟 Overview

IDMS (Identity Management System) provides a complete OAuth2/OpenID Connect authorization server implementation with multi-tenant support, enabling secure identity management across isolated tenant environments.

## 📊 Test Coverage

The backend codebase maintains comprehensive test coverage:

| Metric       | Coverage  | Covered | Missed | Total |
| ------------ | --------- | ------- | ------ | ----- |
| **Lines**    | **87.1%** | 203     | 30     | 233   |
| **Classes**  | **84.4%** | 27      | 5      | 32    |
| **Methods**  | **89.7%** | 87      | 10     | 97    |
| **Branches** | **31.8%** | 7       | 15     | 22    |
| Instructions | 77.4%     | 893     | 261    | 1,154 |

_Coverage reports are generated using JaCoCo and can be found in `idms-web/target/site/jacoco/`._

## ✨ Key Features

### 🔐 Authentication & Authorization

- **OAuth2 Authorization Server** with Authorization Code, Client Credentials, and Refresh Token flows
- **Social Login Integration** (Google, GitHub)
- **Session Management** with Redis-backed session storage
- **JWT Token Support** for stateless API authentication

### 🏢 Multi-Tenancy

- **Schema-Based Tenant Isolation** using PostgreSQL schemas
- **Automatic Tenant Resolution** from HTTP headers
- **Thread-Local Context Management** for tenant-aware operations
- **Connection Pool Optimization** with minimal overhead
- **Tenant Management API** for CRUD operations

### 🔧 Technical Capabilities

- **Database Migrations** with Flyway
- **RESTful API** with comprehensive endpoints
- **Health Checks & Metrics** via Spring Boot Actuator
- **Distributed Tracing** with OpenTelemetry (OTLP)
- **Virtual Threads** support (Java 21)
- **Observability** with Prometheus metrics

### 🎨 Frontend

- **React 19** with TypeScript
- **Vite** for blazing-fast development
- **Chakra UI** for accessible component library
- **TailwindCSS** for utility-first styling
- **React Router 7** for navigation

## 📁 Project Structure

This is a monorepo containing multiple modules:

```
idms/
├── idms-web/                 # Spring Boot backend application
│   ├── src/main/java/        # Java source code
│   │   └── com/turtleby/idms/web/
│   │       ├── security/     # Security configurations
│   │       ├── tenant/       # Tenant management
│   │       └── user/         # User management logic
│   └── src/main/resources/
│       ├── application.yml   # Application configuration
│       └── db/migration/     # Flyway database migrations
│
├── idms-multitenancy/        # Multitenancy library
│   └── src/main/java/        # Schema-based multitenancy implementation
│
├── idms-ui/                  # React frontend application
│   ├── src/
│   │   ├── routes/          # Route components
│   │   ├── App.tsx          # Main application component
│   │   └── router.tsx       # Route configuration
│   └── public/              # Static assets
│
└── shared-dependencies/      # Shared Maven dependencies
```

## 🚀 Getting Started

### Prerequisites

- **Java 21** or higher
- **Node.js 22.12** or higher
- **Yarn 4.10.3** (or use the included package manager)
- **PostgreSQL 14** or higher
- **Redis 7** or higher
- **Maven 3.9+** (or use the included Maven wrapper)

### Environment Setup

1. **Clone the repository**

   ```bash
   git clone https://github.com/laxsuryavanshi/idms.git
   cd idms
   ```

2. **Configure environment variables**

   Create a `.env` file in the root directory (use `.env.example` as a template):

   ```bash
   # Database Configuration
   DATABASE_URL=jdbc:postgresql://localhost:5432/idms
   DATABASE_USERNAME=postgres
   DATABASE_PASSWORD=your_password

   # Redis Configuration
   REDIS_URL=redis://localhost:6379

   # OAuth2 Social Login (Optional)
   GITHUB_OAUTH2_CLIENT_ID=your_github_client_id
   GITHUB_OAUTH2_CLIENT_SECRET=your_github_client_secret
   GOOGLE_OAUTH2_CLIENT_ID=your_google_client_id
   GOOGLE_OAUTH2_CLIENT_SECRET=your_google_client_secret

   # OpenTelemetry (Optional)
   OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318
   OTEL_EXPORTER_OTLP_LOGS_ENDPOINT=http://localhost:4318/v1/logs
   OTEL_EXPORTER_OTLP_METRICS_ENDPOINT=http://localhost:4318/v1/metrics
   OTEL_EXPORTER_OTLP_TRACES_ENDPOINT=http://localhost:4318/v1/traces
   ```

3. **Set up PostgreSQL database**

   ```bash
   createdb idms
   ```

4. **Start Redis** (if not already running)
   ```bash
   redis-server
   ```

### Building the Project

#### Backend (Java/Spring Boot)

```bash
# Build all modules
./mvnw clean install

# Run tests
./mvnw test

# Apply code formatting
./mvnw spotless:apply
```

#### Frontend (React/TypeScript)

```bash
# Install dependencies
yarn install

# Run development server
yarn dev

# Build for production
yarn build
```

### Running the Application

#### Start the Backend

```bash
cd idms-web
../mvnw spring-boot:run
```

Or with environment variables:

```bash
DATABASE_PASSWORD=postgres \
DATABASE_USERNAME=postgres \
DATABASE_URL=jdbc:postgresql://localhost:5432/idms \
REDIS_URL=redis://localhost:6379 \
./mvnw spring-boot:run -pl idms-web
```

The backend will start on `http://localhost:8080`

#### Start the Frontend

```bash
# From the root directory
yarn dev
```

The frontend will start on `http://localhost:5173`

## 🔑 Core Concepts

### Multi-Tenancy Architecture

IDMS uses PostgreSQL schema-based multitenancy for data isolation:

- Each tenant gets a dedicated PostgreSQL schema
- Tenant context is automatically resolved from HTTP headers (`X-Tenant-ID`)
- All database queries are scoped to the current tenant's schema
- Zero application-level filtering required

**Example:**

```bash
# Query from tenant-a schema
curl -H "X-Tenant-ID: tenant-a" http://localhost:8080/api/users

# Query from tenant-b schema
curl -H "X-Tenant-ID: tenant-b" http://localhost:8080/api/users
```

### OAuth2 Authorization Server

IDMS implements a full-featured OAuth2 2.1 and OpenID Connect 1.0 compliant authorization server:

- **Authorization Code Flow** with PKCE
- **Client Credentials Flow** for machine-to-machine
- **Refresh Token Flow** for token renewal
- **JWT Access Tokens** with configurable claims
- **Client Management API** for dynamic client registration

### Security Architecture

The application uses a multi-layered security approach:

1. **Authorization Server Security** (`/oauth2/**`, `/.well-known/**`) - OAuth2 endpoints
2. **API Security** (`/api/**`) - JWT-based stateless authentication
3. **Default Security** - Session-based authentication for web UI

## 🛠️ Development

### Code Quality Tools

The project uses several tools to maintain code quality:

- **Checkstyle** - Java code style enforcement
- **Spotless** - Code formatting (Java & Frontend)
- **Maven Enforcer** - Dependency convergence
- **JaCoCo** - Code coverage reporting
- **ESLint** - JavaScript/TypeScript linting
- **Prettier** - Frontend code formatting

```bash
# Check code style
./mvnw checkstyle:check

# Format code
./mvnw spotless:apply

# Run tests with coverage
./mvnw verify
```

### Testing

```bash
# Run all tests
./mvnw test

# Run integration tests
./mvnw verify

# Run specific test
./mvnw test -Dtest=ApplicationTests
```

The project uses:

- **JUnit 5** for unit testing
- **Spring Boot Test** for integration testing
- **Testcontainers** for database integration tests

## 📊 Observability

### Metrics & Monitoring

IDMS exposes metrics in multiple formats:

- **Prometheus** metrics at `/actuator/prometheus`
- **OTLP** (OpenTelemetry Protocol) for traces, metrics, and logs
- **Micrometer** for application metrics

### Distributed Tracing

OpenTelemetry integration provides:

- Request tracing across services
- Database query tracking
- Custom span creation for business operations

Configure OTLP endpoints:

```bash
OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318
OTEL_EXPORTER_OTLP_TRACES_ENDPOINT=http://localhost:4318/v1/traces
```

## 🏗️ Architecture Highlights

### Technology Stack

**Backend:**

- Spring Boot 3.4+
- Spring Security 6.4+
- Spring Data JDBC
- PostgreSQL 14+
- Redis 7+
- Flyway
- Micrometer + OpenTelemetry

**Frontend:**

- React 19
- TypeScript 5.9+
- Vite 7
- Chakra UI 3.30+
- TailwindCSS 4
- React Router 7

**Build Tools:**

- Maven 3.9+
- Yarn 4.10+

### Design Patterns

- **Repository Pattern** for data access
- **Service Layer** for business logic
- **DTO Pattern** for API contracts
- **Factory Pattern** for object creation
- **Strategy Pattern** for tenant resolution
- **Interceptor Pattern** for cross-cutting concerns

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Run tests and code formatting (`./mvnw verify spotless:apply`)
5. Commit your changes (`git commit -m 'Add amazing feature'`)
6. Push to the branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

### Code Style

- Java: Follow Google Java Style Guide (enforced by Checkstyle)
- TypeScript/React: Use Prettier formatting
- Commit messages: Use conventional commit format

## 👤 Author

**Laxmikant Suryavanshi**

- GitHub: [@laxsuryavanshi](https://github.com/laxsuryavanshi)

## 🙏 Acknowledgments

- Spring Security Team for OAuth2 implementation guidance
- PostgreSQL Community for schema-based multitenancy patterns
- React and Vite communities for modern frontend tooling

## 📞 Support

For issues, questions, or contributions:

- Create an issue on GitHub
- Refer to module-specific READMEs for detailed documentation

---

**Built with ❤️ using Spring Boot and React**
