# 5. Building Block View

## Whitebox Overall System

### System Overview

The t9t framework is structured as a collection of 150+ Maven modules organized by functional domain and architectural layer. The system follows a layered architecture with strict dependency rules.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              t9t Framework                              │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌────────────────────────────────────────────────────────────────┐     │
│  │                    Presentation Layer                          │     │
│  ├────────────────────────────────────────────────────────────────┤     │
│  │  t9t-zkui-*    t9t-gateway-rest    t9t-gateway-mcp   ...       │     │
│  └───────────────────────┬────────────────────────────────────────┘     │
│                          │                                              │
│  ┌───────────────────────▼────────────────────────────────────────┐     │
│  │              Core Processing & Dispatch                        │     │
│  ├────────────────────────────────────────────────────────────────┤     │
│  │  t9t-server   t9t-base-be   Request Dispatching                │     │
│  └───────────────────────┬────────────────────────────────────────┘     │
│                          │                                              │
│  ┌───────────────────────▼────────────────────────────────────────┐     │
│  │                  Business Domain Modules                       │     │
│  ├────────────────────────────────────────────────────────────────┤     │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐           │     │
│  │  │  Auth    │ │   Doc    │ │   I/O    │ │  Email   │ ...       │     │
│  │  │  Module  │ │  Module  │ │  Module  │ │  Module  │           │     │
│  │  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘           │     │
│  │       │            │            │            │                 │     │
│  │  ┌────▼────────────▼────────────▼────────────▼─────-─┐         │     │
│  │  │           Service Layer (SAPI)                    │         │     │
│  │  │  Interfaces, Plugin Points, Business Contracts    │         │     │
│  │  └────┬──────────────────────────────────────────────┘         │     │
│  │       │                                                        │     │
│  │  ┌────▼──────────────────────────────────────────────┐         │     │
│  │  │           Persistence Layer (JPA)                 │         │     │
│  │  │  Entities, Repositories, ORM Configuration        │         │     │
│  │  └────┬──────────────────────────────────────────────┘         │     │
│  └───────┼────────────────────────────────────────────────────────┘     │
│          │                                                              │
│  ┌───────▼────────────────────────────────────────────────────────┐     │
│  │              Foundation Layer                                  │     │
│  ├────────────────────────────────────────────────────────────────┤     │
│  │  t9t-base-api   t9t-core-api   Global DM   Infrastructure      │     │
│  └────────────────────────────────────────────────────────────────┘     │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┼───────────────┐
                    │               │               │
               ┌────▼────┐    ┌─────▼─────┐   ┌────▼────┐
               │Database │    │  Kafka    │   │External │
               │(Postgres)    │  Cluster  │   │ Systems │
               └─────────┘    └───────────┘   └─────────┘
