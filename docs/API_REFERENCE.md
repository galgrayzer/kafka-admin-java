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

### Get User Quota

```http
GET /api/v1/quotas/user/{username}?bootstrapServers=broker1:9092
```

**Response:**
```json
{
  "entityType": "user",
  "entityName": "myuser",
  "configs": {
    "producer_byte_rate": "1048576",
    "consumer_byte_rate": "2097152"
  }
}
```

### Delete Quota

```http
DELETE /api/v1/quotas?username=myuser&bootstrapServers=broker1:9092
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

Grants DESCRIBE and READ on topic, DESCRIBE and READ on group.

```http
POST /api/v1/acls/user/{username}/consumer?bootstrapServers=broker1:9092
Content-Type: application/json

{
  "topic": "my-topic",
  "group": "my-consumer-group"
}
```

### Revoke Consumer ACL

```http
DELETE /api/v1/acls/user/{username}/consumer?bootstrapServers=broker1:9092
Content-Type: application/json

{
  "topic": "my-topic",
  "group": "my-consumer-group"
}
```

### Grant Producer ACL

Grants DESCRIBE, WRITE, and CREATE on topic.

```http
POST /api/v1/acls/user/{username}/producer?bootstrapServers=broker1:9092
Content-Type: application/json

{
  "topic": "my-topic"
}
```

### Revoke Producer ACL

```http
DELETE /api/v1/acls/user/{username}/producer?bootstrapServers=broker1:9092
Content-Type: application/json

{
  "topic": "my-topic"
}
```

### Check Consumer ACL

```http
GET /api/v1/acls/user/{username}/consumer/check?topic=my-topic&bootstrapServers=broker1:9092
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

## Users API

### List Users

```http
GET /api/v1/users?bootstrapServers=broker1:9092
```

### Create User

```http
POST /api/v1/users?bootstrapServers=broker1:9092
Content-Type: application/json

{
  "username": "myuser",
  "password": "secret",
  "mechanism": "SCRAM-SHA-512"
}
```

### Validate User

```http
GET /api/v1/users/{username}/validate?bootstrapServers=broker1:9092
```

### Delete User

```http
DELETE /api/v1/users/{username}?bootstrapServers=broker1:9092
```

---

## Consumer Groups API

### Get Consumer Offsets

```http
GET /api/v1/consumer-groups/{groupId}/offsets?topic=my-topic&bootstrapServers=broker1:9092
```

**Response:**
```json
[
  {
    "topic": "my-topic",
    "partition": 0,
    "currentOffset": 100
  }
]
```

### Reset Consumer Offsets

```http
POST /api/v1/consumer-groups/{groupId}/offsets/reset?bootstrapServers=broker1:9092
Content-Type: application/json

{
  "topic": "my-topic",
  "partition": 0,
  "resetStrategy": "earliest"
}
```

Valid reset strategies: `earliest`, `latest`, or specific offset. If partition is omitted, resets all partitions.

### Reset Consumer Offsets By Timestamp

```http
POST /api/v1/consumer-groups/{groupId}/offsets/reset-by-time?bootstrapServers=broker1:9092
Content-Type: application/json

{
  "topic": "my-topic",
  "partition": 0,
  "timestamp": 1704067200000
}
```

If partition is omitted, resets all partitions.

### Copy Consumer Offsets

```http
POST /api/v1/consumer-groups/{groupId}/offsets/copy?bootstrapServers=broker1:9092
Content-Type: application/json

{
  "sourceGroup": "source-group",
  "topic": "my-topic"
}
```

---

## Messages API

### Get Topic Offsets

```http
GET /api/v1/messages/topic/{topicName}/offsets?bootstrapServers=broker1:9092
```

**Response:**
```json
[
  {
    "topic": "my-topic",
    "partition": 0,
    "currentOffset": 100
  }
]
```

### Fetch Messages

```http
POST /api/v1/messages/fetch?bootstrapServers=broker1:9092
Content-Type: application/json

{
  "topic": "my-topic",
  "partition": 0,
  "startingPosition": "earliest",
  "maxMessages": 10
}
```

Options for `startingPosition`: `earliest`, `latest`, or specific `offset` / `timestamp`.

### Produce Messages

```http
POST /api/v1/messages/produce?bootstrapServers=broker1:9092
Content-Type: application/json

{
  "topic": "my-topic",
  "partition": 0,
  "records": [
    {"key": "key1", "value": "value1"},
    {"key": "key2", "value": "value2", "timestamp": 1234567890}
  ]
}
```

**Produce with Headers:**

```json
{
  "topic": "my-topic",
  "records": [
    {
      "key": "key1",
      "value": "value1",
      "headers": {
        "header1": "value1",
        "header2": "value2"
      }
    }
  ]
}
```

---

## Cluster API

### Get Cluster Metadata

```http
GET /api/v1/cluster/metadata?bootstrapServers=broker1:9092
```

**Response:**
```json
{
  "clusterId": "abc123",
  "brokers": [
    {"id": 0, "host": "localhost", "port": 9092, "rack": null}
  ],
  "topics": [
    {"name": "my-topic", "partitionCount": 3, "replicationFactor": 1, "isInternal": false}
  ]
}
```

### List Topic Names

```http
GET /api/v1/cluster/topics?bootstrapServers=broker1:9092
```

**Response:**
```json
["topic1", "topic2", "my-topic"]
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
