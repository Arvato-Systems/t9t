# 7. Deployment View

## Infrastructure Overview

The t9t framework supports multiple deployment scenarios from development through production.

## Deployment Scenario 1: Docker Compose (Development)

### Overview

Local development setup using Docker Compose with all services containerized.

```
┌────────────────────────────────────────────────────────────────┐
│                    Developer Workstation                       │
├────────────────────────────────────────────────────────────────┤
│                                                                │
│  ┌────────────────────────────────────────────────────────┐    │
│  │                Docker Network (t9t-network)            │    │
│  │                                                        │    │
│  │  ┌────────────────┐      ┌────────────────┐            │    │
│  │  │  MCP Gateway   │      │  Server Main   │            │    │
│  │  │                │      │                │            │    │
│  │  │  Port: 9094    │      │  Port: 8024    │            │    │
│  │  │  Image: t9t-   │      │  Port: 8025    │            │    │
│  │  │  gateway-mcp   │      │  Port: 5701    │            │    │
│  │  │                │      │  (Hazelcast)   │            │    │
│  │  └────────┬───────┘      └────────┬───────┘            │    │
│  │           │                       │                    │    │
│  │           │                       │                    │    │
│  │  ┌────────▼───────────────────────▼───────────┐        │    │
│  │  │         PostgreSQL Database                │        │    │
│  │  │         Port: 5432                         │        │    │
│  │  │         Database: fortytwo                 │        │    │
│  │  │         Volume: postgres-data              │        │    │
│  │  └────────────────────────────────────────────┘        │    │
│  │                                                        │    │
│  │  ┌────────────────┐                                    │    │
│  │  │  Setup Main    │  (Init container)                  │    │
│  │  │  - DB migration│                                    │    │
│  │  │  - Schema init │                                    │    │
│  │  └────────────────┘                                    │    │
│  │                                                        │    │
│  └────────────────────────────────────────────────────────┘    │
│                                                                │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Host Port Mappings:                                     │  │
│  │  • localhost:5432  → PostgreSQL                          │  │
│  │  • localhost:8024  → t9t Server (RPC)                    │  │
│  │  • localhost:9094  → MCP Gateway                         │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                │
└────────────────────────────────────────────────────────────────┘
```

### Container Configuration

**docker-compose.yml** (located at repository root):

**Services**:

| Service | Image | Ports | Purpose |
|---------|-------|-------|---------|
| `mcp-gateway` | localhost:6000/t9t-gateway-mcp | 9094:9094 | MCP protocol gateway |
| `server-main` | localhost:6000/t9t-server-main | 8024:8024 | Main application server |
| `postgres` | postgres:latest (custom) | 5432:5432 | PostgreSQL database |
| `setup-main` | localhost:6000/t9t-setup-main | - | Database initialization |

**Environment Variables**:

```yaml
# MCP Gateway
HOST: host.docker.internal
PORT: 8024
JETTY_HTTP_PORT: 9094
T9T_MCP_APIKEY: f5916def-fake-key0-ba8c-7e87ded2595a

# Server Main
POSTGRES_MAIN_WRITE_URL: jdbc:postgresql://host.docker.internal:5432/fortytwo
POSTGRES_MAIN_WRITE_USER: fortytwo
POSTGRES_MAIN_WRITE_PASSWORD: changeMe
POSTGRES_MAIN_READ_URL: jdbc:postgresql://host.docker.internal:5432/fortytwo
POSTGRES_MAIN_READ_USER: fortytwo
POSTGRES_MAIN_READ_PASSWORD: changeMe
KEYSTORE_PASSWORD: BNpNotARealPasswordEXdMg

# PostgreSQL
POSTGRES_MULTIPLE_DATABASES: fortytwo
POSTGRES_PASSWORD: changeMe
POSTGRES_USER: fortytwo
```

### Volumes

- `postgres`: Persistent database storage

### Build Process

```bash
# Build containers
cd t9t-container
mvn clean install

# Start services
docker compose up -d

# View logs
docker compose logs -f server-main

# Stop services
docker compose down
```

## Deployment Scenario 2: Kubernetes (Production)

### Overview

Enterprise production deployment on Kubernetes with high availability.


