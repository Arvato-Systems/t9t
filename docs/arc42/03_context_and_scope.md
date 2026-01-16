# 3. Context and Scope

## Business Context

### System Overview

The t9t framework serves as an enterprise backend platform that enables organizations to build, deploy, and operate business-critical applications. It acts as a middleware layer providing core enterprise services.

```
┌─────────────────┐         ┌─────────────────┐         ┌─────────────────┐
│  Web Browsers   │         │  Mobile Apps    │         │ Desktop Clients │
│   (ZK UI)       │         │  (REST APIs)    │         │  (REST APIs)    │
└────────┬────────┘         └────────┬────────┘         └────────┬────────┘
         │                           │                           │
         └───────────────────────────┼───────────────────────────┘
                                     │
                          ┌──────────▼──────────┐
                          │                     │
                          │   t9t Framework     │
                          │   (Backend Core)    │
                          │                     │
                          └──────────┬──────────┘
                                     │
         ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━┻━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
         ┃                                                        ┃
    ┌────▼────┐  ┌────────┐  ┌──────┐  ┌──────┐  ┌────────┐  ┌──▼───┐
    │Database │  │  Kafka │  │ SMTP │  │  AWS │  │External│  │ LLMs │
    │(Postgres│  │ Cluster│  │Server│  │  S3  │  │  APIs  │  │ (AI) │
    └─────────┘  └────────┘  └──────┘  └──────┘  └────────┘  └──────┘
```

### External Interfaces - Business Level

| External Entity | Description | Interface Type |
|----------------|-------------|----------------|
| **End Users** | Business users accessing the system via web UI | HTTP/HTTPS (ZK Framework) |
| **API Clients** | External systems integrating via REST | REST/JSON over HTTP |
| **MCP Tools** | AI assistants and development tools | MCP (Model Context Protocol) |
| **Email Recipients** | Users receiving system-generated emails | SMTP |
| **Document Recipients** | External systems receiving generated documents | File I/O, S3, REST |
| **Business Partners** | B2B integrations | REST/SOAP APIs |
| **Administrators** | System administrators managing configuration | Web UI, Database direct access |

### Business Data Flow

**Typical Business Scenarios:**

1. **User Authentication Flow (ZK)**
   - User → ZK Web UI → t9t Auth Module → Database → JWT Token (stays within ZK server) → ZK session → User

2. **Document Generation**
   - Business Process → t9t Doc Module → Template Processing → PDF/Email → Recipient

3. **Data Import**
   - External File → t9t I/O Module → Validation → Database → Notification

4. **Report Generation**
   - User Request → t9t Rep Module → Data Query → Template Processing → Report Output

5. **AI Integration**
   - Application → t9t AI Module → LLM Service → Response Processing → Application

6. **User Authentication Flow (new Angular UIs, not part of t9t core framework)**
   - User → t9t REST gateway → t9t Auth Module → Database → JWT Token → REST gateway → User

7. **User Authentication Flow (deprecated Angular UIs, not part of t9t at all)**
   - User → t9t Auth Module → Database → JWT Token → User


## Technical Context

### System Environment

