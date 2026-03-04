# Agent Guidelines for kafka-admin-java

## Project Overview

This is a Spring Boot 3.2.0 REST API for managing Kafka clusters using the Apache Kafka Admin Client. The project uses Java 17, Maven, and follows standard Spring Boot conventions.

## Build Commands

```bash
# Build the project
mvn clean package

# Run the application
mvn spring-boot:run

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=ClusterLinkServiceTest

# Run a specific test method
mvn test -Dtest=ClusterLinkServiceTest#testListClusterLinks

# Skip tests during build
mvn clean package -DskipTests

# Build with verbose output
mvn clean package -X
```

## Code Style Guidelines

### Project Structure

```
src/
├── main/java/com/kafka/admin/
│   ├── KafkaAdminApplication.java       # Main entry point
│   ├── client/                           # Kafka client factory
│   ├── config/                           # Spring configuration
│   ├── constants/                        # Application constants
│   ├── controller/                       # REST controllers
│   ├── exception/                        # Exception handlers
│   ├── model/
│   │   ├── request/                      # Request DTOs
│   │   ├── response/                     # Response DTOs
│   │   └── enums/                         # Enumerations
│   └── service/                          # Business logic
└── test/java/com/kafka/admin/            # Unit tests
```

### Naming Conventions

- **Packages**: lowercase, singular (e.g., `com.kafka.admin.service`)
- **Classes**: PascalCase (e.g., `TopicController`, `TopicService`)
- **Methods**: camelCase (e.g., `listTopics`, `createTopic`)
- **Variables**: camelCase (e.g., `topicName`, `bootstrapServers`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `DEFAULT_BOOTSTRAP_SERVERS`)
- **DTOs**: Suffix with `Request` or `Response` (e.g., `CreateTopicRequest`, `TopicResponse`)

### Imports

- Use explicit imports, avoid wildcard imports
- Order: java.* → jakarta.* → org.apache.* → org.springframework.* → com.kafka.admin.*
- Use `@Nullable` from `jakarta.annotation` for nullable parameters

### Formatting

- Use standard Java indentation (4 spaces)
- Opening brace on same line for classes/methods
- One blank line between imports and class declaration
- Maximum line length: 120 characters (recommended)

### Types

- Use Java 17 features: `var` for local variable type inference
- Use `List`, `Map`, `Set` from `java.util` instead of specific implementations in method signatures
- Use primitive wrappers (`Integer`, `Short`) when nullability is needed
- Use `String` for all string types

### Controller Patterns

```java
@RestController
@RequestMapping("/api/v1/resource")
@Tag(name = "Resource", description = "Resource operations")
public class ResourceController {

    private final ResourceService service;
    private final RequestContextExtractor contextExtractor;

    public ResourceController(ResourceService service, RequestContextExtractor contextExtractor) {
        this.service = service;
        this.contextExtractor = contextExtractor;
    }

    @GetMapping
    @Operation(summary = "List resources", description = "Get all resources")
    public List<ResourceResponse> list(
            @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {
        
        var ctx = contextExtractor.extract(request);
        return service.list(ctx.bootstrapServers(), ctx.securityProtocol(),
                ctx.username(), ctx.password(), ctx.saslMechanism());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create resource")
    public ApiResponse create(
            @Valid @RequestBody CreateResourceRequest createRequest,
            @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {
        
        var ctx = contextExtractor.extract(request);
        service.create(createRequest, ctx.bootstrapServers(), ctx.securityProtocol(),
                ctx.username(), ctx.password(), ctx.saslMechanism());
        return ApiResponse.success("Resource created successfully");
    }
}
```

### Service Patterns

```java
@Service
public class ResourceService {

    private final KafkaAdminClientFactory adminClientFactory;

    public ResourceService(KafkaAdminClientFactory adminClientFactory) {
        this.adminClientFactory = adminClientFactory;
    }

    public void createTopic(
            CreateTopicRequest request,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {

        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, username, password, saslMechanism)) {
            // Implementation
        }
    }
}
```

### Request DTO Patterns