```

### Contained Building Blocks

| Block | Responsibility | Reference |
|-------|---------------|-----------|
| **Foundation Layer** | Core APIs, base types, infrastructure | Level 1 - Foundation |
| **Domain Modules** | Business logic organized by domain | Level 1 - Domain Modules |
| **Service Layer** | Service interfaces and contracts | Level 1 - Service Layer |
| **Persistence Layer** | Data persistence and ORM | Level 1 - Persistence |
| **Core Processing** | Request dispatch, lifecycle | Level 1 - Core Processing |
| **Presentation Layer** | User interfaces and gateways | Level 1 - Presentation |

### Important Interfaces

| Interface | Source → Target | Description |
|-----------|----------------|-------------|
| **Request/Response** | Client → Server | Bonaparte-based RPC protocol |
| **REST API** | External → Gateway | JSON/XML over HTTP |
| **MCP Protocol** | AI Tools → MCP Gateway | Model Context Protocol |
| **JPA** | Business Logic → Database | Java Persistence API |
| **SAPI** | Business Logic → Services | Service provider interface |
| **Kafka** | System → Message Broker | Event streaming |

## Level 1: Foundation Layer

### Whitebox: Foundation

```
┌───────────────────────────────────────────────────────────┐
│                    Foundation Layer                       │
├───────────────────────────────────────────────────────────┤
│                                                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │  t9t-base-api│  │ t9t-core-api │  │  global-dm   │     │
│  │              │  │              │  │              │     │
│  │ • Base types │  │ • Core DTOs  │  │ • Dependency │     │
│  │ • Auth DTOs  │  │ • Core req.  │  │   management │     │
│  │ • CRUD ops   │  │ • Events     │  │ • Versions   │     │
│  │ • Search     │  │              │  │              │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
│                                                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │t9t-base-sapi │  │t9t-core-sapi │  │  t9t-init    │     │
│  │              │  │              │  │              │     │
│  │ • IRequest   │  │ • IExecutor  │  │ • Bootstrap  │     │
│  │   Handler    │  │ • IAsync     │  │ • Config     │     │
│  │ • IAuthorizer│  │              │  │   loading    │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
│                                                           │
│  ┌──────────────────────────────────────────────────┐     │
│  │            t9t-base-be                           │     │
│  │  • Request dispatcher                            │     │
│  │  • Authentication executor                       │     │
│  │  • Authorization checks                          │     │
│  │  • Lifecycle management                          │     │
│  └──────────────────────────────────────────────────┘     │
│                                                           │
└───────────────────────────────────────────────────────────┘
```

#### Contained Building Blocks

**global-dm**
- Purpose: Dependency management BOM
- Responsibilities:
  - Centralized version management
  - Consistent dependencies across modules
  - Build configuration

**t9t-init**
- Purpose: System initialization
- Responsibilities:
  - Bootstrap process
  - Configuration loading
  - Module registration
  - Dependency injection setup

**t9t-cfg-be**
- Purpose: Backend configuration
- Responsibilities:
  - Provide data structure for T9tServerConfiguration
  - Configuration loading, enrich secrets from environment
  - APIs to provide configuration to program code

**t9t-base-api**
- Purpose: Fundamental types and interfaces used across all modules
- Responsibilities:
  - Base DTO definitions
  - CRUD request/response types
  - Search and query types
  - Authentication DTOs
- Key Classes: `RequestParameters`, `ServiceResponse`, `SearchRequest`, `CrudRequest`

**t9t-base-sapi**
- Purpose: Core service interfaces
- Responsibilities:
  - `IRequestHandler<T>` - Handler interface
  - `IAuthorizer` - Authorization interface
  - `IExecutor` - Interface to perform subrequests within the same transaction, or asynchronously
  - `ITextSearch` - Search service interface
- Provides plugin points for all business logic
- Key Class: `RequestContext`: Context of the execution of the current request (also contains authentication information required for callout to other services)

**t9t-base-be**
- Purpose: Core request processing implementation
- Responsibilities:
  - Request routing and dispatch
  - Authentication/authorization execution
  - Transaction management
  - Error handling and logging
  - Cross-cutting concerns
- Key Classes: `Executor`: Implementation of `IExecutor`, `RequestProcessor`

**t9t-base-jpa**
- Purpose: Core JPA based relational database implementation
- Responsibilities:
  - Provides superclasses of resolvers, mappers, CRUD request handlers

**t9t-core-api/sapi/be**
- Purpose: Core business objects and operations
- Responsibilities:
  - Tenant management
  - Module configuration
  - System events
  - Core business entities

## Level 1: Domain Modules

### Whitebox: Domain Modules

Each domain module follows a consistent structure with 4 layers:

```
Domain Module Pattern (e.g., t9t-auth-*, t9t-doc-*, t9t-io-*)

