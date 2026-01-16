# 12. Glossary

## A

**API (Application Programming Interface)**
: Set of interfaces and contracts for interacting with the system. In t9t, primarily refers to `*-api` modules containing DTOs and request/response definitions.

**Arc42**
: A template for architecture documentation, used for this documentation set. See [arc42.org](https://arc42.org).

**Architecture Test**
: Automated tests that verify architectural rules and constraints using ArchUnit. Located in `t9t-tests-arch*` modules.

**Autonomous Transaction**
: A transaction that runs independently of the calling transaction, often used for logging or auditing that must persist even if the main transaction rolls back.

**AWS Aurora**
: Amazon's cloud-native relational database service, PostgreSQL-compatible, with automatic failover and replication capabilities.

## B

**BDDL (Bonaparte Database Definition Language)**
: DSL for defining JPA entities. Files with `.bddl` extension that define database table mappings.

**BE (Business Logic)**
: Suffix for modules containing business logic implementation (`*-be`). Implements request handlers and services.

**Bonaparte**
: Custom domain-specific language and framework for defining type-safe data structures. Used for all DTOs, requests, responses, and entities in t9t.

**BON File**
: Bonaparte definition file with `.bon` extension. Defines DTOs, requests, and responses.

**BOM (Bill of Materials)**
: Maven POM file that centralizes dependency version management. t9t uses multiple BOMs including internal jpaw-bom, jdp-bom, bonaparte-bom.

**BPMN (Business Process Model and Notation)**
: Standard for business process modeling. t9t includes modules for BPMN support.

**Business Logic Layer**
: Layer containing domain-specific business logic, implemented in `*-be` modules.

## C

**C3P0**
: Connection pooling library used for primary (write) database connections in t9t.

**Circuit Breaker**
: Design pattern that prevents cascading failures by detecting failures and temporarily stopping requests to failing services.

**Cluster**
: Multiple server instances working together, coordinated via Hazelcast or Kafka.

**ConfigMap**
: Kubernetes resource for storing configuration data as key-value pairs.

**Cross-Cutting Concern**
: Aspect of a system that affects multiple modules (e.g., logging, security, transactions).

**CRUD (Create, Read, Update, Delete)**
: Basic data operations. t9t provides generic CRUD operations via `CrudRequest` types.

## D

**DAO (Data Access Object)**
: Pattern for abstracting data persistence. In t9t, this is handled by JPA repositories.

**Dependency Injection (DI)**
: Design pattern for inverting control of object creation. t9t uses the jdp framework for DI.

**DTO (Data Transfer Object)**
: Object that carries data between layers or processes. All DTOs in t9t are defined in Bonaparte.

**DSL (Domain-Specific Language)**
: Programming language specialized for a particular domain. Bonaparte is a DSL for data structures.

## E

**EclipseLink**
: JPA implementation alternative to Hibernate, supported via `t9t-orm-eclipselink-jpa`.

**Elasticsearch**
: Search and analytics engine, supported as a Hibernate Search backend.

**Embedded Test**
: Integration test that runs with an embedded database, located in `t9t-tests-embedded`.

**Entity**
: JPA object representing a database table row. Defined in `*-jpa` modules.

## F

**Flyway**
: Database migration tool used for versioned schema updates.

**fortytwo**
: Predecessor of the **t9t** framework. **fortytwo**, as the answer to all questions, was more complicated (somewhat overengineered) and used a classic Java EE application server (JBOSS), which required an EAR file to be deployed separately, instead of the lean embedded **vert.x** framework.

## G

**Gateway**
: Entry point to the system. t9t has multiple gateways:
- REST Gateway (`t9t-gateway-rest`)
- MCP Gateway (`t9t-gateway-mcp`)

**Global DM**
: Global Dependency Management module (`global-dm`) that manages versions across all modules.

## H

**Handler**
: Class implementing `IRequestHandler<T>` that processes a specific request type.

**Hazelcast**
: In-memory data grid used for distributed caching and clustering.

**Hibernate**
: JPA implementation, primary ORM used in t9t via `t9t-orm-hibernate-jpa`.

**Hikari**
: Fast, lightweight JDBC connection pool used for shadow (read-only) database connections.

## I

**I/O (Input/Output)**
: File-based data exchange. Handled by `t9t-io-*` modules for import/export operations.

**IntelliJ IDEA**
: JetBrains IDE, supported as an alternative to Eclipse for t9t development.

**Integration Test**
: Test that verifies interaction between multiple components. See Embedded Test and Remote Test.

## J

**JAX-RS (Java API for RESTful Web Services)**
: Java standard for REST APIs, implemented using RESTEasy in t9t.

**JCEKS (Java Cryptography Extension KeyStore)**
: Keystore format used for JWT signing keys in t9t.

**Jdp (jpaw dependency injection)**
: Lightweight dependency injection framework used by t9t instead of Spring.

**Jetty**
: Embedded web server used in t9t for HTTP/HTTPS connectivity.

**JMX (Java Management Extensions)**
: Technology for monitoring and managing Java applications.

**JPA (Java Persistence API)**
: Standard Java API for object-relational mapping. Modules suffixed with `*-jpa`.

**JWT (JSON Web Token)**
: Standard for stateless authentication tokens. Used for user authentication in t9t.

## K

**Kafka**
: Distributed event streaming platform used for asynchronous messaging in t9t.

**Kubernetes (K8s)**
: Container orchestration platform supported for t9t deployment.

## L

**Langchain**
: Framework for building LLM applications, integrated via `t9t-ai-be-langchain`.

**Layer**
: Horizontal slice of architecture with specific responsibilities:
- API Layer: Interfaces and DTOs
- SAPI Layer: Service interfaces
- BE Layer: Business logic
- JPA Layer: Persistence

**LLM (Large Language Model)**
: AI models for text generation. Supported via OpenAI and Ollama modules.

**Logback**
: Logging backend used in the slf4j framework, successor to Log4j, used throughout t9t.

**Lucene**
: Search engine library, supported as Hibernate Search backend.

## M

**Maven**
: Build automation and dependency management tool. t9t uses multi-module Maven project structure.

**MCP (Model Context Protocol)**
: Protocol for AI tool integration. Supported via `t9t-gateway-mcp`.

**MDC (Mapped Diagnostic Context)**
: Logback feature for adding contextual information to log messages.

**Microservices**
: Architectural style with loosely coupled services. t9t is currently monolithic but designed for potential migration.

**Module**
: Maven module, a distinct unit of code with its own `pom.xml`. t9t has 150+ modules.

**Multi-Tenancy**
: Architectural approach where a single instance serves multiple customers (tenants) with data isolation.

## O

**Ollama**
: Local LLM hosting platform, integrated via `t9t-ollama-*` modules.

**OpenAI**
: AI platform providing GPT models, integrated via `t9t-openai-*` modules.

**ORM (Object-Relational Mapping)**
: Technique for mapping objects to database tables. Implemented via JPA.

**Optimistic Locking**
: Concurrency control strategy using version numbers to detect concurrent modifications.

## P

**PaaS (Platform as a Service)**
: Cloud computing model. t9t can be deployed on various PaaS platforms.

**Persistence Layer**
: Layer responsible for data storage and retrieval, implemented in `*-jpa` modules.

**Pinecone**
: Vector database for AI applications, integrated via `t9t-vdb-be-pinecone`.

**Plugin Architecture**
: Design allowing functionality extension without modifying core code, implemented via service interfaces.

**POI (Apache POI)**
: Library for reading/writing Microsoft Office formats, used in `t9t-io-be-poi`.

**PostgreSQL**
: Open source relational database, primary database for t9t.

**Primary Database**
: Main database connection used for write operations and transactions.

**Process Reference (objectRef)**
: Unique identifier for each request execution, used for tracking and correlation.

## Q

**Qdrant**
: Vector database for AI applications, integrated via `t9t-vdb-be-qdrant`.

**Quality Gate**
: Automated check that must pass before code can be merged or deployed.

## R

**RAG (Retrieval Augmented Generation)**
: AI pattern combining vector search with LLM generation for context-aware responses.

**Read Replica**
: Copy of database optimized for read operations. See Shadow Database.

**Remote Test**
: Integration test that runs against a deployed instance, located in `t9t-tests-remote`.

**Request**
: DTO extending `RequestParameters` that represents a business operation to be performed.

**Request Handler**
: Class implementing `IRequestHandler<T>` that processes a specific request type.

**Response**
: DTO extending `ServiceResponse` that represents the result of a request.

**REST (Representational State Transfer)**
: Architectural style for APIs. t9t exposes REST APIs via gateway modules.

**RESTEasy**
: JAX-RS implementation used in t9t.

**Return Code**
: Integer in `ServiceResponse` indicating success (0) or error (>0).

## S

**SAAS (Software as a Service)**
: Cloud delivery model. t9t's multi-tenancy supports SaaS deployments.

**SAPI (Service API)**
: Module suffix for service interface definitions (`*-sapi`).

**Service Layer**
: Layer defining business service interfaces, implemented in `*-sapi` modules.

**Shadow Database**
: Read-only database replica used for queries and reports, reducing load on primary database.

**SLF4J (Simple Logging Facade for Java)**
: Logging abstraction used throughout t9t.

**SMTP (Simple Mail Transfer Protocol)**
: Protocol for sending email, used by email modules.

**Solr (Apache Solr)**
: Search platform, integrated via `t9t-solr-*` modules.

**SSE (Server-Sent Events)**
: Protocol for server-to-client streaming over HTTP, used by MCP Gateway.

**Stateless**
: Architecture where servers don't maintain session state, enabling horizontal scaling.

## T

**t9t**
: Name of the enterprise framework. Short for "twentyeight" (in math: the second perfect number). Successor of **fortytwo**.

**Tenant**
: A customer/organization in a multi-tenant system. All data is associated with a tenant.

**tenantId**
: Human readable (alphanumeric) reference field present in all entities identifying which tenant owns the data.

**tenantRef**
: Technical reference field (numeric), was present in **fortytwo** in all entities identifying which tenant owns the data, has been replaced by the better readable **tenantId** now.

**Thread Pool**
: Collection of threads for executing tasks. t9t has worker and autonomous thread pools.

**TLS (Transport Layer Security)**
: Cryptographic protocol for secure communication, used for HTTPS.

**Transaction**
: Unit of work executed atomically in the database (all or nothing).

## U

**UI (User Interface)**
: Visual interface for users. t9t uses the ZK Framework for the administration web UI.

**Unit Test**
: Test of a single component in isolation, located in various backend modules.

## V

**Vector Database**
: Database optimized for storing and searching high-dimensional vectors, used for AI applications.

**Vert.x**
: Reactive toolkit for building applications on the JVM, used in async modules.

## W

**WAR (Web Application Archive)**
: Packaging format for Java web applications.

**Workflow**
: Automated business process. Supported via BPMN modules.

## X

**Xtext**
: Framework for developing domain-specific languages, under the umbrella of the Eclipse foundation. Used to implement Bonaparte DSL.

**XML (eXtensible Markup Language)**
: Text format for structured data. Used for configuration (`t9tconfig.xml`).

## Z

**ZK Framework**
: Java web framework for building rich UIs. Used for t9t web interface in `t9t-zkui-*` modules.


---

## Acronym Quick Reference

| Acronym | Full Term |
|---------|-----------|
| ADR | Architecture Decision Record |
| API | Application Programming Interface |
| BDDL | Bonaparte Database Definition Language |
| BE | Business Logic (layer/module) |
| BOM | Bill of Materials |
| BPMN | Business Process Model and Notation |
| CDI | Contexts and Dependency Injection |
| CI/CD | Continuous Integration/Continuous Deployment |
| CPU | Central Processing Unit |
| CRUD | Create, Read, Update, Delete |
| DAO | Data Access Object |
| DB | Database |
| DI | Dependency Injection |
| DTO | Data Transfer Object |
| DSL | Domain-Specific Language |
| GDPR | General Data Protection Regulation |
| GC | Garbage Collection |
| HTTP | Hypertext Transfer Protocol |
| HTTPS | HTTP Secure |
| I/O | Input/Output |
| IDE | Integrated Development Environment |
| JAR | Java Archive |
| JAX-RS | Java API for RESTful Web Services |
| JDBC | Java Database Connectivity |
| JDK | Java Development Kit |
| JCEKS | Java Cryptography Extension KeyStore |
| JMX | Java Management Extensions |
| JPA | Java Persistence API |
| JSON | JavaScript Object Notation |
| JTA | Java Transaction API |
| JVM | Java Virtual Machine |
| JWT | JSON Web Token |
| K8s | Kubernetes |
| LLM | Large Language Model |
| MCP | Model Context Protocol |
| MDC | Mapped Diagnostic Context |
| MSK | Managed Streaming for Kafka (AWS) |
| ORM | Object-Relational Mapping |
| PaaS | Platform as a Service |
| POI | Poor Obfuscation Implementation (Apache POI) |
| RDBMS | Relational Database Management System |
| REST | Representational State Transfer |
| RPO | Recovery Point Objective |
| RTO | Recovery Time Objective |
| SaaS | Software as a Service |
| SAPI | Service API (layer/module) |
| SLA | Service Level Agreement |
| SLF4J | Simple Logging Facade for Java |
| SMTP | Simple Mail Transfer Protocol |
| SQL | Structured Query Language |
| SSE | Server-Sent Events |
| SSL | Secure Sockets Layer |
| TLS | Transport Layer Security |
| TTL | Time To Live |
| UI | User Interface |
| URL | Uniform Resource Locator |
| UUID | Universally Unique Identifier |
| WAR | Web Application Archive |
| XML | eXtensible Markup Language |

---

## Module Naming Conventions

Understanding t9t module naming:

**Pattern**: `t9t-<domain>-<layer>`

**Domains** (examples):
- `auth` - Authentication and authorization
- `base` - Core framework
- `doc` - Document processing
- `io` - Input/output operations
- `email` - Email handling
- `ai` - Artificial intelligence
- `solr` - Apache Solr search
- `rep` - Reporting

**Layers**:
- `api` - API definitions (DTOs, requests, responses)
- `sapi` - Service API interfaces
- `be` - Business logic implementation
- `jpa` - JPA entities and persistence
- `vertx` - Vert.x async implementation
- `mock` - Mock implementation for testing

**Special Suffixes**:
- `apiext` - Extended API features
- `be-<tech>` - Technology-specific implementation (e.g., `be-smtp`, `be-aws`)
- `nodb` - No database implementation

**Examples**:
- `t9t-auth-api` - Authentication API definitions
- `t9t-auth-be` - Authentication business logic
- `t9t-auth-jpa` - Authentication database entities
- `t9t-doc-be-pdf` - PDF document generation implementation
- `t9t-email-be-smtp` - SMTP email sending implementation

---

**Note**: This glossary is a living document. Add terms as they are introduced in the codebase and documentation.