```
┌──────────────────────────────────────────────────────────────────┐
│                        Client Layer                              │
├──────────────────────────────────────────────────────────────────┤
│  Web Browsers │ REST Clients │ MCP Clients │ Kafka Consumers    │
└───────┬──────────────┬────────────┬─────────────────┬───────────┘
        │              │            │                 │
┌───────▼──────────────▼────────────▼─────────────────▼───────────┐
│                     Presentation Layer                           │
├──────────────────────────────────────────────────────────────────┤
│  t9t-zkui-*  │  t9t-gateway-rest  │  t9t-gateway-mcp  │ ...     │
├──────────────────────────────────────────────────────────────────┤
│                        API Layer (REST/RPC)                      │
├──────────────────────────────────────────────────────────────────┤
│  Jetty Server │ JAX-RS │ Bonaparte RPC Protocol │ Vert.x        │
└───────┬──────────────────────────────────────────────┬───────────┘
        │                                              │
┌───────▼──────────────────────────────────────────────▼───────────┐
│                     Business Logic Layer                         │
├──────────────────────────────────────────────────────────────────┤
│  Request Handlers (*-be modules)                                 │
│  ├─ Authentication & Authorization (t9t-auth-be)                 │
│  ├─ Document Processing (t9t-doc-be)                             │
│  ├─ I/O Operations (t9t-io-be)                                   │
│  ├─ AI Integration (t9t-ai-be)                                   │
│  ├─ Email (t9t-email-be)                                         │
│  ├─ Search (t9t-solr-be, t9t-hibernate-search-be)               │
│  └─ [30+ other functional modules]                               │
└───────┬──────────────────────────────────────────────────────────┘
        │
┌───────▼──────────────────────────────────────────────────────────┐
│                     Service Layer (SAPI)                         │
├──────────────────────────────────────────────────────────────────┤
│  Service Interfaces (*-sapi modules)                             │
│  Business logic contracts and plugin points                      │
└───────┬──────────────────────────────────────────────────────────┘
        │
┌───────▼──────────────────────────────────────────────────────────┐
│                     Persistence Layer                            │
├──────────────────────────────────────────────────────────────────┤
│  JPA Entities (*-jpa modules)                                    │
│  ├─ Hibernate Implementation (t9t-orm-hibernate-jpa)             │
│  ├─ EclipseLink Implementation (t9t-orm-eclipselink-jpa)         │
│  └─ Connection Pooling (C3P0, Hikari)                            │
└───────┬──────────────────────────────────────────────────────────┘
        │
┌───────▼──────────────────────────────────────────────────────────┐
│                   External Systems Layer                         │
├──────────────────────────────────────────────────────────────────┤
│  PostgreSQL │ Kafka │ SMTP │ AWS S3 │ LLMs │ Vector DBs │ ...   │
└──────────────────────────────────────────────────────────────────┘
```

### Technical Interfaces

#### 1. Database Interface

**Technology**: JPA 3.0+ with JDBC
**Protocol**: PostgreSQL wire protocol (port 5432)
**Data Format**: Relational tables with JSON/JSONB support

| Aspect | Details |
|--------|---------|
| **Primary Connection** | Write operations, transactions |
| **Shadow Connection** | Read-only queries, reporting |
| **Connection Pool** | C3P0 (primary), Hikari (shadow) |
| **Failover** | AWS Aurora wrapper with automatic failover |
| **Schema Management** | Flyway migrations via `t9t-sql-migration-executor` |

**Configuration**: `t9tconfig.xml` → `<databaseConfiguration>`

#### 2. REST API Gateway

**Technology**: JAX-RS (RESTEasy) on Jetty
**Protocol**: HTTP/HTTPS
**Data Format**: JSON (via Jackson), Bonaparte compact format

| Endpoint | Module | Purpose |
|----------|--------|---------|
| `/rpc` | t9t-*-vertx | Bonaparte RPC over HTTP (compact or JSON payloads) |
| `/api/*` | t9t-rest-sapi | REST operations (JSON or XML payloads) |
| `/sse` | t9t-gateway-mcp | MCP |
| `/mcp` | t9t-gateway-mcp | MCP |

**Security**: JWT Bearer tokens, API key authentication

#### 3. MCP Gateway Interface

**Technology**: MCP Java SDK on Jetty
**Protocol**: HTTP with Server-Sent Events (SSE)
**Data Format**: JSON-RPC 2.0 per MCP specification

**Module**: `t9t-gateway-mcp`
**Port**: 9094 (configurable)
**Authentication to backend**: API Key (`t9t.mcp.apiKey`)

See [MCP Gateway Documentation](../MCP-gateway.md) for details.

This gateway is not used in production, because it is based on the official SDK which does not support authentication, use the MCP services of the REST gateway instead.

#### 4. Web UI Interface

**Technology**: ZK Framework 9.6.x
**Protocol**: HTTP/HTTPS with WebSocket
**Module**: `t9t-zkui-*`

| Component | Purpose |
|-----------|---------|
| **t9t-zkui-ce** | Community Edition UI components |
| **t9t-zkui-ee** | Enterprise Edition UI features |
| **t9t-zkui-screens** | Screen definitions and templates |

**Port**: 8080 (default)

#### 5. Messaging Interface (Kafka)

**Technology**: Apache Kafka
**Protocol**: Kafka binary protocol
**Modules**: `t9t-kafka-be`, `t9t-cluster-kafka-api`, `t9t-msglog-kafka-sender`

**Usage**:
- Asynchronous message processing
- Cluster coordination
- Event streaming
- Message log distribution

