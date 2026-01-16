# 4. Solution Strategy

## Fundamental Technology Decisions

### Core Architectural Pattern: Layered Architecture

The t9t framework follows a strict **multi-layered architecture** with clear separation of concerns:

```
┌──────────────────────────────┐
│     Presentation Layer       │  ← Web UI, REST Gateways
├──────────────────────────────┤
│     API Layer (*-api)        │  ← Request/Response DTOs, interfaces
├──────────────────────────────┤
│     Service Layer (*-sapi)   │  ← Business service interfaces
├──────────────────────────────┤
│     Business Layer (*-be)    │  ← Request handlers, business logic
├──────────────────────────────┤
│     Persistence Layer (*-jpa)│  ← JPA entities, repositories
├──────────────────────────────┤
│     Data Layer               │  ← Database, external systems
└──────────────────────────────┘
```

**Rationale**: This separation enables:
- Clear definition of dependencies
- Clear dependency management (dependencies flow downward only)
- Technology substitution within layers (e.g., different JPA providers)
- Module composition flexibility for different customer needs

### Domain-Driven Design (DDD)

**Module Organization by Business Domain**:
- Each functional domain has its own module set (auth, doc, email, io, etc.)
- Clear bounded contexts between domains
- Domain-specific DTOs and entities via Bonaparte DSL

**Benefits**:
- Business logic co-located with domain
- Reduced coupling between domains
- Team organization aligned with business capabilities

### Type-Safe Data Structures: Bonaparte DSL

**Decision**: Use custom DSL (Bonaparte) for all data structures instead of plain Java classes.

**Bonaparte Features**:
- `.bon` files define DTOs, requests, responses
- `.bddl` files define JPA entity mappings
- Code generation ensures type safety
- Allowable character subsets for strings, clearly defined minimum and maximum lengths
- More compact and readable notation compared to Java annotations (JSR 303)
- Use of symbolic names without any runtime class-overhead
- Built-in validation, serialization, versioning

**Example**:
```bon
class UserDTO {
    required userId                 userId;
    required Uppercase(2)           countryCode;
    required Unicode(50) userName;
    optional emailAddress email;
    required boolean isActive;
}
```

**Rationale**:
- Type-safe across JVM and external systems
- Consistent serialization (JSON, XML, compact binary)
- Security (not possible to transmit objects not intended to be passed)
- Automatic validation
- Evolution support for backward compatibility
- Clear contracts between layers

### Dependency Injection: Jdp Framework

**Decision**: Use jdp (jpaw dependency injection) instead of Spring or CDI.

**Characteristics**:
- Lightweight, compile-time safety
- Interface-based injection
- No reflection overhead
- Clear provider patterns
- Familiar due to identical annotation simple names for most features (@Singleton, @Dependent, @Specializes)

**Rationale**:
- Better performance than reflection-based DI
- Explicit dependencies
- Smaller footprint
- Framework control
- Stack traces not cluttered by CDI layers
- No restrictions related to using final keywords

## Quality Goal Achievement Strategies

### Modularity Strategy

**Mechanisms**:
1. **Module Separation**: 150+ Maven modules with clear responsibilities
2. **API-First Design**: All inter-module communication via API modules
3. **Plugin Architecture**: Service interfaces (SAPI) for extensibility
4. **Optional Modules**: Features can be included/excluded at build time

**Example**: A minimal deployment might include only:
- t9t-base-* (core)
- t9t-auth-* (authentication)
- t9t-server (runtime)

While a full deployment adds AI, document processing, search, etc.

### Scalability Strategy

**Horizontal Scaling**:
- **Stateless Application Servers**: No session state in application tier
- **Database Connection Pooling**: Efficient resource utilization
- **Shadow Database**: Read-only replicas for query distribution
- **Cluster Support**: Hazelcast-based clustering (t9t-orm-hibernate-cluster)
- **Kafka Integration**: Distributed message processing, provides sharing logic via partitions

**Vertical Scaling**:
- **Thread Pools**: Configurable worker and autonomous pools
- **Async Processing**: Non-blocking I/O via Vert.x
- **Caching**: Hibernate second-level cache, custom caching

**Database Scaling**:
- **Read/Write Separation**: Primary for writes, shadow for reads
- **Connection Pool Sizing**: Configurable per deployment
- **Query Optimization**: Shadow DB for reporting queries

### Extensibility Strategy