┌────────────────────────────────────────────┐
│         Domain: {domain-name}              │
├────────────────────────────────────────────┤
│                                            │
│  ┌────────────────────────────────────┐    │
│  │  {domain}-api                      │    │
│  │  • Request DTOs                    │    │
│  │  • Response DTOs                   │    │
│  │  • Domain types                    │    │
│  │  • Constants                       │    │
│  └────────────────────────────────────┘    │
│                   ▲                        │
│  ┌────────────────┴───────────────────┐    │
│  │  {domain}-sapi                     │    │
│  │  • Service interfaces              │    │
│  │  • Plugin points                   │    │
│  │  • Business contracts              │    │
│  └────────────────────────────────────┘    │
│                   ▲                        │
│  ┌────────────────┴───────────────────┐    │
│  │  {domain}-be                       │    │
│  │  • Request handlers                │    │
│  │  • Business logic                  │    │
│  │  • Service implementations         │    │
│  └─────────┬──────────────────────────┘    │
│            │                               │
│  ┌─────────▼──────────────────────────┐    │
│  │  {domain}-jpa                      │    │
│  │  • JPA entities                    │    │
│  │  • Repositories                    │    │
│  │  • Converters                      │    │
│  └────────────────────────────────────┘    │
│                                            │
└────────────────────────────────────────────┘
```

### Key Domain Modules

#### 1. Authentication Domain (t9t-auth-*)

**Purpose**: User authentication, authorization, and session management

**Modules**:
- `t9t-auth-api`: User DTOs, authentication requests
- `t9t-auth-apiext`: Extended API features
- `t9t-auth-sapi`: Authentication service interfaces
- `t9t-auth-be`: Authentication logic, JWT validation
- `t9t-auth-jpa`: User, role, permission entities
- `t9t-auth-vertx`: Vert.x-based async auth
- `t9t-auth-mock`: Mock implementation for testing
- `t9t-auth-nodb`: In-memory implementation (used for some outbound gateways such as PSP connectivities or fiscalization gateways in applications built with t9t)

**Key Responsibilities**:
- User login/logout
- JWT token generation and validation
- Permission checks
- Role-based access control
- Multi-tenant user management
- Password policies
- API key management
- Integration with MS Entra

#### 2. Document Domain (t9t-doc-*)

**Purpose**: Document generation, transformation, and management

**Modules**:
- `t9t-doc-api`: Document requests, format types
- `t9t-doc-sapi`: Document service interfaces
- `t9t-doc-be`: Document processing core
- `t9t-doc-be-pdf`: PDF generation (via iText, Flying Saucer)
- `t9t-doc-jpa`: Document templates, metadata

**Key Responsibilities**:
- Template-based document generation
- PDF generation
- Format conversion
- Document archiving
- Template management
- Dynamic data binding

#### 3. I/O Domain (t9t-io-*)

**Purpose**: Data import, export, and file operations

**Modules**:
- `t9t-io-api`: I/O requests, format definitions
- `t9t-io-sapi`: I/O service interfaces
- `t9t-io-sapi-intern`: Internal I/O services
- `t9t-io-be`: Core I/O processing
- `t9t-io-be-intern`: Internal I/O implementation
- `t9t-io-be-aws`: AWS S3 integration
- `t9t-io-be-camel`: Apache Camel integration
- `t9t-io-be-camel-k8s`: Kubernetes-specific Camel
- `t9t-io-be-kafka`: Kafka-based I/O
- `t9t-io-be-mfcobol`: Microfocus COBOL data format support
- `t9t-io-be-poi`: Excel processing (Apache POI)
- `t9t-io-jpa`: I/O queue, statistics entities
- `t9t-io-jpa-proxy`: Proxy entities

**Key Responsibilities**:
- CSV import/export
- Excel import/export
- Fixed-width format support
- File upload/download
- Scheduled data processing
- Format validation
- Data transformation

#### 4. Email Domain (t9t-email-*)

**Purpose**: Email generation, templating, and delivery

**Modules**:
- `t9t-email-api`: Email requests, configuration
- `t9t-email-sapi`: Email service interfaces
- `t9t-email-be`: Email processing core
- `t9t-email-be-smtp`: SMTP implementation
- `t9t-email-be-vertx`: Vert.x async email
- `t9t-email-jpa`: Email templates, queue

**Key Responsibilities**:
- Template-based email generation
- HTML and plain text emails
- Attachment handling
- Email queuing
- Retry logic
- SMTP configuration

#### 5. AI Domain (t9t-ai-*, t9t-openai-*, t9t-ollama-*)

**Purpose**: AI and LLM integration

**Modules**:
- `t9t-ai-api`: AI requests, conversation types
- `t9t-ai-sapi`: AI service interfaces
- `t9t-ai-be`: Core AI processing
- `t9t-ai-be-langchain`: LangChain integration
- `t9t-ai-jpa`: AI conversation history
- `t9t-ai-mcp`: MCP tool definitions
- `t9t-openai-api/be/sapi`: OpenAI integration
- `t9t-ollama-api/be/sapi`: Ollama integration

**Key Responsibilities**:
- LLM request/response handling
- Conversation management
- Prompt templating
- Vector embedding generation
- RAG (Retrieval Augmented Generation) support
- Multiple LLM provider support

#### 6. Search Domain (t9t-solr-*, t9t-hibernate-search-*)

**Purpose**: Full-text search and indexing

**Modules**:
- `t9t-solr-api/apiext/sapi/be/jpa`: Apache Solr integration
- `t9t-hibernate-search-api/be`: Hibernate Search core
- `t9t-hibernate-search-be-lucene`: Lucene backend
- `t9t-hibernate-search-be-elasticsearch`: Elasticsearch backend

**Key Responsibilities**:
- Full-text indexing
- Complex search queries
- Faceted search
- Highlighting
- Search result ranking

#### 7. Reporting Domain (t9t-rep-*)

**Purpose**: Report generation and management

**Modules**:
- `t9t-rep-api`: Report requests
- `t9t-rep-sapi`: Report service interfaces
- `t9t-rep-be`: Report processing (based on JasperReports)
- `t9t-rep-jpa`: Report definitions
- `t9t-rep-proxy`: Report proxy

**Key Responsibilities**:
- Report execution
- Data aggregation
- Export to multiple formats
- Scheduled reports
- Report templates

#### 8. Translation Domain (t9t-translation-*, t9t-translations-*)

**Purpose**: Multi-language support and translation management

**Modules**:
- `t9t-translation-api/sapi/be/be-intern`: Translation services
- `t9t-translations-api/sapi/be/jpa`: Translation storage

**Key Responsibilities**:
- Text translation
- Language detection
- Translation caching
- Fallback handling

#### 9. Vector Database Domain (t9t-vdb-*)

**Purpose**: Vector storage for AI/ML applications

**Modules**:
- `t9t-vdb-api/sapi/be/jpa`: Vector DB interfaces
- `t9t-vdb-be-pinecone`: Pinecone integration
- `t9t-vdb-be-qdrant`: Qdrant integration
- `t9t-vdb-jpa-pgvector`: PostgreSQL pgvector

**Key Responsibilities**:
- Vector embedding storage
- Similarity search
- Vector indexing
- Multiple backend support

## Level 1: Persistence Layer

### Whitebox: Persistence

```
┌─────────────────────────────────────────────────────────────┐
│                    Persistence Layer                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  JPA Entities (*-jpa modules)                       │    │
│  │  • Domain entity definitions (.bddl)                │    │
│  │  • Generated JPA entities                           │    │
│  │  • Repository interfaces                            │    │
│  └───────────────────┬─────────────────────────────────┘    │
│                      │                                      │
│  ┌───────────────────▼─────────────────────────────────┐    │
│  │  ORM Implementation                                 │    │
│  │  ┌──────────────────┐  ┌──────────────────┐         │    │
│  │  │ t9t-orm-         │  │ t9t-orm-         │         │    │
│  │  │ hibernate-jpa    │  │ eclipselink-jpa  │         │    │
│  │  │                  │  │                  │         │    │
│  │  │ • Hibernate impl │  │ • EclipseLink    │         │    │
│  │  │ • Clustering     │  │   impl           │         │    │
│  │  └──────────────────┘  └──────────────────┘         │    │
│  └───────────────────┬─────────────────────────────────┘    │
│                      │                                      │
│  ┌───────────────────▼─────────────────────────────────┐    │
│  │  Connection Management                              │    │
│  │  • t9t-base-jpa      (Common base code)             │    │
│  │  • t9t-base-jpa-rl   (Resource-local)               │    │
│  │  • t9t-base-jpa-st   (Spring Data based)            │    │
│  │  • t9t-base-jpa-jta  (JTA support)                  │    │
│  │  • C3P0 pooling      (Write)                        │    │
│  │  • Hikari pooling    (Read, JDBC)                   │    │
│  └───────────────────┬─────────────────────────────────┘    │
│                      │                                      │
│  ┌───────────────────▼─────────────────────────────────┐    │
│  │  Database Access                                    │    │
│  │  • JDBC drivers                                     │    │
│  │  • AWS Aurora wrapper (failover)                    │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
                         │
                         ▼
                  ┌──────────────┐
                  │  PostgreSQL  │
                  │   Database   │
                  └──────────────┘
