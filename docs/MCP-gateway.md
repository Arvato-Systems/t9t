# 🚀 MCP Gateway for Model Context Protocol (MCP)

This document introduces the MCP Gateway developed as part of **TBE-1473**, detailing its architecture, setup, and technical impact on the `arvato-systems-jacs/t9t` repository. The gateway enables external tools to interact with t9t via the standardized Model Context Protocol (MCP), providing a secure, modular, and extensible integration point.

---

## 📋 Overview

The `t9t-gateway-mcp` is a standalone module designed as a JAX-RS/Jetty-based application. It leverages the MCP Java SDK to facilitate communication between external tools and the t9t backend, supporting dynamic tool and prompt registration as specified by the MCP protocol.

For a complete list of available MCP tools, see the [MCP Tools Reference](./MCP-tools.md).

### ✨ Key Features

- **🏗️ Standalone Gateway Module:** The gateway operates independently, allowing flexible deployment and configuration.
- **⚡ Jetty-based Server:** Utilizes an embedded Jetty server for HTTP communication.
- **🔌 MCP SDK Integration:** Directly connects to MCP via the Java SDK for protocol-specific logic.
- **🧩 Dynamic Tool and Prompt Support:** Enables extensibility for future MCP features.
- **🔐 API Key Authentication:** Enforces security through configurable API key authentication.
- **⚙️ Externalized Configuration:** All key parameters (port, API key, thread pool, etc.) are managed via property files.
- **🧱 Clean Separation of Components:** Implements `IT9tMcpProcessor` for backend communication, ensuring modularity.

---

## 🔍 Technical Summary & Commit Impact

### 📦 Commit TBE-1473: Summary

- **🆕 New Maven Module:** Introduced `t9t-gateway-mcp` with its own `pom.xml` and dependencies (t9t-core, MCP-SDK, Jetty, etc.).
- **🎯 Main Class:** `com.arvatosystems.t9t.mcp.gateway.McpJettyServer` serves as the entry point.
- **🔧 Initializer:** `T9tInitializer` manages configuration loading and tool registration.
- **🔗 Processor Interface:** `IT9tMcpProcessor` defines the contract for backend communication.
- **⚡ Implementation:** `impl/T9tMcpProcessor` processes MCP requests and interacts with the t9t backend.
- **⚙️ Configuration:** Uses property files (e.g., `t9t.mcp.properties`) for runtime settings.
- **📁 Artifact Name:** Finalized as `t9t-gateway-mcp.jar`.

### 📊 Technical Impact

- **🧩 Modularity:** The gateway is decoupled from the core system, allowing independent updates and deployments.
- **🔒 Security:** API key authentication is mandatory and configurable.
- **🔄 Extensibility:** Supports dynamic registration of tools and prompts.
- **⚙️ Configurability:** All operational parameters are externalized for easy adjustment.

---

## 🏗️ Code Structure

- **🎯 Main Class (`McpJettyServer`):** Starts the Jetty server and initializes MCP logic.
- **🔧 Initializer (`T9tInitializer`):** Loads configuration and registers tools with the MCP SDK.
- **🔗 Processor Interface (`IT9tMcpProcessor`):** Abstracts backend communication.
- **⚡ Implementation (`impl/T9tMcpProcessor`):** Handles MCP requests and backend interaction.
- **⚙️ Configuration:** Managed via property files, supporting both local and containerized deployments.

---

## ⚡ How the Gateway Works

1. **🚀 Startup:** Launch `McpJettyServer` (via JAR or Docker), which reads configuration from property files.
2. **⚙️ Server Initialization:** Jetty starts on the configured port, initializing the MCP SDK.
3. **🔌 Tool Registration:** Available tools and prompts are registered with the MCP SDK.
4. **📨 Request Handling:** Incoming MCP requests are authenticated using the API key and routed to the backend via `IT9tMcpProcessor`.
5. **📤 Response:** Backend results are returned to the client in MCP protocol format.

---

## 🛠️ Setup & Build Instructions

### 📋 Requirements

- ☕ Java 21+
- 📦 Maven 3.9+

### 🔨 Build

```sh
mvn clean install -pl t9t-gateway-mcp -am
```

This creates the executable JAR `t9t-gateway-mcp.jar` in `t9t-gateway-mcp/target/`.

### ⚙️ Configuration

Create a property file (e.g., `t9t.mcp.properties`):

```properties
t9t.mcp.apiKey=your-api-key-here
jetty.http.port=9094
# further optional settings
```

> **💡 Tip:** Ensure the file is on the classpath or referenced via JVM options.