```
┌────────────────────────────────────────────────────────────────────┐
│                        Kubernetes Cluster                          │
├────────────────────────────────────────────────────────────────────┤
│                                                                    │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │                    Ingress Controller                        │  │
│  │  • HTTPS termination                                         │  │
│  │  • Load balancing                                            │  │
│  │  • Path routing                                              │  │
│  └────────┬──────────────────────────┬──────────────────────────┘  │
│           │                          │                             │
│           ▼                          ▼                             │
│  ┌──────────────────┐      ┌──────────────────┐                    │
│  │  t9t-gateway     │      │  t9t-server      │                    │
│  │  Deployment      │      │  Deployment      │                    │
│  │  (ReplicaSet)    │      │  (ReplicaSet)    │                    │
│  │                  │      │                  │                    │
│  │  ┌────────────┐  │      │  ┌────────────┐  │                    │
│  │  │ Pod 1      │  │      │  │ Pod 1      │  │                    │
│  │  │ Container  │  │      │  │ Container  │  │                    │
│  │  └────────────┘  │      │  └────────────┘  │                    │
│  │                  │      │                  │                    │
│  │  ┌────────────┐  │      │  ┌────────────┐  │                    │
│  │  │ Pod 2      │  │      │  │ Pod 2      │  │                    │
│  │  │ Container  │  │      │  │ Container  │  │                    │
│  │  └────────────┘  │      │  └────────────┘  │                    │
│  │                  │      │                  │                    │
│  │  ┌────────────┐  │      │  ┌────────────┐  │                    │
│  │  │ Pod N      │  │      │  │ Pod N      │  │                    │
│  │  │ Container  │  │      │  │ Container  │  │                    │
│  │  └────────────┘  │      │  └────────────┘  │                    │
│  └──────────────────┘      └─────────┬────────┘                    │
│                                      │                             │
│                                      ▼                             │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │              External Services                               │  │
│  │                                                              │  │
│  │  ┌──────────────────┐    ┌──────────────────┐                │  │
│  │  │  AWS RDS         │    │  AWS S3          │                │  │
│  │  │  (PostgreSQL)    │    │  (File Storage)  │                │  │
│  │  │  - Primary       │    │                  │                │  │
│  │  │  - Read Replica  │    │                  │                │  │
│  │  └──────────────────┘    └──────────────────┘                │  │
│  │                                                              │  │
│  │  ┌──────────────────┐    ┌──────────────────┐                │  │
│  │  │  Kafka Cluster   │    │  Elasticsearch   │                │  │
│  │  │  (MSK)           │    │  Service         │                │  │
│  │  └──────────────────┘    └──────────────────┘                │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                    │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │              Configuration                                   │  │
│  │  • ConfigMaps: Application config                            │  │
│  │  • Secrets: Credentials, API keys, JWT keystore              │  │
│  │  • PersistentVolumes: Shared file storage                    │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                    │
└────────────────────────────────────────────────────────────────────┘
```

### Kubernetes Resources

**Deployments**:
- `t9t-server`: Main application server (3+ replicas)
- `t9t-gateway-rest`: REST API gateway (2+ replicas)
- `t9t-gateway-mcp`: MCP gateway (1+ replicas)
- `t9t-zkui`: Web UI application (2+ replicas)

**Services**:
- `t9t-server-service`: ClusterIP, internal only
- `t9t-gateway-service`: LoadBalancer or NodePort
- `t9t-zkui-service`: LoadBalancer for web access

**ConfigMaps**:
- `t9t-config`: Base application configuration
- `t9t-server-config`: Server-specific configuration (t9tconfig.xml)
- `logback-config`: Logging configuration

**Secrets**:
- `t9t-db-credentials`: Database username/password
- `t9t-jwt-keystore`: JWT signing keys
- `t9t-api-keys`: External API keys (OpenAI, AWS, etc.)

**PersistentVolumeClaims**:
- `t9t-shared-files`: Shared file storage for imports/exports

### Resource Requirements

**Per Pod**:

| Component | CPU Request | CPU Limit | Memory Request | Memory Limit |
|-----------|------------|-----------|----------------|--------------|
| t9t-server | 1 core | 4 cores | 2Gi | 8Gi |
| t9t-gateway | 0.5 core | 2 cores | 1Gi | 4Gi |
| t9t-zkui | 0.5 core | 2 cores | 1Gi | 4Gi |

### High Availability

**Strategies**:
1. **Multiple Replicas**: At least 3 server pods for redundancy
2. **Pod Anti-Affinity**: Spread pods across nodes
3. **Database Failover**: AWS Aurora automatic failover
4. **Health Checks**: Readiness and liveness probes
5. **Rolling Updates**: Zero-downtime deployments

**Health Check Endpoints**:
- `/health`: Basic health check
- `/ready`: Readiness probe (checks DB connectivity)