```

**Key Components**:

- **JPA Entities**: Generated from Bonaparte BDDL files, ensure type safety between Java and database
- **ORM Layer**: Pluggable implementation (Hibernate or EclipseLink)
- **Connection Pooling**: Separate pools for read/write optimization
- **Transaction Management**: JTA support for distributed transactions
- **Migration**: Flyway-based schema versioning

## Level 1: Core Processing

### Whitebox: Core Processing

```
┌────────────────────────────────────────────────────────────┐
│                    Core Processing                         │
├────────────────────────────────────────────────────────────┤
│                                                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │         t9t-server (Main Server)                     │  │
│  │  • JVM Bootstrap                                     │  │
│  │  • Configuration loading                             │  │
│  │  • Module initialization                             │  │
│  │  • Jetty server startup                              │  │
│  └────────────────────┬─────────────────────────────────┘  │
│                       │                                    │
│  ┌────────────────────▼─────────────────────────────────┐  │
│  │      Request Processing Pipeline                     │  │
│  │  ┌────────────────────────────────────────────┐      │  │
│  │  │ 1. Authentication & Request Validation     │      │  │
│  │  └────────────────┬───────────────────────────┘      │  │
│  │                   │                                  │  │
│  │  ┌────────────────▼───────────────────────────┐      │  │
│  │  │ 2. Authorization Check                     │      │  │
│  │  └────────────────┬───────────────────────────┘      │  │
│  │                   │                                  │  │
│  │  ┌────────────────▼───────────────────────────┐      │  │
│  │  │ 3. Request Dispatcher                      │      │  │
│  │  │    • Find appropriate handler              │      │  │
│  │  │    • Transaction management                │      │  │
│  │  └────────────────┬───────────────────────────┘      │  │
│  │                   │                                  │  │
│  │  ┌────────────────▼───────────────────────────┐      │  │
│  │  │ 4. Handler Execution                       │      │  │
│  │  │    • Domain-specific logic                 │      │  │
│  │  │    • Service calls                         │      │  │
│  │  └────────────────┬───────────────────────────┘      │  │
│  │                   │                                  │  │
│  │  ┌────────────────▼───────────────────────────┐      │  │
│  │  │ 5. Response Generation                     │      │  │
│  │  │    • Serialization                         │      │  │
│  │  │    • Error handling                        │      │  │
│  │  └────────────────────────────────────────────┘      │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │      Asynchronous Processing                         │  │
│  │  • Worker thread pools                               │  │
│  │  • Autonomous pools                                  │  │
│  │  • Scheduled tasks                                   │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

