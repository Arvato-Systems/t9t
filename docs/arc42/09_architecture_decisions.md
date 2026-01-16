# 9. Architecture Decisions

This chapter documents important architectural decisions made during the development of the t9t framework. Each decision is documented with its context, decision, and consequences.

## ADR-001: Use Bonaparte DSL for All Data Structures of Internal API

**Status**: Accepted

**Context**:
The framework requires type-safe data structures that can be:
- Serialized efficiently across network boundaries
- Validated automatically
- Evolved over time while maintaining compatibility
- Shared between different layers and systems

**Decision**:
Use Bonaparte DSL (custom domain-specific language) for defining all DTOs, requests, responses, and entities. Bonaparte files (`.bon` and `.bddl`) are processed by code generators to produce Java classes.

**Consequences**:
- **Positive**:
  - Strong type safety across all layers
  - Consistent serialization formats (compact binary, JSON, XML)
  - Built-in validation from schema
  - Clear contracts between modules
  - Version evolution support
  - Reduced boilerplate code
- **Negative**:
  - Learning curve for Bonaparte syntax
  - Additional code generation step in build
  - Dependency on Bonaparte framework
  - IDE support requires plugins


## ADR-002: Jdp Dependency Injection Instead of Spring or CDI

**Status**: Accepted

**Context**:
The framework needs dependency injection for:
- Decoupling implementation from interfaces
- Plugin architecture
- Testability
- Service lifecycle management

Traditional options include Spring Framework and CDI.

**Decision**:
Use jdp (jpaw dependency injection) - a lightweight, compile-time dependency injection framework.

**Consequences**:
- **Positive**:
  - Better startup performance (no reflection)
  - Compile-time safety
  - Smaller runtime footprint
  - Much simpler stack traces
  - Explicit dependencies
  - Full control over injection mechanism
- **Negative**:
  - Less ecosystem support than Spring
  - Fewer built-in features
  - Team familiarity (most Java developers know Spring)
  - Custom tooling required

## ADR-003: Multi-Layered Module Architecture

**Status**: Accepted

**Context**:
With 150+ modules, the framework needs clear organization principles to:
- Prevent circular dependencies
- Enable independent module development
- Support flexible module composition
- Facilitate testing

**Decision**:
Adopt a strict 4-layer pattern for each functional domain:
- `*-api`: DTOs and interfaces (minimal dependencies)
- `*-sapi`: Service interfaces (depends on api)
- `*-be`: Business logic implementation (depends on sapi, api)
- `*-jpa`: Persistence entities (depends on api)

**Consequences**:
- **Positive**:
  - Clear separation of concerns
  - Predictable dependency flow
  - Easy to understand structure
  - Modules can be included/excluded independently
  - Simplified testing (mock at layer boundaries)
- **Negative**:
  - More modules to manage
  - Initial learning curve for contributors
  - Some code duplication across layers
  - Build complexity with many modules

## ADR-004: PostgreSQL as Primary Database

**Status**: Accepted

**Context**:
The framework needs a robust, scalable relational database that supports:
- Complex transactions
- JSON data types
- Full-text search
- Mature tooling and ecosystem
- Cloud deployment options

**Decision**:
Standardize on PostgreSQL as the primary supported database. Other databases may work via JPA but are not officially supported.

**Consequences**:
- **Positive**:
  - Excellent performance and reliability
  - Rich feature set (JSONB, arrays, full-text search, availability of conditional indexes)
  - Strong ACID compliance
  - Great cloud support (AWS Aurora, Azure Database)
  - Open source and free
  - Large community and ecosystem
- **Negative**:
  - If PostgreSQL-specific features are used, this limits portability
  - Requires PostgreSQL expertise for optimization

## ADR-005: JWT for Stateless Authentication

**Status**: Accepted

**Context**:
The system needs to:
- Support stateless horizontal scaling
- Authenticate users across multiple services
- Minimize database lookups for authentication
- Support API clients and web browsers

**Decision**:
Use JWT (JSON Web Tokens) for authentication. Tokens contain user identity, tenant, roles, and permissions, and are signed with HMAC, RSA, or ECDSA.

**Consequences**:
- **Positive**:
  - Stateless authentication enables horizontal scaling
  - No session storage required
  - Self-contained tokens reduce database queries
  - Standard format with good library support
  - Can be used across different technologies
- **Negative**:
  - Cannot revoke tokens until expiration (unless blacklist used)
  - Token size can be large with many claims
  - Sensitive data in token must be encrypted
  - Key management complexity

## ADR-006: Request/Response Pattern for All Operations

**Status**: Accepted

**Context**:
The framework needs a uniform way to:
- Define business operations
- Implement cross-cutting concerns (auth, logging, metrics)
- Support both synchronous and asynchronous processing
- Enable easy testing and mocking