#### 6. Email Interface

**Technology**: Jakarta Mail (SMTP)
**Protocol**: SMTP/SMTPS
**Module**: `t9t-email-be-smtp`, `t9t-email-be-vertx`

**Features**:
- Template-based email generation
- Attachment support
- HTML and plain text formats
- Queue-based sending

#### 7. Cloud Storage Interface (AWS)

**Technology**: AWS SDK for Java
**Services**: S3, Aurora, Parameter Store
**Modules**: `t9t-io-be-aws`, `t9t-media-resolver-aws`

| Service | Purpose |
|---------|---------|
| **S3** | File storage, document archival |
| **Aurora** | Database with automatic failover |
| **SSM Parameter Store** | Configuration management |

#### 8. AI/LLM Interfaces

**Modules**: `t9t-ai-*`, `t9t-openai-*`, `t9t-ollama-*`

| Provider | Protocol | Module |
|----------|----------|--------|
| **OpenAI** | REST (HTTPS) | t9t-openai-be |
| **Ollama** | REST (HTTP) | t9t-ollama-be |
| **Langchain** | Java Library | t9t-ai-be-langchain |

**Vector Database Support**:
- Pinecone (t9t-vdb-be-pinecone)
- Qdrant (t9t-vdb-be-qdrant)
- pgvector (t9t-vdb-jpa-pgvector)

#### 9. Search Interfaces

**Hibernate Search**:
- Backend: Lucene (t9t-hibernate-search-be-lucene)
- Backend: Elasticsearch (t9t-hibernate-search-be-elasticsearch)

**Apache Solr**:
- Module: t9t-solr-be
- Protocol: HTTP REST
- Use case: Advanced search, faceting

### Data Formats

| Format | Usage | Modules |
|--------|-------|---------|
| **Bonaparte** | Internal RPC, efficient binary serialization | All *-api modules |
| **JSON** | REST APIs, configuration | t9t-jackson, REST gateways |
| **XML** | Legacy APIs, configuration | t9t-xml-*, config files |
| **CSV** | Data import/export | t9t-io-be |
| **Excel** | Data export | t9t-io-be-poi |
| **PDF** | Document generation | t9t-doc-be* |
| **COBOL** | Microfocus COBOL format integration | t9t-io-be-mfcobol |

### Network Protocols

| Protocol | Port(s) | Purpose |
|----------|---------|---------|
| **HTTP/HTTPS** | 8024, 8080, 9094 | REST APIs, Web UI, MCP Gateway |
| **PostgreSQL** | 5432 | Database connections |
| **Kafka** | 9092, 9093 | Message streaming |
| **SMTP** | 25, 587, 465 | Email sending |
| **Hazelcast** | 5701 | Cluster communication |

### File System Interface

| Path Type | Environment Variable | Purpose |
|-----------|---------------------|---------|
| **Root** | `FORTYTWO` | Base path for all file operations |
| **Import** | (configured per sink) | Incoming data files |
| **Export** | (configured per sink) | Outgoing data files |
| **Temp** | System temp | Temporary processing files |

### Configuration Sources

Generic settings

| Source | Format | Priority | Usage |
|--------|--------|----------|-------|
| **System Properties** | -Dkey=value | Highest | JVM configuration |
| **Environment Variables** | KEY=value | Medium | Container deployments, secrets |
| **Properties Files** | .properties | Lowest | Application defaults |

**Configuration Resolution**: Higher priority sources override lower priority.


Additional sources of configuration

| Source | Format | Usage |
|--------|--------|-------|
| **XML Config** | t9tconfig.xml | Core server configuration |
| **Database** | Module config tables | Runtime configuration |


## External Dependencies

### Required External Systems

| System | Purpose | Criticality |
|--------|---------|-------------|
| **PostgreSQL Database** | Data persistence | Critical |
| **Network Connectivity** | Inter-service communication | Critical |

### Optional External Systems

| System | Purpose | Impact if Unavailable |
|--------|---------|----------------------|
| **Kafka Cluster** | Async messaging | Message processing delayed |
| **SMTP Server** | Email delivery | Emails queued or mocked |
| **AWS Services** | Cloud storage/computing | Fallback to local storage |
| **Elasticsearch** | Advanced search | Degraded search capabilities |
| **LLM Services** | AI features | AI features unavailable |