## Deployment Scenario 3: Standalone Server

### Overview

Single-server deployment for smaller installations or customer-managed infrastructure.

```
┌────────────────────────────────────────────────────────────┐
│                  Physical/Virtual Server                   │
├────────────────────────────────────────────────────────────┤
│                                                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │            Operating System (Linux)                  │  │
│  │                                                      │  │
│  │  ┌────────────────────────────────────────────────┐  │  │
│  │  │   Java Runtime (OpenJDK 21 or Corretto 21)     │  │  │
│  │  │                                                │  │  │
│  │  │  ┌──────────────────────────────────────────┐  │  │  │
│  │  │  │  t9t-server.jar                          │  │  │  │
│  │  │  │  • Main application                      │  │  │  │
│  │  │  │  • Embedded Jetty                        │  │  │  │
│  │  │  │  • All business modules                  │  │  │  │
│  │  │  └──────────────────────────────────────────┘  │  │  │
│  │  │                                                │  │  │
│  │  │  ┌──────────────────────────────────────────┐  │  │  │
│  │  │  │  t9t-gateway-mcp.jar (Optional)          │  │  │  │
│  │  │  │  • MCP Gateway                           │  │  │  │
│  │  │  └──────────────────────────────────────────┘  │  │  │
│  │  └────────────────────────────────────────────────┘  │  │
│  │                                                      │  │
│  │  ┌────────────────────────────────────────────────┐  │  │
│  │  │  Configuration Files                           │  │  │
│  │  │  • /etc/t9t/t9tconfig.xml                      │  │  │
│  │  │  • /etc/t9t/logback.xml                        │  │  │
│  │  │  • /etc/t9t/t9tkeystore.jceks                  │  │  │
│  │  └────────────────────────────────────────────────┘  │  │
│  │                                                      │  │
│  │  ┌────────────────────────────────────────────────┐  │  │
│  │  │  File System                                   │  │  │
│  │  │  • /var/t9t/import  (Import directory)         │  │  │
│  │  │  • /var/t9t/export  (Export directory)         │  │  │
│  │  │  • /var/t9t/archive (Archive)                  │  │  │
│  │  │  • /var/log/t9t     (Logs)                     │  │  │
│  │  └────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │         PostgreSQL Database (Local or Remote)        │  │
│  │  • Port 5432                                         │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

### Installation Steps

1. **Prerequisites**:
   ```bash
   # Install Java 21
   sudo apt update
   sudo apt install -y openjdk-21-jdk
   
   # Verify installation
   java -version
   ```

2. **Create User**:
   ```bash
   sudo useradd -m -s /bin/bash t9t
   sudo mkdir -p /var/t9t/{import,export,archive}
   sudo mkdir -p /var/log/t9t
   sudo mkdir -p /etc/t9t
   sudo chown -R t9t:t9t /var/t9t /var/log/t9t /etc/t9t
   ```

3. **Deploy Application**:
   ```bash
   # Copy JAR
   sudo cp t9t-server.jar /opt/t9t/
   
   # Copy configuration
   sudo cp t9tconfig.xml /etc/t9t/
   sudo cp logback.xml /etc/t9t/
   sudo cp t9tkeystore.jceks /etc/t9t/
   
   # Set permissions
   sudo chown t9t:t9t /opt/t9t/t9t-server.jar
   sudo chmod 640 /etc/t9t/t9tkeystore.jceks
   ```

4. **Systemd Service**:
   ```ini
   # /etc/systemd/system/t9t-server.service
   [Unit]
   Description=t9t Enterprise Server
   After=network.target postgresql.service
   
   [Service]
   Type=simple
   User=t9t
   WorkingDirectory=/opt/t9t
   ExecStart=/usr/bin/java -Xms2g -Xmx8g \
     -XX:+UseG1GC \
     -Dlogback.configurationFile=/etc/t9t/logback.xml \
     -jar /opt/t9t/t9t-server.jar
   Restart=on-failure
   RestartSec=10
   
   [Install]
   WantedBy=multi-user.target
   ```

5. **Start Service**:
   ```bash
   sudo systemctl daemon-reload
   sudo systemctl enable t9t-server
   sudo systemctl start t9t-server
   sudo systemctl status t9t-server
   ```

### Monitoring

**Log Files**:
- `/var/log/t9t/application.log` - Main application log
- `/var/log/t9t/error.log` - Error log
- `/var/log/t9t/access.log` - Access log

**JMX Monitoring**:
- Enable JMX: `-Dcom.sun.management.jmxremote.port=9010`
- Connect with JConsole or VisualVM

## Infrastructure Components

### Database

**PostgreSQL Configuration**:

| Setting | Production Value | Notes |
|---------|------------------|-------|
| `max_connections` | 200 | Based on connection pool sizes |
| `shared_buffers` | 25% of RAM | Database buffer cache |
| `effective_cache_size` | 50% of RAM | Query planner hint |
| `work_mem` | 16MB | Per-operation memory |
| `maintenance_work_mem` | 512MB | For VACUUM, CREATE INDEX |
| `checkpoint_completion_target` | 0.9 | Spread out checkpoint writes |
| `wal_buffers` | 16MB | Write-ahead log buffer |
| `random_page_cost` | 1.1 | SSD-optimized |

**AWS Aurora Specific**:
- Use Aurora PostgreSQL-compatible edition
- Enable automatic failover
- Configure reader endpoints for shadow database
- Use parameter groups for tuning

### File Storage

**Local Deployment**:
- File system paths defined by `FORTYTWO` environment variable
- Separate directories for import, export, archive

**Cloud Deployment**:
- AWS S3 for file storage (via `t9t-io-be-aws`)
- Mount S3 via S3FS or use SDK directly
- Consider lifecycle policies for archival

### Message Broker

**Kafka Configuration**:

| Component | Recommendation | Notes |
|-----------|----------------|-------|
| **Cluster Size** | 3+ brokers | High availability |
| **Replication Factor** | 3 | Data durability |
| **Partitions** | 10-50 per topic | Parallelism |
| **Retention** | 7 days | Configurable per topic |

**AWS MSK**:
- Managed Kafka service
- Automatic patching and monitoring
- Integrated with CloudWatch

### Load Balancer

**Requirements**:
- Session affinity not required (stateless)
- Health check endpoint: `/health`
- HTTPS termination
- WebSocket support for ZK UI

**Options**:
- AWS ALB (Application Load Balancer)
- NGINX
- Kubernetes Ingress
- HAProxy

## Network Architecture

### Ports

| Port | Service | Protocol | Access |
|------|---------|----------|--------|
| 8024 | Main RPC endpoint | HTTP/HTTPS | Internal |
| 8025 | Admin endpoint | HTTP/HTTPS | Internal |
| 8080 | ZK UI | HTTP/HTTPS | External |
| 9094 | MCP Gateway | HTTP/HTTPS (SSE) | External |
| 5432 | PostgreSQL | PostgreSQL | Internal |
| 9092 | Kafka | Kafka binary | Internal |
| 5701 | Hazelcast | TCP | Internal (cluster) |

### Security Groups / Firewall

**Application Server**:
- Inbound: 8024, 8025, 8080, 9094 from load balancer
- Inbound: 5701 from other application servers (cluster)
- Outbound: 5432 to database
- Outbound: 9092 to Kafka
- Outbound: 443 to external APIs (LLMs, AWS, etc.)

**Database Server**:
- Inbound: 5432 from application servers only
- No outbound restrictions needed

## Deployment Best Practices

### Configuration Management

1. **Environment Variables**: Use for environment-specific values
   - Database URLs, credentials
   - API keys
   - Service endpoints

2. **ConfigMaps/Secrets** (Kubernetes): For sensitive data
   - Mounted as files or environment variables
   - Automatic updates with pod restarts

3. **Version Control**: Store non-sensitive config in Git
   - Base configurations
   - Deployment manifests
   - Helm charts

### Zero-Downtime Deployments

1. **Rolling Updates**: Update pods gradually
2. **Health Checks**: Only route traffic to healthy pods
3. **Database Migrations**: Run before application update
4. **Backward Compatible APIs**: During transition periods

### Backup and Recovery

**Database Backups**:
- Automated daily backups
- Point-in-time recovery capability
- Test restoration regularly

**Configuration Backups**:
- Version control for all config files
- Backup keystores securely

**Application State**:
- Stateless design minimizes backup needs
- File storage backed up separately

### Monitoring and Alerting

**Key Metrics**:
- Request throughput and latency
- Database connection pool usage
- JVM heap usage and GC behavior
- Thread pool sizes
- Error rates

**Alerting Thresholds**:
- Response time > 5 seconds
- Error rate > 5%
- Heap usage > 80%
- Database connections > 90% of pool
- Disk space < 20%

**Tools**:
- Prometheus + Grafana
- AWS CloudWatch
- ELK Stack for log aggregation
- JMX exporters