---

## 🚀 Running the Gateway

### ☕ As a JAR

```sh
java -jar t9t-gateway-mcp.jar
```

To override the API key or other configuration properties at runtime, use JVM options:

```sh
java -Dt9t.mcp.apiKey=your-api-key-here -Djetty.http.port=9094 -jar t9t-gateway-mcp.jar
```

Or set environment variables before running:

```sh
export T9T_MCP_APIKEY=your-api-key-here
export JETTY_HTTP_PORT=9094
java -jar t9t-gateway-mcp.jar
```

By default, the server listens on port 9094. Adjust configuration as needed.

### 🐳 With Docker

1. **📝 Dockerfile:**
    Only a simple example:

    ```dockerfile
    FROM amazoncorretto:21-alpine-full
    WORKDIR /app
    COPY target/t9t-gateway-mcp.jar /app/
    ENV \
    HOST=main \
    PORT=8024 \
    MAIN_CLASS=com.arvatosystems.t9t.mcp.gateway.McpJettyServer \
    JETTY_HTTP_PORT=9094
    EXPOSE 9094
    ENTRYPOINT ["java", "-jar", "/app/t9t-gateway-mcp.jar"]
    ```

2. **🔨 Build image:**

    You will need Docker installed and running.
    You will need a local registry to push the image to, in default running on localhost:6000.

    The mvn build will additionally build all t9t base container images, which will be used in t9t based projects.

    To run the local registry:

    ```sh
    docker run -d -p 6000:5000 --restart=always --name registry  registry:2
    ```

    (Port 5000 is often sadly blocked by Bertelsmann internal services)

    For the actual build we use a multi-stage Dockerfile which can be used to reduce the final image size by separating the build environment from the runtime environment. It is available in the t9t-container folder of this repository.

    The image can be built using the following command:

   ```sh
   cd t9t-container
   mvn clean install
   ```

   It will copy the needed JAR and properties files into the Docker image. It will automatically create a container with the version of the project e.g. t9t-gateway-mcp:9.0-SNAPSHOT, which should help to keep the versions all aligned.

3. **▶️ Start container:**
    in the main directory of t9t, run:

    ```sh
    docker compose up -d mcp-gateway
    ```

    Or you can start the service direct in VS Code with start service button in docker-compose file.

    ```yml
   services:
    mcp-gateway:
        image: ${REGISTRY:-localhost:6000}/t9t-gateway-mcp:latest
        build:
        context: ./t9t-container
        dockerfile: Dockerfile
        target: t9t-gateway-mcp
        environment:
        # Backend connection
        HOST: host.docker.internal
        PORT: 8024
        # Gateway config
        JETTY_HTTP_PORT: 9094
        T9T_MCP_APIKEY: f5916def-b209-434d-ba8c-7e87ded2595a #your-api-key-here
        ports:
        - "9094:9094"
        networks:
        - t9t-network

    networks:
    t9t-network:
        driver: bridge
    ```

---

## 📝 Notes & Best Practices

- 🎯 The main entry points are `McpJettyServer` and `T9tInitializer` in the `t9t-gateway-mcp` module.
- 🔐 API key authentication is required for all requests.
- 🛡️ For production, manage properties and secrets securely.

---

## ⚙️ Configuration Properties

The MCP Gateway uses the following configuration properties (read via CONFIG_READER):

| Property                        | Type    | Default   | Description                                      |
|----------------------------------|---------|-----------|--------------------------------------------------|
| jetty.http.port                 | int     | 9094      | HTTP port for the Jetty server                   |
| jetty.threadPool.minThreads     | int     | 4         | Minimum number of threads in the thread pool     |
| jetty.threadPool.maxThreads     | int     | 20        | Maximum number of threads in the thread pool     |
| jetty.threadPool.idleTimeout    | int     | 5000      | Idle timeout for threads (ms)                    |
| jetty.connection.idleTimeout    | int     | 300000    | Idle timeout for HTTP connections (ms)           |
| jetty.stopTimeout               | int     | 5000      | Timeout for server shutdown (ms)                 |
| jetty.contextPath               | String  | "/"       | Context path for the Jetty server                |
| t9t.mcp.apiKey                  | String  | (none)    | API key required for authentication              |

### 🛠️ How to set these properties

**🖥️ In Eclipse (or any IDE):**

- 📁 Create a file named `t9t.mcp.properties` in the project directory or in the resources folder.
- ✏️ Add the properties in standard Java properties format, e.g.:

    ```properties
    t9t.mcp.apiKey=your-api-key-here
    jetty.http.port=9094
    ```

