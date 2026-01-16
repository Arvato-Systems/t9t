# 6. Runtime View

## Request Processing Flow

### Scenario 1: User Authentication

This scenario shows the complete flow of a user login request.

```
┌─────────┐         ┌──────────┐         ┌────────────┐         ┌──────────┐          ┌────────────┐
│ Client  │         │  Jetty   │         │ t9t-server │         │  Auth    │          │ Database   │
│ (Browser)         │  Server  │         │            │         │  Handler │          │            │
└────┬────┘         └─────┬────┘         └──────┬─────┘         └────┬─────┘          └─────┬──────┘
     │                    │                     │                    │                      │
     │ 1. POST /rpc       │                     │                    │                      │
     │  LoginRequest      │                     │                    │                      │
     ├───────────────────>│                     │                    │                      │
     │                    │                     │                    │                      │
     │                    │ 2. Deserialize      │                    │                      │
     │                    │    Request          │                    │                      │
     │                    ├────────────────────>│                    │                      │
     │                    │                     │                    │                      │
     │                    │                     │ 3. Find handler    │                      │
     │                    │                     │    (LoginRequest)  │                      │
     │                    │                     ├───────────────────>│                      │
     │                    │                     │                    │                      │
     │                    │                     │                    │ 4. Query user        │
     │                    │                     │                    │    credentials       │
     │                    │                     │                    ├─────────────────────>│
     │                    │                     │                    │                      │
     │                    │                     │                    │ 5. User data         │
     │                    │                     │                    │<─────────────────────┤
     │                    │                     │                    │                      │
     │                    │                     │                    │ 6. Verify password   │
     │                    │                     │                    │    (bcrypt)          │
     │                    │                     │                    │                      │
     │                    │                     │                    │ 7. Query permissions │
     │                    │                     │                    ├─────────────────────>│
     │                    │                     │                    │                      │
     │                    │                     │                    │ 8. Permissions       │
     │                    │                     │                    │<─────────────────────┤
     │                    │                     │                    │                      │
     │                    │                     │                    │ 9. Generate JWT      │
     │                    │                     │                    │    (t9t-jwt)         │
     │                    │                     │                    │                      │
     │                    │                     │ 10. LoginResponse  │                      │
     │                    │                     │     (JWT token)    │                      │
     │                    │                     │<───────────────────┤                      │
     │                    │                     │                    │                      │
     │                    │ 11. Serialize       │                    │                      │
     │                    │     Response        │                    │                      │
     │                    │<────────────────────┤                    │                      │
     │                    │                     │                    │                      │
     │ 12. HTTP 200       │                     │                    │                      │
     │     + JWT token    │                     │                    │                      │
     │<───────────────────┤                     │                    │                      │
     │                    │                     │                    │                      │
```

**Steps**:
1. Client sends POST request with username/password
2. Jetty receives HTTP request, deserializes request, sends it in compact Bonaparte format to backend server
3. Request dispatcher finds appropriate handler (`LoginRequestHandler`)
4. Handler queries database for user record
5. Database returns user entity with hashed password
6. Handler verifies password
7. Handler queries user permissions and roles
8. Database returns permission data
9. Handler generates JWT token with user claims
10. Handler returns `LoginResponse` with JWT
11. Server serializes response to Bonaparte format
12. Client receives HTTP 200 with JWT token

**Key Classes**:
- `com.arvatosystems.t9t.auth.request.LoginRequest`
- `com.arvatosystems.t9t.auth.be.request.LoginRequestHandler`
- `com.arvatosystems.t9t.jwt.IJwtService`

## Scenario 2: CRUD Operation with Authorization

This shows a typical Create/Read/Update/Delete operation with authorization checks.

