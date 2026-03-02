# Kafka Admin Java

A REST API for managing Kafka cluster operations using the Apache Kafka Admin Client.

## Features

- **Topics**: Create, list, describe, update, and delete Kafka topics
- **Users**: Manage SCRAM users (create, list, delete)
- **Quotas**: Manage client/user quotas
- **ACLs**: Manage access control lists including convenient producer/consumer ACL helpers
- **Cluster Linking**: Manage cluster links and mirror topics (Kafka 3.0+)

## Requirements

- Java 17+
- Kafka 3.0+ (for cluster linking features)

## Quick Start

```bash
# Build the project
./mvnw clean package

# Run the application
./mvnw spring-boot:run

# Or run with custom configuration
java -jar target/kafka-admin-1.0.0-SNAPSHOT.jar \
  --kafka-admin.default-bootstrap-servers=broker1:9092,broker2:9092
```

## API Documentation

Once running, visit:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Configuration

Configuration priority (highest to lowest):
1. HTTP Headers
2. Query Parameters
3. Environment Variables
4. Application Properties (defaults)
5. Code Constants

### Environment Variables

| Variable | Description | Default          |
|----------|-------------|------------------|
| `KAFKA_ADMIN_DEFAULT_BOOTSTRAP_SERVERS` | Default bootstrap servers | `localhost:9092` |
| `KAFKA_ADMIN_DEFAULT_SECURITY_PROTOCOL` | Default security protocol | `SASL_PLAINTEXT` |
| `KAFKA_ADMIN_DEFAULT_USERNAME` | Default admin username | -                |
| `KAFKA_ADMIN_DEFAULT_PASSWORD` | Default admin password | -                |
| `KAFKA_ADMIN_DEFAULT_SASL_MECHANISM` | Default SASL mechanism | `SCRAM-SHA-256`  |

### HTTP Headers

| Header | Description |
|--------|-------------|
| `X-Kafka-Security-Protocol` | Security protocol (e.g., `SASL_PLAINTEXT`, `SSL`) |
| `X-Kafka-Username` | Username for SASL authentication |
| `X-Kafka-Password` | Password for SASL authentication |
| `X-Kafka-Sasl-Mechanism` | SASL mechanism (e.g., `PLAIN`, `SCRAM-SHA-256`) |

### Query Parameters

All endpoints accept a `bootstrapServers` query parameter to override the default bootstrap servers.

Example:
```
GET /api/v1/topics?bootstrapServers=broker1:9092,broker2:9092
```

## Endpoints

### Topics

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/topics` | List all topics |
| GET | `/api/v1/topics/{name}` | Get topic details |
| POST | `/api/v1/topics` | Create a new topic |
| PATCH | `/api/v1/topics/{name}` | Update topic configuration |
| DELETE | `/api/v1/topics/{name}` | Delete a topic |

### Users (SCRAM)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/users` | List all SCRAM users |
| POST | `/api/v1/users` | Create a new SCRAM user |
| DELETE | `/api/v1/users/{username}` | Delete a SCRAM user |

### Quotas

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/quotas` | List all quotas |
| POST | `/api/v1/quotas` | Create or alter a quota |
| DELETE | `/api/v1/quotas?entityType=...&entityName=...` | Delete a quota |

### ACLs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/acls` | List all ACLs |
| POST | `/api/v1/acls` | Create an ACL |
| DELETE | `/api/v1/acls` | Delete an ACL |
| POST | `/api/v1/acls/user/{username}/consumer` | Grant consumer ACLs |
| POST | `/api/v1/acls/user/{username}/producer` | Grant producer ACLs |
| GET | `/api/v1/acls/user/{username}/consumer/check` | Check consumer ACLs |
| GET | `/api/v1/acls/user/{username}/producer/check` | Check producer ACLs |

### Cluster Links

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/cluster-links` | List all cluster links |
| POST | `/api/v1/cluster-links` | Create a cluster link |
| DELETE | `/api/v1/cluster-links/{linkName}` | Delete a cluster link |
| POST | `/api/v1/cluster-links/{linkName}/mirror-topics` | Create mirror topics |
| POST | `/api/v1/cluster-links/{linkName}/reverse-and-start` | Reverse and start replication |
| POST | `/api/v1/cluster-links/{linkName}/truncate-and-restore` | Truncate and restore |
| POST | `/api/v1/cluster-links/{linkName}/failover` | Failover to mirror cluster |
| POST | `/api/v1/cluster-links/{linkName}/promote` | Promote mirror to primary |

## Example Requests

### Create Topic

```bash
curl -X POST "http://localhost:8080/api/v1/topics?bootstrapServers=broker1:9092" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "my-topic",
    "partitions": 3,
    "replicationFactor": 1,
    "configs": {
      "cleanup.policy": "delete"
    }
  }'
```

### Grant Consumer ACL

```bash
curl -X POST "http://localhost:8080/api/v1/acls/user/myuser/consumer?bootstrapServers=broker1:9092" \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "my-topic",
    "group": "my-consumer-group"
  }'
```

### Grant Producer ACL

```bash
curl -X POST "http://localhost:8080/api/v1/acls/user/myuser/producer?bootstrapServers=broker1:9092" \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "my-topic"
  }'
```

## Security

By default, the API uses SASL_PLAINTEXT with the PLAIN mechanism. Override credentials via headers:

```bash
curl -X GET "http://localhost:8080/api/v1/topics" \
  -H "X-Kafka-Username: admin" \
  -H "X-Kafka-Password: secret"
```

## License

Apache License 2.0