- 🌍 To set properties via environment variables, use uppercase and replace dots with underscores. For example, `t9t.mcp.apiKey` becomes `T9T_MCP_APIKEY`.
- ⚙️ In your IDE's run configuration, you can set environment variables like:

    ```sh
    T9T_MCP_APIKEY=your-api-key-here
    JETTY_HTTP_PORT=9094
    ```

- 🎯 Alternatively, you can set JVM arguments in the run configuration:

    ```sh
    -Dt9t.mcp.apiKey=your-api-key-here -Djetty.http.port=9094
    ```

**🐳 In Docker:**

- 🌍 Properties can be set as environment variables via `JAVA_TOOL_OPTIONS`, e.g.:

    ```sh
    docker run -e JAVA_TOOL_OPTIONS="-Dt9t.mcp.apiKey=your-api-key-here -Djetty.http.port=9094" ...
    ```

- 📁 Or mount a properties file into the image and ensure it is on the classpath:

    ```sh
    docker run -v $(pwd)/t9t.mcp.properties:/app/t9t.mcp.properties ...
    ```

> **⚡ Note:** JVM arguments (`-D...`) override values from the properties file.

---

## 🔗 Backend Communication: T9tMCPProcessor and IRemoteConnection

The backend communication between the MCP Gateway and the t9t backend is handled by the `T9tMCPProcessor` class, which implements the logic for processing incoming MCP requests and forwarding them to the backend service.

### 🚀 T9tMCPProcessor

- 🌉 Acts as the main bridge between the MCP protocol layer and the t9t backend.
- 🔄 Responsible for translating MCP requests into t9t service calls and returning the results in the expected format.
- 🔐 Handles authentication, request routing, and error handling.

### 🔌 Connection Setup with IRemoteConnection

- 🔗 The `T9tMCPProcessor` uses the `IRemoteConnection` interface to communicate with the t9t backend.
- 📋 `IRemoteConnection` defines methods for executing requests and authentication, both synchronously and asynchronously.
- ⚙️ The actual connection details (host, port, paths) are configured via environment variables or system properties:
  - `t9t.host`: Backend host
  - `t9t.port`: Backend port
  - `t9t.rpcpath`: Path for regular requests
  - `t9t.authpath`: Path for authentication

- 📦 An implementation of `IRemoteConnection` is selected by including the appropriate JAR in the classpath (e.g., HTTP client implementation).
- 🔒 Request serialization is always performed using the "compact bonaparte" format.

**📝 Example:**

When the MCP Gateway receives a request, `T9tMCPProcessor` authenticates (if needed) and forwards the request to the backend using an `IRemoteConnection` instance. The response is then mapped back to the MCP protocol and returned to the client.

---

## 💻 VS Code Integration: Example Configuration

To connect VS Code or compatible tools to the MCP Gateway, add the following configuration to your settings (e.g. in a `.mcp.json` or similar config file):

```json
{
    "t9t-mcp": {
        "url": "http://localhost:9094/sse",
        "type": "http"
    }
}
```

### 🛠️ How to add this configuration in VS Code

1. 📂 Open VS Code.
2. 📝 Create or open a file named `.mcp.json` (or use your tool's required config file) in your project root or workspace.
3. ✏️ Paste the above JSON snippet into the file and adjust the URL if your gateway runs on a different host or port.
4. 💾 Save the file.
5. ⚙️ Make sure your extension or tool is configured to use this file for MCP connections.

This enables VS Code or compatible clients to communicate with the t9t MCP Gateway via HTTP on the specified endpoint.

### 🐳 Run as docker container from vscode mcp.json config

Maybe later on, we should provide a config option to run the MCP Gateway as a Docker container directly from the VS Code configuration.

like this (not working yet):

```json
{
    "t9t-mcp": {
        "command": "docker",
        "args": [
            "run",
            "-i",
            "--rm",
            "-e",
            "HOST=host.docker.internal",
            "-e",
            "T9T_MCP_APIKEY=f5916def-b209-434d-ba8c-7e87ded2595a",
            "localhost:6000/t9t-gateway-mcp:9.0-SNAPSHOT"
        ],
        "type": "stdio"
    }
}

---

## 🔗 Related Documentation

- **[MCP Tools Reference](./MCP-tools.md)** - Complete list of available MCP tools with descriptions and usage examples
- [Model Context Protocol Specification](https://modelcontextprotocol.io/) - Official MCP specification
- t9t AI API Documentation - API definitions in `t9t-ai-api` module
```
