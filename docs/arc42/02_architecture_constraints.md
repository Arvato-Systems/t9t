# 2. Architecture Constraints

## Technical Constraints

### Programming Language and Platform

| Constraint | Background/Motivation |
|------------|----------------------|
| **Java 21** | Required language version. Uses modern Java features including records, pattern matching. |
| **Maven 3.9+** | Build tool with multi-module support. Central dependency management via BOMs (Bill of Materials). |
| **Bonaparte DSL** | Custom domain-specific language for data structure definitions (`.bon` files) and database mappings (`.bddl` files). Generated code forms the core DTOs and entities. |
| **JPA 3.0+** | Primary persistence abstraction. Supports Hibernate and EclipseLink implementations. |

### Frameworks and Libraries

| Technology | Constraint | Rationale |
|------------|-----------|-----------|
| **Xtext 2.40.0** | Code generation framework for Bonaparte DSL | Core infrastructure for type-safe data structures |
| **Jetty 12.1.5** | Embedded web server for REST and web UIs | Lightweight, embeddable, production-ready |
| **ZK Framework** | UI framework for web applications | Component-based Java web framework |
| **JAX-RS (RESTEasy)** | REST API implementation | Standard Java REST API with wide support |
| **Hibernate/EclipseLink** | JPA implementations | Flexible ORM strategy |
| **Kafka** | Asynchronous messaging | Distributed event streaming platform |
| **Vert.x** | Reactive toolkit | High-performance reactive applications |

### Database Support

| Database | Status | Notes |
|----------|--------|-------|
| **PostgreSQL** | Primary | Recommended for production, full feature support |
| **AWS Aurora PostgreSQL** | Supported | Production-ready with specific connection wrapper plugins |
| **pgvector extension** | Optional | For vector database capabilities |
| **Other RDBMS** | Via JPA | H2 for testing, other databases possible but not primarily tested |

### Development Constraints

| Area | Constraint |
|------|-----------|
| **Code Quality** | Mandatory Checkstyle validation covering Java, SQL, Xtend, properties, XML, BON, BDDL files |
| **Testing** | Multi-level test strategy: unit, embedded, remote, architecture tests |
| **License Headers** | Apache 2.0 license headers required on all source files |
| **Build Environment** | Requires access to internal Arvato Systems Maven repositories or prerequisite local builds for proprietary BOMs (`jpaw`, `jdp`, `bonaparte`) |

## Organizational Constraints

### Development Process

| Aspect | Constraint |
|--------|-----------|
| **Version Control** | Git/GitHub with branch-based development |
| **Tasks** | Arvato Systems internal JIRA project, GitHub issues |
| **CI/CD** | GitHub Actions for build, test, and publishing |
| **Code Review** | Pull request-based workflow required |
| **Documentation** | In-repo markdown documentation required for features |

### Team Structure

| Area | Constraint |
|------|-----------|
| **Module Ownership** | Distributed ownership across functional domains |
| **Architecture Decisions** | Requires architecture team approval for cross-cutting changes |
| **Release Management** | Coordinated releases across all modules |

### Time and Budget

| Aspect | Constraint |
|--------|-----------|
| **Release Branches** | Multiple maintained versions (8.0 last Jakarta EE 10 based, 9.0+ Jakarta EE 11) |
| **Backward Compatibility** | API stability within major versions |
| **Migration Support** | Database migration scripts required for schema changes |

## Conventions

### Code Conventions

| Convention | Description |
|------------|-------------|
| **Module Naming** | `t9t-<domain>-<layer>` pattern (e.g., `t9t-auth-api`, `t9t-doc-be`) |
| **Package Structure** | `com.arvatosystems.t9t.<domain>.<sublayer>` |
| **Bonaparte Files** | `.bon` for DTOs and requests, `.bddl` for JPA entities |
| **Request Handlers** | Implement `IRequestHandler<RequestType>` interface |
| **Dependency Injection** | Jdp (jpaw dependency injection) framework |

### Layer Architecture

**Mandatory layer separation:**

```
*-api      → API definitions (Bonaparte DTOs, Request/Response objects) for internal APIs
*-apiext   → additional API definitions (public APIs for downstream repos)
*-sapi     → Service API (interfaces for business logic)
*-be       → Business logic implementation
*-jpa      → JPA entities and repositories
*-restapi  → JAX-RS (JAKARTA-RS) endpoints, using data structures defined in apiext modules
```

**Dependency rules:**
- API modules have minimal dependencies, these are used in API gateways, UI servers and the backend
- SAPI depends on API, used in the backend only
- BE depends on SAPI and API, used in the backend only
- JPA depends on SAPI and API, used in the backend only
- Cross-module dependencies only at API or SAPI level

### Testing Conventions

| Test Type | Location | Purpose |
|-----------|----------|---------|
| **Unit Tests** | `t9t-tests-unit` | Isolated component testing |
| **Embedded Tests** | `t9t-tests-embedded` | Integration tests with in-process database |
| **Remote Tests** | `t9t-tests-remote` | Full system integration tests (blackbox tests) |
| **Architecture Tests** | `t9t-tests-arch*` | Enforce architecture rules and conventions |

## Legal and Compliance Constraints

| Constraint | Description |
|------------|-------------|
| **License** | Apache License 2.0 for the framework |
| **Third-party Licenses** | All dependencies must have compatible licenses |
| **Data Protection** | GDPR compliance capabilities required |
| **Export Control** | No export-controlled encryption beyond standard JVM capabilities |

## Deployment Constraints

### Infrastructure Requirements

| Component | Minimum Requirements |
|-----------|---------------------|
| **JVM** | OpenJDK 21 or compatible (Amazon Corretto 21 or Temurin 21 recommended) |
| **Memory** | Minimum 2GB heap for production deployments |
| **Database** | PostgreSQL 15+ |
| **Network** | HTTP/HTTPS connectivity for REST APIs |
| **Storage** | Persistent storage for file I/O operations |

### Container Support

| Platform | Status |
|----------|--------|
| **Docker** | Full support with multi-stage Dockerfile |
| **Docker Compose** | Reference configuration provided |
| **Kubernetes** | Deployable (customer-specific configurations) |

### Security Requirements

| Area | Requirement |
|------|-------------|
| **Authentication** | JWT-based with configurable keystore (HMAC, RSA, ECDSA) |
| **Transport Security** | HTTPS required for production |
| **Credential Storage** | Environment variables or secure vault systems |
| **Secrets Management** | No hardcoded credentials permitted |

## Tooling Constraints

| Tool | Purpose | Version |
|------|---------|---------|
| **Eclipse IDE** | Primary development environment | 2025-09 or later, package for DSL developers |
| **Git** | Version control | 2.x+ |
| **Docker** | Container development and testing | 20.x+ |
| **PostgreSQL Client** | Database administration | 15+ |
