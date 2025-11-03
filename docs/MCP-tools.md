# 🛠️ MCP Tools Reference

This document provides a comprehensive list of all MCP (Model Context Protocol) tools available in the t9t framework. These tools are exposed via the MCP Gateway and can be called by AI agents and other MCP-compliant clients.

---

## 📋 Overview

The t9t framework exposes AI tools through the MCP Gateway, allowing external AI agents to interact with the backend system. Tools are automatically discovered and registered at startup based on implementations of the `IAiTool` interface.

### Tool Discovery

Tools are:
- Automatically discovered at startup by the `AiToolToolCollector`
- Registered in the `AiToolRegistry`
- Exposed via the MCP Gateway on port 9094 (default)
- Protected by permission checks (only tools with EXECUTE permission are exposed to users)

---

## 🔧 Available MCP Tools

### 1. AiToolCurrentDate

**Description:** Retrieves the current date, giving the day in field today and the weekday in nameOfDayOfTheWeek.

**Request Parameters:** None

**Response:**
- `today` (Day) - The current date
- `nameOfDayOfTheWeek` (String) - The name of the day of the week

**Implementation:** `com.arvatosystems.t9t.ai.tools.impl.AiToolToday`

**Use Case:** When an AI agent needs to know the current date or day of the week.

---

### 2. AiToolUserList

**Description:** Retrieves user data, for a given name, or all users.

**Request Parameters:**
- `name` (String, optional) - The name of the user, or part of it.

**Response:**
- `users` (List<UserDTO>) - List of matching users

**Implementation:** `com.arvatosystems.t9t.ai.tools.impl.AiToolUserSearch`

**Use Case:** Search for users in the system by name or retrieve all users.

---

### 3. AiToolSendEmail

**Description:** Sends an email to user, optionally with attachments.

**Request Parameters:**
- `subject` (String, optional) - The subject line of the email
- `emailText` (String, optional) - The message of the email
- `attachment` (MediaData, optional) - An optional file attachment of the email

**Response:**
- `message` (String) - Usually "OK" when successful

**Implementation:** `com.arvatosystems.t9t.ai.tools.impl.AiToolEmailSender`

**Use Case:** Send email notifications or communications from AI agents.

---

### 4. AiToolCreateQRCode

**Description:** Creates the QR code for the given text, as image in PNG format. By specification of QR codes, the text may be of maximum length 4296 characters.

**Request Parameters:**
- `text` (String, required) - The text to convert to QR code (max 4296 characters)
- `width` (Integer, optional) - The number of pixels height and width of the generated QR code. If not specified, 128 is used.

**Response:**
- `mediaData` (MediaData) - The generated QR code as PNG image

**Implementation:** `com.arvatosystems.t9t.ai.tools.doc.impl.AiToolQRCode`

**Use Case:** Generate QR codes for URLs, text data, or other content.

---

### 5. AiToolExplainErrorCode

**Description:** Retrieves the description of an error code and also its classification.

The classification is the most significant digit (scale 10^8), with the following meaning:

| Classification | Meaning |
|---------------|---------|
| 0 | Successful return |
| 1 | Operation rejected due to business reasons (credit limit exceeded, etc.) |
| 2 | Service parameter / XML parsing problem or alphanumeric data in numeric field or field size exceeded, etc. |
| 3 | Parameter error (for example currency or tenant referenced which does not exist or has been inactivated) |
| 4 | Timeout (external 3rd party service did not respond within allowed time) |
| 5 | Reserved |
| 6 | Reserved |
| 7 | Invalid parameter of an internal request or response (often a coding problem) |
| 8 | Internal logic error generated when internal plausibility checks fail (this is always a coding problem) |
| 9 | An uncaught general exception or some resource exhausted (for example disk full) |

**Request Parameters:**
- `errorCode` (Integer, required) - The error code

**Response:**
- `description` (String) - Description of the error
- `classification` (Integer, optional) - The classification of the error

**Implementation:** `com.arvatosystems.t9t.ai.tools.coding.impl.AiToolErrorCodes`

**Use Case:** Understand and explain error codes returned by the system.

---

### 6. AiToolExplainClass

**Description:** Provides information about a class. For a given class name, the purpose is returned, as well as a list of its fields. If the class is a subclass of another class, the name of its parent is also provided. If the class is request class, the name of its response class is also provided. For every field, the name, type and meaning is provided in the response. For alphanumeric fields, also the length is given. For numeric fields, the total number of digits (precision) and also the number of fractional digits is provided.

**Request Parameters:**
- `className` (String, required) - The simple name of the class

**Response:**
- `className` (String) - The simple name of the class
- `packageName` (String, optional) - The Java package name in which the class is located
- `parentClass` (String, optional) - The name of the parent class, in case the class is a subclass
- `responseClass` (String, optional) - The name of the response class, in case the class represents a request
- `description` (String, optional) - A high level description of the class
- `fields` (List<AiToolFieldDescription>) - The fields in this class

