# Development Guide

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- Running Kafka cluster (3.0+ for full feature support)

## Setup

1. Clone the repository
2. Configure defaults in `application.yml` or use environment variables
3. Build: `./mvnw clean package`
4. Run: `./mvnw spring-boot:run`

## Configuration

### Application Properties

Edit `src/main/resources/application.yml`:

```yaml
server:
  port: 8080

kafka-admin:
  default-bootstrap-servers: localhost:9092
  default-security-protocol: SASL_PLAINTEXT
  default-sasl-mechanism: PLAIN
  # Default credentials (optional)
  # default-username: admin
  # default-password: secret
```

### Environment Variables

```bash
export KAFKA_ADMIN_DEFAULT_BOOTSTRAP_SERVERS="broker1:9092,broker2:9092"
export KAFKA_ADMIN_DEFAULT_SECURITY_PROTOCOL="SASL_PLAINTEXT"
export KAFKA_ADMIN_DEFAULT_USERNAME="admin"
export KAFKA_ADMIN_DEFAULT_PASSWORD="secret"
export KAFKA_ADMIN_DEFAULT_SASL_MECHANISM="PLAIN"
```

## Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=TopicServiceTest

# Run with verbose output
./mvnw test -X
```

## Adding New Endpoints

### Step 1: Create Request DTO

```java
// src/main/java/com/kafka/admin/model/request/MyFeatureRequest.java
package com.kafka.admin.model.request;

import jakarta.validation.constraints.NotBlank;

public class MyFeatureRequest {
    @NotBlank(message = "Name is required")
    private String name;
    
    // getters/setters
}
```

### Step 2: Create Response DTO

```java
// src/main/java/com/kafka/admin/model/response/MyFeatureResponse.java
package com.kafka.admin.model.response;

public class MyFeatureResponse {
    private String name;
    // getters/setters
}
```

### Step 3: Add Service Method

```java
// src/main/java/com/kafka/admin/service/ExistingService.java
public void myMethod(MyFeatureRequest request, 
        String bootstrapServers, String securityProtocol,
        String username, String password, String saslMechanism) {
    try (Admin admin = adminClientFactory.createAdminClient(
            bootstrapServers, securityProtocol, username, password, saslMechanism)) {
        // Implementation
    }
}
```

### Step 4: Add Controller Endpoint

```java
// src/main/java/com/kafka/admin/controller/ExistingController.java
@PostMapping
@Operation(summary = "My feature")
public ApiResponse myMethod(
        @Valid @RequestBody MyFeatureRequest request,
        @RequestParam(required = false) String bootstrapServers,
        HttpServletRequest httpRequest) throws Exception {
    
    var ctx = contextExtractor.extract(httpRequest);
    service.myMethod(request, ctx.bootstrapServers(), ctx.securityProtocol(),
            ctx.username(), ctx.password(), ctx.saslMechanism());
    return ApiResponse.success("Operation completed");
}
```

## Security Protocol Support

| Protocol | Authentication | Use Case |
|----------|---------------|----------|
| `PLAINTEXT` | None | Development |
| `SSL` | None + TLS | Production (internal) |
| `SASL_PLAINTEXT` | SASL | Development/Testing |
| `SASL_SSL` | SASL + TLS | Production |

### SASL Mechanisms

- `PLAIN` - Simple username/password (requires SSL in production)
- `SCRAM-SHA-256` - Salted challenge response
- `SCRAM-SHA-512` - Stronger SCRAM variant

## Common Issues

### Connection Timeout

Increase timeout in `application.yml`:
```yaml
kafka-admin:
  connection-timeout: 60000
```

### SSL/TLS Errors

Ensure truststore is configured in JVM args:
```bash
java -Djavax.net.ssl.trustStore=/path/to/truststore.jks \
     -Djavax.net.ssl.trustStorePassword=password \
     -jar target/kafka-admin-1.0.0-SNAPSHOT.jar
```

### Authorization Errors

Ensure the admin user has sufficient permissions:
- `DESCRIBE` on cluster
- `DESCRIBE` on topics
- `CREATE` on topics (for topic creation)
- `ALTER` on cluster (for ACLs)

## Code Style

- Use meaningful variable names
- Add JavaDoc for public APIs
- Use validation annotations on request DTOs
- Return appropriate HTTP status codes
- Log errors appropriately

## Building for Production

```bash
# Create production JAR
./mvnw clean package -Pprod

# Or build with custom profile
./mvnw clean package -DskipTests
```

Run with:
```bash
java -jar target/kafka-admin-1.0.0-SNAPSHOT.jar
```