**Plugin Points**:
1. **Request Handlers**: Implement `IRequestHandler<T>` for new operations
2. **Service Extensions**: Override SAPI implementations via DI
3. **Custom Modules**: Add new *-api/*-be/*-jpa module sets
4. **Configuration**: XML and database-driven configuration

**No Core Modification Required**:
- Customers extend via new modules
- Override behavior via configuration
- Plugin interfaces for cross-cutting concerns

**Example Extension Points**:
- Custom authentication providers
- Additional document formats
- New data import/export formats
- Custom workflow steps
- Additional search backends

## Top-Level Decomposition

### Module Categorization

| Category | Purpose | Example Modules |
|----------|---------|-----------------|
| **Core** | Essential framework functionality | t9t-base-*, t9t-core-*, t9t-server |
| **Authentication** | User management, authorization | t9t-auth-* |
| **Document** | Document generation, templates | t9t-doc-* |
| **I/O** | Data import/export | t9t-io-* |
| **Communication** | Email, messaging | t9t-email-*, t9t-kafka-* |
| **Search** | Full-text search | t9t-solr-*, t9t-hibernate-search-* |
| **AI** | LLM integration, vector databases | t9t-ai-*, t9t-openai-*, t9t-ollama-* |
| **Integration** | External system connectors | t9t-adobe-* |
| **UI** | Web user interface | t9t-zkui-* |
| **Gateway** | API gateways | t9t-gateway-rest, t9t-gateway-mcp |
| **Testing** | Test infrastructure | t9t-tests-* |
| **Infrastructure** | Container, deployment | t9t-container, t9t-sql |

### Request Processing Flow

**Core Pattern**: All business operations are modeled as request/response pairs.

```
1. Client sends Request DTO
2. Gateway receives, authenticates
3. RequestProcessor dispatches to appropriate handler
4. Handler executes business logic
5. Handler returns Response DTO
6. Gateway serializes response to client
```

**Implementation**:
```java
public interface IRequestHandler<REQUEST extends RequestParameters> {
    ServiceResponse execute(REQUEST request);
}
```

**Benefits**:
- Uniform processing pipeline
- Cross-cutting concerns (auth, logging, metrics) in one place
- Easy testing (input/output pairs)
- Async processing support

## Technical Decisions for Quality Goals

### Performance

| Technique | Implementation | Module |
|-----------|----------------|--------|
| **Connection Pooling** | C3P0, Hikari | t9t-base-jpa-* |
| **Statement Caching** | JPA query cache | Via Hibernate/EclipseLink |
| **Second-Level Cache** | Hibernate cache | t9t-orm-hibernate-* |
| **Read Replica** | Shadow database | Configuration in t9tconfig.xml |
| **Async Processing** | Thread pools, Vert.x | t9t-base-be, t9t-*-vertx |
| **Efficient Serialization** | Bonaparte compact format | All modules |

### Security

| Concern | Solution | Implementation |
|---------|----------|----------------|
| **Authentication** | JWT tokens (HS256/384/512, RS256/384/512, ES256/384/512) | t9t-auth-be, t9t-jwt |
| **Authorization** | Role-based access control (RBAC) | t9t-auth-api, database |
| **Transport Security** | HTTPS (TLS) | Jetty configuration |
| **Credential Storage** | Environment variables, keystores | t9t-server, config |
| **SQL Injection** | Use only parameterized queries (JPA) | All *-jpa modules |
| **XSS Prevention** | Output encoding in ZK UI | t9t-zkui-* |
| **API Security** | API keys, JWT validation | Gateways |
| **Dependencies** | Use of GitHub dependabot, GitHub Advanced Security | GitHub |

### Maintainability

| Practice | Implementation |
|----------|----------------|
| **Code Quality** | Checkstyle for all file types |
| **Architecture Testing** | ArchUnit tests in t9t-tests-arch* |
| **Clear Module Boundaries** | Maven enforcer, dependency rules |
| **Consistent Patterns** | Request/response handlers |
| **Documentation** | Inline docs, markdown guides |
| **Version Control** | Git with branch strategy |

### Testability

| Level | Module | Approach |
|-------|--------|----------|
| **Unit** | t9t-*-be | Isolated component tests |
| **Integration (Embedded)** | t9t-tests-embedded | In-process DB, full stack |
| **Integration (Remote)** | t9t-tests-remote | Against deployed system |
| **Architecture** | t9t-tests-arch* | Structure and dependency rules using ArchUnit |
| **Manual** | - | UAT on deployed environment |

### Observability

| Aspect | Technology | Configuration |
|--------|-----------|---------------|
| **Logging** | Logback | logback.xml |
| **Structured Logging** | SLF4J with context | All modules |
| **Metrics** | JMX, custom metrics | t9t-metrics |
| **Monitoring** | JMX exporter | Container config |
| **Tracing** | Request correlation IDs | t9t-base-be |

## Integration Strategy

### Internal Integration (Between Modules)

**Mechanism**: Bonaparte-based RPC within same JVM
- Request/response DTOs defined in *-api modules
- Handlers in *-be modules
- Type-safe, no network overhead

### External Integration (External Systems)

**REST APIs**:
- JAX-RS for incoming REST requests
- HTTP clients for outgoing REST calls
- JSON serialization via Jackson

**Messaging**:
- Kafka for async, event-driven integration
- Topics per business event type
- Producer and consumer modules

**File-Based**:
- Configurable data sinks/sources (t9t-io-*)
- Multiple format support (CSV, Excel, XML, JSON)
- Scheduled or event-triggered processing

**Database Integration**:
- Direct JDBC where needed
- Stored procedures via JPA native queries
- Schema migrations for version management

## Deployment Strategy

### Container-First Approach

**Primary Deployment**: Docker containers
- Multi-stage Dockerfile in t9t-container
- Separate images: server, UI, gateway, setup
- docker-compose.yml for local development
- Kubernetes-ready (customer-configured)

**Configuration Management**:
- Environment variables for runtime config
- ConfigMaps/Secrets in Kubernetes
- XML base configuration
- Database for runtime overrides

### Database Migration Strategy

**Flyway Integration**:
- Versioned SQL scripts
- Automatic migration on startup
- Rollback support
- Located in t9t-sql modules

**Schema Loader**:
- Initial schema setup via schema-loader-*
- Bonaparte BDDL to DDL generation
- Support for multiple database brands

## Development Strategy

### Build Automation

**Maven Multi-Module**:
- Single root POM (pom.xml)
- Dependency management via BOMs
- Consistent versioning across modules
- Parallel builds supported

**CI/CD**:
- GitHub Actions workflows
- Automated build on commits
- Checkstyle validation
- Test execution
- Artifact publishing

### Code Generation

**Bonaparte DSL**:
- .bon → Java DTOs
- .bddl → JPA entities
- Xtext-based toolchain
- IDE integration (Eclipse)

**Benefits**:
- Reduced boilerplate
- Type safety
- Consistency
- Faster development

### Testing Strategy

**Multiple Test Scopes**:
1. Fast feedback: Unit tests during development
2. Integration: Embedded tests pre-commit
3. Full system: Remote tests in CI
4. Architecture: Continuous architecture validation

**No Mocking Philosophy**:
- Prefer embedded tests with real database
- Use test containers where possible
- Mock only external systems (SMTP, S3)

## Migration and Evolution Strategy

**API Versioning**:
- Major version in package names where needed
- Backward compatibility within major versions
- Deprecation warnings before removal

**Database Migrations**:
- Flyway scripts for schema changes
- No breaking changes to existing columns
- Additive changes preferred
- Data migration scripts when required

**Module Evolution**:
- New modules added without affecting existing
- Deprecated modules clearly marked
- Major release versions used for significant changes in dependencies (Jakarta EE 10 → 11, Java 17 → 21)

## Technology Choices Rationale

### Why Bonaparte Instead of Standard DTOs?

| Aspect | Bonaparte | Standard Java |
|--------|-----------|---------------|
| **Type Safety** | Compile-time via DSL | Runtime only |
| **Security** | Only objects defined in DSL can be transmitted | Passing of arbitrary objects |
| **Serialization** | Built-in, efficient | Manual or library |
| **Validation** | Automatic from DSL (generated) | Manual annotations, reflection-based |

### Why Jdp Instead of Spring?

| Aspect | Jdp | Spring |
|--------|-----|--------|
| **Performance** | Compile-time | Reflection-based |
| **Startup Time** | Fast | Slower |
| **Footprint** | Minimal | Larger |
| **Complexity** | Explicit | Magic/conventions |
| **Control** | Full | Framework-driven |

### Why ZK for UI?

| Aspect | Benefit |
|--------|---------|
| **Server-Side** | No JavaScript required for developers |
| **Component-Based** | Rich widget library |
| **AJAX Built-in** | Automatic client-server sync |
| **Java Integration** | Direct backend access |
| **Enterprise Features** | Grid, tree, charts out of box |