```java
public class CreateTopicRequest {

    @NotBlank(message = "Topic name is required")
    private String name;

    @NotNull(message = "Partitions is required")
    @Min(value = 1, message = "At least 1 partition is required")
    private Integer partitions;

    @NotNull(message = "Replication factor is required")
    @Min(value = 1, message = "Replication factor must be at least 1")
    private Short replicationFactor;

    private Map<String, String> configs;

    // Getters and setters
}
```

### Response DTO Patterns

```java
public class TopicResponse {

    private String name;
    private Integer partitions;
    private Short replicationFactor;
    private List<PartitionReplica> partitionsReplicas;
    private Map<String, String> configs;

    // Getters and setters

    public static class PartitionReplica {
        private Integer partitionId;
        private List<Integer> replicas;
        private List<Integer> isr;
        // Getters and setters
    }
}
```

### Exception Handling

Use `@RestControllerAdvice` for global exception handling:

```java
@RestControllerAdvice
public class KafkaAdminExceptionHandler {

    @ExceptionHandler(TopicExistsException.class)
    public ResponseEntity<ApiResponse> handleTopicExistsException(TopicExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Topic already exists: " + ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Unexpected error: " + ex.getMessage()));
    }
}
```

### Testing

- Use JUnit 5 (`org.junit.jupiter.api`)
- Use static imports from `org.junit.jupiter.api.Assertions`:
  ```java
  import static org.junit.jupiter.api.Assertions.*;
  ```
- Test class naming: `<ClassName>Test` (e.g., `ClusterLinkServiceTest`)
- Test method naming: `test<MethodName><Scenario>` (e.g., `testListClusterLinks`)

```java
class ClusterLinkServiceTest {

    @Test
    void testListClusterLinks() {
        KafkaAdminClientFactory factory = mock(KafkaAdminClientFactory.class);
        when(factory.createAdminClient(any(), any(), any(), any(), any())).thenReturn(mock(Admin.class));
        ClusterLinkService service = new ClusterLinkService(factory);
        
        assertDoesNotThrow(() -> 
            service.listClusterLinks("localhost:9092", null, null, null, null)
        );
    }
}
```

### API Response Format

All endpoints return `ApiResponse`:

```java
public class ApiResponse {
    private boolean success;
    private String message;
    private Object data;

    public static ApiResponse success(String message) { ... }
    public static ApiResponse success(String message, Object data) { ... }
    public static ApiResponse error(String message) { ... }
}
```

### Configuration Priority

1. HTTP Headers (highest)
2. Query Parameters
3. Environment Variables
4. Application Properties (defaults)

### Kafka Connection Parameters

| Parameter | Description | Header/Env |
|-----------|-------------|------------|
| bootstrapServers | Kafka brokers | `bootstrapServers` / `KAFKA_ADMIN_DEFAULT_BOOTSTRAP_SERVERS` |
| securityProtocol | Security protocol | `X-Kafka-Security-Protocol` / `KAFKA_ADMIN_DEFAULT_SECURITY_PROTOCOL` |
| username | SASL username | `X-Kafka-Username` / `KAFKA_ADMIN_DEFAULT_USERNAME` |
| password | SASL password | `X-Kafka-Password` / `KAFKA_ADMIN_DEFAULT_PASSWORD` |
| saslMechanism | SASL mechanism | `X-Kafka-Sasl-Mechanism` / `KAFKA_ADMIN_DEFAULT_SASL_MECHANISM` |

### Security Protocols

- `PLAINTEXT` - No authentication (development)
- `SSL` - TLS without authentication
- `SASL_PLAINTEXT` - SASL authentication
- `SASL_SSL` - SASL with TLS

### SASL Mechanisms

- `PLAIN` - Simple username/password
- `SCRAM-SHA-256` - Salted challenge response
- `SCRAM-SHA-512` - Stronger SCRAM variant

### Adding New Endpoints

1. Create Request DTO in `src/main/java/com/kafka/admin/model/request/`
2. Create Response DTO in `src/main/java/com/kafka/admin/model/response/`
3. Add service method in appropriate service class
4. Add controller endpoint
5. Add exception handlers if needed
6. Write unit tests

### Swagger/OpenAPI

All endpoints should include OpenAPI annotations:
- `@Tag` on controller class
- `@Operation` on each endpoint
- `@Parameter` for request parameters
- Use `example` attribute for documentation