**Key Responsibilities**:
- Request lifecycle management
- Transaction boundaries
- Error handling and logging
- Metrics collection
- Thread pool management

## Level 1: Presentation Layer

### Whitebox: Presentation

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────┐   │
│  │  t9t-zkui-*      │  │ t9t-gateway-rest │  │  t9t-    │   │
│  │                  │  │                  │  │ gateway- │   │
│  │  • ZK screens    │  │  • REST endpoints│  │   mcp    │   │
│  │  • UI components │  │  • JSON/XML      │  │          │   │
│  │  • View models   │  │  • Authentication│  │  • MCP   │   │
│  │  • Controllers   │  │                  │  │   tools  │   │
│  └──────────────────┘  └──────────────────┘  └──────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

**Components**:
1. **ZK UI**: Server-side Java UI framework for web applications
2. **REST Gateway**: RESTful API for external integrations
3. **MCP Gateway**: Model Context Protocol for AI tools

## Module Dependencies

### Dependency Rules

**Strict rules enforced by Maven and architecture tests:**

1. **API modules** depend only on:
   - Other API modules
   - Base API
   - No implementation modules

2. **SAPI modules** depend on:
   - Corresponding API module
   - Other SAPI modules
   - No BE or JPA modules

3. **BE modules** depend on:
   - Corresponding API and SAPI
   - Other BE modules' APIs/SAPIs only
   - No direct JPA dependencies (use SAPI)

4. **JPA modules** depend on:
   - Corresponding API
   - Base JPA
   - No BE modules

5. **No circular dependencies** allowed at any level

## Key Design Patterns

### Used Patterns

| Pattern | Usage | Example Modules |
|---------|-------|-----------------|
| **Strategy** | Pluggable implementations via SAPI | All *-sapi/*-be pairs |
| **Factory** | Object creation | Request handler factories |
| **Template Method** | Request processing pipeline | AbstractRequestHandler |
| **Repository** | Data access | JPA repositories |
| **DTO** | Data transfer | All *-api modules (Bonaparte) |
| **Dependency Injection** | Component wiring | Jdp framework |
| **Builder** | Complex object construction | Request builders |
| **Facade** | Simplified interfaces | Service facades in SAPI |