**Decision**:
Model all business operations as request/response pairs where:
- Requests extend `RequestParameters`
- Responses extend `ServiceResponse`
- Handlers implement `IRequestHandler<REQUEST>`
- All requests go through central dispatcher

**Consequences**:
- **Positive**:
  - Uniform processing pipeline
  - Easy to add cross-cutting concerns
  - Simple testing (input/output pairs)
  - Clear contracts for operations
  - Supports async processing naturally
  - Request/response logged automatically
  - Easy integration of scheduler tasks
- **Negative**:
  - More verbose than direct method calls
  - Extra classes for each operation
  - Performance overhead of dispatching
  - Less familiar pattern for some developers

## ADR-007: Dual Database Connection Pools

**Status**: Accepted

**Context**:
The system has two distinct types of database access:
- **Transactional writes**: Need consistency, ACID guarantees
- **Read-heavy queries**: Reports, searches, monitoring

**Decision**:
Implement two separate connection pools:
- **Primary Pool** (C3P0): For write operations and transactions
- **Shadow Pool** (Hikari): For read-only queries, connected to read replica

**Consequences**:
- **Positive**:
  - Write operations don't compete with read queries
  - Can scale reads independently with replicas
  - Optimized pool configurations per use case
  - Better resource utilization
  - Reduced contention on primary database
- **Negative**:
  - More complex configuration
  - Read operations may see slightly stale data (replication lag)
  - Application must choose correct pool
  - Double the connection overhead

(After all, use of the shadow database is optional!)

## ADR-008: Kafka for Asynchronous Messaging

**Status**: Accepted

**Context**:
The system needs asynchronous messaging for:
- Cluster coordination
- Event notifications
- Long-running process coordination
- Integration with external systems

**Decision**:
Use Apache Kafka as the primary message broker for asynchronous, event-driven communication.

**Consequences**:
- **Positive**:
  - High throughput and scalability
  - Durable message storage
  - Replay capability
  - Good ecosystem and tooling
  - Cloud-managed options (AWS MSK)
  - Exactly-once semantics support
- **Negative**:
  - Additional infrastructure component
  - Operational complexity
  - Learning curve for Kafka concepts
  - Overkill for simple use cases

Use of kafka is optional.

## ADR-009: Jetty as Embedded Web Server

**Status**: Accepted

**Context**:
The framework needs an embedded web server for:
- REST APIs
- Web UI
- Admin endpoints
- MCP gateway

**Decision**:
Use Jetty as the embedded web server, configured and started programmatically.

**Consequences**:
- **Positive**:
  - Lightweight and embeddable
  - Excellent performance
  - Mature and stable
  - Good HTTP/2 and WebSocket support
  - Java-based configuration
  - Single JAR deployment
- **Negative**:
  - Less familiar than Tomcat for some teams
  - Programmatic configuration more verbose than XML
  - Fewer out-of-box features than full app servers

## ADR-010: ZK Framework for Web UI

**Status**: Accepted

**Context**:
The system needs a web UI framework that:
- Allows Java developers to build UIs
- Provides rich component library
- Supports complex enterprise UIs
- Integrates well with backend

**Decision**:
Use ZK Framework for server-side, component-based web UI development.

**Consequences**:
- **Positive**:
  - No JavaScript knowledge required for developers
  - Rich component library (grids, trees, charts)
  - Automatic AJAX handling
  - Type-safe (Java-based)
  - Direct backend integration
  - Good for CRUD-heavy applications
- **Negative**:
  - Server-side rendering limits some UI patterns
  - Less flexibility than modern JS frameworks
  - Smaller community than React/Angular/Vue
  - Not suitable for public-facing websites
  - Licensing costs for Enterprise Edition features

## ADR-011: Multi-Tenancy as Core Architecture

**Status**: Accepted

**Context**:
The framework is designed for SaaS scenarios where:
- Multiple customers share the same infrastructure
- Complete data isolation is required
- Per-tenant configuration is needed
- Efficient resource utilization is important

**Decision**:
Build multi-tenancy into the core architecture:
- Every entity has a `tenantRef` field
- Tenant context in every request
- Automatic filtering of queries by tenant
- Per-tenant configuration support

**Consequences**:
- **Positive**:
  - Efficient resource utilization (shared infrastructure)
  - Simpler deployment than per-customer instances
  - Cost-effective SaaS model
  - Centralized management and updates
- **Negative**:
  - Cannot easily extract single tenant
  - Risk of data leakage if implemented incorrectly
  - Performance impact of additional filtering
  - Complexity in queries and testing
  - Schema changes affect all tenants

## ADR-012: Flyway for Database Migrations