```
┌─────────┐    ┌────────┐    ┌──────────┐    ┌─────────┐    ┌─────────┐    ┌──────────┐
│ Client  │    │Gateway │    │ Request  │    │  Auth   │    │ Domain  │    │ Database │
│         │    │        │    │Dispatcher│    │ Checker │    │ Handler │    │          │
└────┬────┘    └───┬────┘    └────┬─────┘    └────┬────┘    └────┬────┘    └────┬─────┘
     │             │              │               │              │              │
     │ CrudRequest │              │               │              │              │
     │   + JWT     │              │               │              │              │
     ├────────────>│              │               │              │              │
     │             │              │               │              │              │
     │             │ Validate JWT │               │              │              │
     │             │ Extract user │               │              │              │
     │             │ context      │               │              │              │
     │             │              │               │              │              │
     │             │ Dispatch     │               │              │              │
     │             │ request      │               │              │              │
     │             ├─────────────>│               │              │              │
     │             │              │               │              │              │
     │             │              │Check          │              │              │
     │             │              │permissions    │              │              │
     │             │              ├──────────────>│              │              │
     │             │              │               │              │              │
     │             │              │ Authorized    │              │              │
     │             │              │<──────────────┤              │              │
     │             │              │               │              │              │
     │             │              │ Execute       │              │              │
     │             │              │ handler       │              │              │
     │             │              ├─────────────────────────────>│              │
     │             │              │               │              │              │
     │             │              │               │              │Begin         │
     │             │              │               │              │transaction   │
     │             │              │               │              ├─────────────>│
     │             │              │               │              │              │
     │             │              │               │              │Persist       │
     │             │              │               │              │entity        │
     │             │              │               │              ├─────────────>│
     │             │              │               │              │              │
     │             │              │               │              │Commit        │
     │             │              │               │              ├─────────────>│
     │             │              │               │              │              │
     │             │              │               │              │Success       │
     │             │              │               │              │<─────────────┤
     │             │              │               │              │              │
     │             │              │ Response      │              │              │
     │             │              │<─────────────────────────────┤              │
     │             │              │               │              │              │
     │             │ Response     │               │              │              │
     │             │<─────────────┤               │              │              │
     │             │              │               │              │              │
     │ Response    │              │               │              │              │
     │<────────────┤              │               │              │              │
     │             │              │               │              │              │
```

**Transaction Boundaries**:
- Start: Before handler execution
- Commit: After successful handler completion
- Rollback: On any exception

## Scenario 3: Document Generation and Email

This scenario demonstrates document generation with subsequent email delivery.

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│ Business │    │   Doc    │    │ Template │    │   PDF    │    │  Email   │    │   SMTP   │
│  Logic   │    │ Handler  │    │ Engine   │    │Generator │    │  Queue   │    │  Server  │
└────┬─────┘    └────┬─────┘    └────┬─────┘    └────┬─────┘    └────┬─────┘    └────┬─────┘
     │               │               │               │               │               │
     │CreateDocument │               │               │               │               │
     │Request        │               │               │               │               │
     ├──────────────>│               │               │               │               │
     │               │               │               │               │               │
     │               │Load template  │               │               │               │
     │               ├──────────────>│               │               │               │
     │               │               │               │               │               │
     │               │Template       │               │               │               │
     │               │<──────────────┤               │               │               │
     │               │               │               │               │               │
     │               │Merge data     │               │               │               │
     │               │with template  │               │               │               │
     │               ├──────────────>│               │               │               │
     │               │               │               │               │               │
     │               │HTML output    │               │               │               │
     │               │<──────────────┤               │               │               │
     │               │               │               │               │               │
     │               │Convert to PDF │               │               │               │
     │               ├──────────────────────────────>│               │               │
     │               │               │               │               │               │
     │               │PDF bytes      │               │               │               │
     │               │<──────────────────────────────┤               │               │
     │               │               │               │               │               │
     │               │Store/Archive  │               │               │               │
     │               │document       │               │               │               │
     │               │               │               │               │               │
     │               │Queue email    │               │               │               │
     │               │with attachment│               │               │               │
     │               ├──────────────────────────────────────────────>│               │
     │               │               │               │               │               │
     │Response       │               │               │               │               │
     │<──────────────┤               │               │               │               │
     │               │               │               │               │               │
     │               │               │               │               │ Send email    │
     │               │               │               │               │ (async)       │
     │               │               │               │               ├──────────────>│
     │               │               │               │               │               │
     │               │               │               │               │ Delivery      │
     │               │               │               │               │ confirmation  │
     │               │               │               │               │<──────────────┤
     │               │               │               │               │               │
