# API Reference

## Common Parameters

All endpoints support these parameters:

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `bootstrapServers` | Query | No | Comma-separated list of Kafka brokers (default: from config) |

### Security Headers

| Header | Description | Example |
|--------|-------------|---------|
| `X-Kafka-Security-Protocol` | Security protocol | `SASL_PLAINTEXT`, `SSL`, `PLAINTEXT` |
| `X-Kafka-Username` | SASL username | `admin` |
| `X-Kafka-Password` | SASL password | `secret` |
| `X-Kafka-Sasl-Mechanism` | SASL mechanism | `PLAIN`, `SCRAM-SHA-256`, `SCRAM-SHA-512` |

## Topics API

### List Topics

```http
GET /api/v1/topics?bootstrapServers=broker1:9092,broker2:9092
```

**Response:**
```json
[
  {
    "name": "my-topic",
    "partitions": 3,
    "replicationFactor": 1,
    "configs": {
      "cleanup.policy": "delete"
    },
    "partitionsReplicas": [
      {
        "partitionId": 0,
        "replicas": [0, 1, 2],
        "isr": [0, 1, 2]
      }
    ]
  }
]
```

### Get Topic

```http
GET /api/v1/topics/{topicName}?bootstrapServers=broker1:9092
```

### Create Topic

```http
POST /api/v1/topics?bootstrapServers=broker1:9092
Content-Type: application/json

{
  "name": "my-topic",
  "partitions": 3,
  "replicationFactor": 1,
  "configs": {
    "cleanup.policy": "delete"
  }
}
```

### Update Topic Config

```http
PATCH /api/v1/topics/{topicName}?bootstrapServers=broker1:9092
Content-Type: application/json

{
  "name": "my-topic",
  "configs": {
    "retention.ms": "86400000"
  }
}
```

### Delete Topic

```http
DELETE /api/v1/topics/{topicName}?bootstrapServers=broker1:9092
```

---

## Users API

### List Users

```http
GET /api/v1/users?bootstrapServers=broker1:9092
```

**Response:**
```json
[
  {
    "username": "admin",
    "mechanisms": ["SCRAM-SHA-512", "SCRAM-SHA-256"]
  }
]
```

### Create User

```http
POST /api/v1/users?bootstrapServers=broker1:9092
Content-Type: application/json

{
  "username": "newuser",
  "password": "password123",
  "mechanism": "SCRAM-SHA-512"
}
```

### Delete User

```http
DELETE /api/v1/users/{username}?bootstrapServers=broker1:9092
```

---

## Quotas API

### List Quotas

```http
GET /api/v1/quotas?bootstrapServers=broker1:9092
```

**Response:**
```json
[
  {
    "entityType": "user",
    "entityName": "admin",
    "configs": {
      "producer_byte_rate": "1048576",
      "consumer_byte_rate": "1048576"
    }
  }
]
```

### Create/Alter Quota

```http
POST /api/v1/quotas?bootstrapServers=broker1:9092
Content-Type: application/json

{
  "entityType": "user",
  "user": "myuser",
  "configs": {
    "producer_byte_rate": "1048576"
  }
}
```

### Delete Quota

```http
DELETE /api/v1/quotas?entityType=user&entityName=myuser&bootstrapServers=broker1:9092
```

---

## ACLs API

### List ACLs

```http
GET /api/v1/acls?bootstrapServers=broker1:9092
```

**Response:**
```json
[
  {
    "resourceType": "TOPIC",
    "resourceName": "my-topic",
    "principal": "User:myuser",
    "host": "*",
    "operation": "READ",
    "permission": "ALLOW"
  }
]
```

### Grant Consumer ACL

Grants READ and DESCRIBE on topic, READ on group.

```http
POST /api/v1/acls/user/{username}/consumer?bootstrapServers=broker1:9092
Content-Type: application/json

{
  "topic": "my-topic",
  "group": "my-consumer-group"
}
```

### Grant Producer ACL

Grants WRITE and DESCRIBE on topic.

```http
POST /api/v1/acls/user/{username}/producer?bootstrapServers=broker1:9092
Content-Type: application/json

{
  "topic": "my-topic"
}
```

### Check Consumer ACL

```http
GET /api/v1/acls/user/{username}/consumer/check?topic=my-topic&group=my-group&bootstrapServers=broker1:9092
```

### Check Producer ACL

```http
GET /api/v1/acls/user/{username}/producer/check?topic=my-topic&bootstrapServers=broker1:9092
```

---

## Cluster Links API

### List Cluster Links

```http
GET /api/v1/cluster-links?bootstrapServers=broker1:9092
```

**Response:**
```json
[
  {
    "linkName": "my-link",
    "sourceClusterId": "source-cluster",
    "state": "ACTIVE",
    "configs": {}
  }
]
```

### Create Cluster Link

```http
POST /api/v1/cluster-links?bootstrapServers=broker1:9092
Content-Type: application/json

{
  "linkName": "my-link",
  "sourceBootstrapServers": "source-broker:9092",
  "configs": {
    "security.protocol": "SASL_PLAINTEXT"
  }
}
```

### Create Mirror Topics

```http
POST /api/v1/cluster-links/{linkName}/mirror-topics?bootstrapServers=broker1:9092
Content-Type: application/json

{
  "topics": ["topic1", "topic2"],
  "configs": {
    "replication.factor": "1"
  }
}
```

### Failover

```http
POST /api/v1/cluster-links/{linkName}/failover?bootstrapServers=broker1:9092
Content-Type: application/json

{
  "primaryClusterId": "source-cluster",
  "failoverMode": "leader"
}
```

---

## Error Responses

All errors return a standard error format:

```json
{
  "success": false,
  "message": "Error description"
}
```

### Common HTTP Status Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request (validation error) |
| 404 | Not Found (topic/user doesn't exist) |
| 409 | Conflict (topic already exists) |
| 500 | Internal Server Error |
