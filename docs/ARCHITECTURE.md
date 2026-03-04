# Architecture Documentation

## Project Structure

```
src/main/java/com/kafka/admin/
├── KafkaAdminApplication.java          # Spring Boot entry point
├── constants/
│   └── AdminConstants.java              # Default values and header names
├── config/
│   ├── KafkaAdminConfig.java            # Configuration properties
│   └── OpenApiConfig.java               # Swagger/OpenAPI configuration
├── client/
│   └── KafkaAdminClientFactory.java     # Factory for creating Admin clients
├── controller/
│   ├── RequestContextExtractor.java      # Extracts security context from requests
│   ├── TopicController.java              # Topic CRUD endpoints
│   ├── UserController.java               # SCRAM user endpoints
│   ├── QuotaController.java              # Quota management endpoints
│   ├── AclController.java                # ACL management endpoints
│   ├── ClusterLinkController.java        # Cluster link endpoints
│   ├── ConsumerGroupController.java      # Consumer group operations
│   ├── MessageController.java            # Message fetch/produce
│   └── ClusterController.java            # Cluster metadata/topics
├── service/
│   ├── TopicService.java                 # Topic business logic
│   ├── UserService.java                  # User business logic
│   ├── QuotaService.java                # Quota business logic
│   ├── AclService.java                  # ACL business logic
│   ├── ClusterLinkService.java           # Cluster link business logic
│   ├── ConsumerService.java              # Consumer group operations
│   ├── MessageService.java              # Message fetch/produce
│   └── ClusterService.java              # Cluster metadata operations
├── model/
│   ├── request/                          # Request DTOs
│   ├── response/                        # Response DTOs
│   └── enums/                           # Enumerations
└── exception/
    └── KafkaAdminExceptionHandler.java   # Global exception handling
```

## Request Flow

1. **HTTP Request** arrives with:
   - Query parameter: `bootstrapServers`
   - Headers: Security credentials (`X-Kafka-*`)

2. **RequestContextExtractor** extracts:
   - Bootstrap servers (query param or default)
   - Security protocol (header or default)
   - Username/password (header or default)
   - SASL mechanism (header or default)

3. **Controller** receives the request and:
   - Extracts security context via `RequestContextExtractor`
   - Calls appropriate **Service** method

4. **Service** creates an **Admin** client using:
   - `KafkaAdminClientFactory` with extracted security context
   - Performs Kafka operations
   - Returns result or throws exception

5. **Exception Handler** catches errors and returns:
   - Appropriate HTTP status code
   - JSON error message

## Security Context Resolution

Priority order (highest to lowest):
1. **HTTP Header** - e.g., `X-Kafka-Username`
2. **Query Parameter** - e.g., `bootstrapServers`
3. **Environment Variable** - e.g., `KAFKA_ADMIN_DEFAULT_USERNAME`
4. **application.yml** - e.g., `kafka-admin.default-*-servers`
5. **Code Constant** - e.g., `AdminConstants.DEFAULT_BOOTSTRAP_SERVERS`

## Service Responsibilities

### TopicService
- Creates, lists, describes, updates, and deletes topics
- Uses `NewTopic`, `TopicDescription`, `ConfigResource`

### UserService
- Manages SCRAM users
- Uses `DescribeUserScramCredentials`, `alterUserScramCredentials`

### QuotaService
- Manages client/user quotas
- Uses `DescribeClientQuotas`, `alterClientQuotas`

### AclService
- Manages ACLs
- Creates consumer/producer convenience ACLs
- Uses `AclBinding`, `AclOperation`, `ResourcePattern`

### ClusterLinkService
- Manages cluster links and mirror topics
- Uses `NewClusterLink`, `describeClusterLinks`, `createClusterLinks`, `deleteClusterLinks`
- Mirror operations: `reverseAndStart`, `truncateAndRestore`, `failover`, `promote` (per-topic)

### ConsumerService
- Manages consumer group offsets
- Gets, resets, and copies consumer offsets
- Uses `listConsumerGroupOffsets`, `alterConsumerGroupOffsets`

### MessageService
- Fetches and produces messages
- Gets topic offsets
- Uses `KafkaConsumer` and `KafkaProducer`

### ClusterService
- Gets cluster metadata (brokers, topics)
- Lists topic names
- Uses `describeCluster`, `listTopics`

## Key Design Decisions

1. **AdminClient per request**: Each request creates a new AdminClient and closes it after use. This ensures thread safety and proper connection handling.

2. **DTO separation**: Request and response objects are separate for validation and flexibility.

3. **Nullable security params**: Security parameters (username, password) can be null if not using SASL authentication.

4. **Convenience ACL methods**: Separate endpoints for granting consumer/producer ACLs to simplify common operations.

## Adding New Features

To add a new feature:

1. Create **Request DTO** in `model/request/`
2. Create **Response DTO** in `model/response/`
3. Add methods to appropriate **Service**
4. Add endpoints to appropriate **Controller**
5. Add exception handling in **KafkaAdminExceptionHandler**
6. Update Swagger annotations in Controller

## Testing

Run tests with:
```bash
./mvnw test
```

Run with coverage:
```bash
./mvnw test jacoco:report
```