**Status**: Accepted

**Context**:
The system needs a reliable way to:
- Version database schema
- Apply migrations automatically
- Support multiple environments
- Coordinate schema changes with code changes

**Decision**:
Use Flyway for versioned database migrations with SQL scripts.

**Consequences**:
- **Positive**:
  - Simple, SQL-based migrations
  - Version control for schema
  - Automatic application of migrations
  - Repeatable migrations for dev environments
  - Good tooling and IDE support
  - Clear migration history
- **Negative**:
  - Manual SQL writing (no ORM-generated migrations)
  - Rollback requires manual scripts
  - Must coordinate with deployments
  - Failed migrations can block startup

## ADR-013: Checkstyle for Multi-Format Code Quality

**Status**: Accepted

**Context**:
The project includes multiple file types:
- Java source code
- Bonaparte DSL files (.bon, .bddl)
- XML configuration
- SQL scripts
- Properties files

All need consistent formatting and quality checks.

**Decision**:
Use Checkstyle with custom rules that validate:
- Java code style
- Bonaparte DSL formatting
- XML formatting
- SQL formatting
- Properties file formatting

**Consequences**:
- **Positive**:
  - Consistent code style across project
  - Catches common mistakes early
  - Enforces best practices
  - Single tool for multiple formats
  - Integrates with CI/CD
- **Negative**:
  - Slight increase of build time / build resource requirements
  - Initial configuration complexity
  - May conflict with personal preferences
  - Maintenance of custom rules

## ADR-014: Architecture Testing with ArchUnit

**Status**: Accepted

**Context**:
With 150+ modules, it's easy to:
- Introduce circular dependencies
- Violate layer boundaries
- Use internal APIs inappropriately
- Break architectural principles

**Decision**:
Use ArchUnit to write automated tests that enforce architectural rules:
- Layer dependency rules
- Package structure conventions
- Naming conventions
- Access restrictions

**Consequences**:
- **Positive**:
  - Architecture enforced in CI/CD
  - Prevents gradual decay
  - Documents architecture rules in code
  - Fast feedback on violations
  - Easy to extend with new rules
- **Negative**:
  - Additional test maintenance
  - Can be too restrictive
  - Requires understanding of ArchUnit DSL
  - May slow down test execution

## ADR-015: Optional Module System

**Status**: Accepted

**Context**:
Different customers need different feature sets:
- Small deployments don't need all features
- Some features require expensive licenses (ZK EE)
- Development should be possible without all modules
- Deployment size should be minimized

**Decision**:
Design modules as optional and composable:
- Core modules required for all deployments
- Feature modules included as needed
- Plugin interfaces allow extending functionality
- Module presence detected at runtime

**Consequences**:
- **Positive**:
  - Smaller deployment footprint
  - Lower licensing costs for minimal deployments
  - Faster startup with fewer modules
  - Customers use only for what they use
  - Less memory requirements of JVM
- **Negative**:
  - Complexity in handling optional dependencies
  - Testing all combinations is difficult
  - Documentation must indicate which modules are needed
  - Runtime errors if required module missing

## ADR-016: Container-First Deployment

**Status**: Accepted

**Context**:
Modern deployment increasingly uses:
- Docker containers
- Kubernetes orchestration
- Cloud-native patterns
- Infrastructure as code

**Decision**:
Prioritize container deployment:
- Dockerfile in repository
- Multi-stage builds for optimization
- Docker Compose for local development
- Kubernetes-ready design (stateless, health checks)

**Consequences**:
- **Positive**:
  - Consistent environment across dev/test/prod
  - Easy local development setup
  - Cloud deployment flexibility
  - Modern DevOps practices
  - Scalability built-in
- **Negative**:
  - Requires Docker knowledge
  - More complex than simple JAR deployment
  - Container image size management
  - Debugging can be harder



## Future Architecture Decisions

**Areas for Future ADRs**:
- Migration to newer Java versions (21 → 25)
- GraphQL API addition
- Event sourcing for certain domains
- CQRS pattern adoption
- Observability strategy (OpenTelemetry)

## Decision Process

**When to Create an ADR**:
- Technology choice affecting multiple modules
- Change to fundamental architecture principles
- Significant impact on developers or operations
- Trade-offs between multiple valid options

**ADR Template**:
```markdown
## ADR-XXX: [Title]

**Status**: [Proposed | Accepted | Deprecated | Superseded]

**Context**: [What forces are at play? What problem needs solving?]

**Decision**: [What decision was made?]

**Consequences**: [What are the positive and negative outcomes?]
```

**Review Process**:
1. Proposal by developer/architect
2. Review by architecture team
3. Discussion and refinement
4. Decision and documentation
5. Implementation and communication