**Field Description Structure:**
- `name` (String) - The name of the field
- `type` (String) - The type of the field
- `length` (Integer, optional) - The field length
- `totalDigits` (Integer, optional) - The number of total digits
- `fractionalDigits` (Integer, optional) - The number of fractional digits
- `isSigned` (Boolean, optional) - If the number is a signed number
- `description` (String, optional) - The field description

**Implementation:** `com.arvatosystems.t9t.ai.tools.coding.impl.AiToolClassInformation`

**Use Case:** Understand the structure and purpose of classes in the t9t framework, useful for code exploration and understanding data models.

---

## 🔐 Security & Permissions

All MCP tools are protected by the t9t permission system:

- Tools are only exposed to users who have **EXECUTE** permission for the specific tool
- Permissions are checked using the `TOOL_CALL` permission type
- The tool name is used as the permission resource identifier

This ensures that users can only access tools they are authorized to use.

---

## 🎯 Tool Categories

Tools are organized into the following categories:

### 📅 General Tools
- **AiToolCurrentDate** - Date and time information

### 👥 User Management
- **AiToolUserList** - User search and retrieval

### 📧 Communication
- **AiToolSendEmail** - Email sending

### 📄 Document Processing
- **AiToolCreateQRCode** - QR code generation

### 💻 Development & Debugging
- **AiToolExplainErrorCode** - Error code explanation
- **AiToolExplainClass** - Class structure information

---

## 🔄 Tool Discovery Process

1. **Startup**: At application startup, `AiToolToolCollector` runs
2. **Discovery**: All `IAiTool` implementations are discovered via dependency injection
3. **Registration**: Each tool is registered in `AiToolRegistry` with its metadata
4. **Schema Generation**: JSON schemas are automatically generated from Bonaparte class definitions
5. **Exposure**: Tools are exposed via the MCP Gateway with proper authentication and permissions

---

## 📊 Tool Request/Response Flow

```
┌─────────────┐         ┌──────────────┐         ┌─────────────┐
│  AI Agent   │────────▶│  MCP Gateway │────────▶│  t9t Backend│
│  (Client)   │         │  (Port 9094) │         │             │
└─────────────┘         └──────────────┘         └─────────────┘
      │                        │                         │
      │  1. tools/list         │                         │
      │───────────────────────▶│                         │
      │  2. Tool list          │                         │
      │◀───────────────────────│                         │
      │                        │                         │
      │  3. tools/call         │  4. Execute tool        │
      │───────────────────────▶│────────────────────────▶│
      │                        │  5. Tool result         │
      │  6. Tool result        │◀────────────────────────│
      │◀───────────────────────│                         │
```

---

## 🧪 Testing Tools

Tools can be tested using:

1. **MCP-compatible clients** (e.g., VS Code with MCP extension)
2. **Direct HTTP requests** to the MCP Gateway
3. **Unit tests** in the `t9t-tests-*` modules
4. **Integration tests** against a running t9t instance

### Example: Testing with curl

```bash
# Get list of available tools
curl -X POST http://localhost:9094/mcp \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -d '{
    "jsonrpc": "2.0",
    "id": 1,
    "method": "tools/list"
  }'

# Call a tool
curl -X POST http://localhost:9094/mcp \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -d '{
    "jsonrpc": "2.0",
    "id": 2,
    "method": "tools/call",
    "params": {
      "name": "AiToolCurrentDate",
      "arguments": {}
    }
  }'
```

---

## 🔗 Related Documentation

- [MCP Gateway Setup](./MCP-gateway.md) - Complete setup and configuration guide
- [Model Context Protocol Specification](https://modelcontextprotocol.io/) - Official MCP specification
- t9t AI API Documentation - API definitions in `t9t-ai-api` module

---

## 🚀 Extending with Custom Tools

To add new MCP tools:

1. **Define the tool request/response** in a `.bon` file extending `AbstractAiTool` and `AbstractAiToolResult`
2. **Implement the tool** by creating a class that implements `IAiTool<RequestType, ResultType>`
3. **Annotate with `@Named`** using the request class PQON as the qualifier
4. **Add tool description** in the Bonaparte class comments
5. **Configure permissions** for the tool in the permission system
6. **Restart the application** - the tool will be automatically discovered and registered

Example:

```java
@Named("com.arvatosystems.t9t.ai.tools.MyNewTool")
public class MyNewToolImpl implements IAiTool<MyNewTool, MyNewToolResult> {
    @Override
    public MyNewToolResult performToolCall(RequestContext ctx, MyNewTool request) {
        // Implementation
        return result;
    }
}
```

---

## 📞 Support

For questions or issues related to MCP tools:
- Check the [MCP Gateway documentation](./MCP-gateway.md)
- Review tool implementations in `t9t-ai-be/src/main/java/com/arvatosystems/t9t/ai/tools/`
- Consult tool definitions in `t9t-ai-api/src/main/bon/dto/`