```

**Async Processing**:
- Email sending happens asynchronously in a separate thread pool
- Retry logic handles temporary SMTP failures
- Email status tracked in database

**Key Modules**:
- `t9t-doc-be`: Document generation coordination
- `t9t-doc-be-pdf`: PDF rendering
- `t9t-email-be`: Email queue management
- `t9t-email-be-smtp`: SMTP delivery

## Scenario 4: Data Import Processing

File import with validation and error handling.

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│File      │    │   I/O    │    │ Format   │    │ Business │    │ Database │
│Upload    │    │ Handler  │    │ Parser   │    │ Validator│    │          │
└────┬─────┘    └────┬─────┘    └────┬─────┘    └────┬─────┘    └────┬─────┘
     │               │               │               │               │
     │ Upload CSV    │               │               │               │
     │ file          │               │               │               │
     ├──────────────>│               │               │               │
     │               │               │               │               │
     │               │ Detect format │               │               │
     │               │ (CSV)         │               │               │
     │               ├──────────────>│               │               │
     │               │               │               │               │
     │               │ Parse line 1  │               │               │
     │               ├──────────────>│               │               │
     │               │               │               │               │
     │               │ Row DTO       │               │               │
     │               │<──────────────┤               │               │
     │               │               │               │               │
     │               │ Validate      │               │               │
     │               ├──────────────────────────────>│               │
     │               │               │               │               │
     │               │ Valid         │               │               │
     │               │<──────────────────────────────┤               │
     │               │               │               │               │
     │               │ Save to DB    │               │               │
     │               ├──────────────────────────────────────────────>│
     │               │               │               │               │
     │               │ [Repeat for all rows...]      │               │
     │               │               │               │               │
     │               │ Parse line N  │               │               │
     │               ├──────────────>│               │               │
     │               │               │               │               │
     │               │ Row DTO       │               │               │
     │               │<──────────────┤               │               │
     │               │               │               │               │
     │               │ Validate      │               │               │
     │               ├──────────────────────────────>│               │
     │               │               │               │               │
     │               │ INVALID       │               │               │
     │               │ (error msg)   │               │               │
     │               │<──────────────────────────────┤               │
     │               │               │               │               │
     │               │ Log error     │               │               │
     │               │ Continue      │               │               │
     │               │               │               │               │
     │ Import        │               │               │               │
     │ summary       │               │               │               │
     │ (N success,   │               │               │               │
     │  M errors)    │               │               │               │
     │<──────────────┤               │               │               │
     │               │               │               │               │
```

**Error Handling**:
- Per-row validation
- Errors logged but processing continues
- Summary report with success/failure counts
- Failed records can be exported for correction

**Supported Formats**:
- CSV (configurable delimiter, encoding)
- Excel (XLS, XLSX)
- Fixed-width
- XML
- JSON
- COBOL
- custom

## Performance Considerations

### Connection Pooling

**Primary Database (Write)**:
- C3P0 pool
- Configurable min/max connections
- Connection validation on checkout

**Shadow Database (Read)**:
- Hikari pool
- Larger pool size for read-heavy workloads
- Used for reporting and search queries

### Caching Strategy

| Level | Technology | Use Case |
|-------|-----------|----------|
| **L1 Cache** | Hibernate session | Transaction-scoped entities |
| **L2 Cache** | Hibernate 2nd level | Rarely-changed entities |
| **Application Cache** | Custom/Caffeine | Configuration, translations |
| **Distributed Cache** | Hazelcast | Cluster-wide shared state |

### Async Processing

**Thread Pools**:
- **Worker Pool**: General business logic (configurable size)
- **Autonomous Pool**: Background tasks (configurable size)
- **I/O Pool**: File operations
- **Email Pool**: Email sending

**Vert.x Event Loop**:
- Non-blocking I/O
- Used in async modules (t9t-*-vertx)
- High concurrency with low resource usage

