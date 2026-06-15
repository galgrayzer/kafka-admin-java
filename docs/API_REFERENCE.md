# API Reference

## Common Parameters

All endpoints support these parameters:

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `bootstrapServers` | Query | Yes | Comma-separated list of Kafka brokers |

### Security Headers

| Header | Description | Example |
|--------|-------------|---------|
| `X-Kafka-Security-Protocol` | Security protocol | `SASL_PLAINTEXT`, `SSL`, `PLAINTEXT` |
| `X-Kafka-Username` | SASL username | `admin` |
| `X-Kafka-Password` | SASL password | `secret` |
| `X-Kafka-Sasl-Mechanism` | SASL mechanism | `PLAIN`, `SCRAM-SHA-256`, `SCRAM-SHA-512` |

## Topics API

### Get Topic

```http
GET /api/v1/topics/{topicName}?bootstrapServers=broker1:9092
```

**Response:**
```json
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

### Get Topic Partition Offsets

```http
GET /api/v1/topics/{topicName}/offsets?bootstrapServers=broker1:9092
```

**Response:**
```json
[
  {
    "topic": "my-topic",
    "partition": 0,
    "beginningOffset": 0,
    "endOffset": 100
  }
]
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

### Validate User

```http
GET /api/v1/users/{username}/validate?bootstrapServers=broker1:9092
```

### Check Authentication

```http
POST /api/v1/users/authenticate?username=myuser&password=secret&topic=my-topic&bootstrapServers=broker1:9092
```

**Response:**
```json
{
  "success": true,
  "message": "Authentication check completed",
  "data": {
    "authenticated": true,
    "role": "consumer"
  }
}
```

---

## Quotas API

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
DELETE /api/v1/quotas?username=myuser&bootstrapServers=broker1:9092
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

### Create ACL

```http
POST /api/v1/acls?bootstrapServers=broker1:9092
Content-Type: application/json

{
  "resourceType": "TOPIC",
  "resourceName": "my-topic",
  "principal": "User:myuser",
  "host": "*",
  "operation": "READ",
  "permission": "ALLOW"
}
```

### Delete ACL

```http
DELETE /api/v1/acls?resourceType=TOPIC&resourceName=my-topic&principal=User:myuser&host=*&operation=READ&permission=ALLOW&bootstrapServers=broker1:9092
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

Grants DESCRIBE and WRITE on topic.

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

### Describe Mirror Topics

```http
GET /api/v1/cluster-links/{linkName}/mirror-topics?bootstrapServers=broker1:9092
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

### Delete Cluster Link

```http
DELETE /api/v1/cluster-links/{linkName}?bootstrapServers=broker1:9092
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

### Reverse and Start

```http
POST /api/v1/cluster-links/{linkName}/topics/{topicName}/reverse-and-start?bootstrapServers=broker1:9092
```

### Truncate and Restore

```http
POST /api/v1/cluster-links/{linkName}/topics/{topicName}/truncate-and-restore?bootstrapServers=broker1:9092
```

### Failover

```http
POST /api/v1/cluster-links/{linkName}/topics/{topicName}/failover?bootstrapServers=broker1:9092
```

### Promote

```http
POST /api/v1/cluster-links/{linkName}/topics/{topicName}/promote?bootstrapServers=broker1:9092
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

### Update Topic Partition Offsets

```http
POST /api/v1/consumer-groups/{topicName}/offsets/batch-update?bootstrapServers=broker1:9092
Content-Type: application/json

{
  "groupId": "my-group",
  "partitionOffsets": [
    {"partition": 0, "offset": 100},
    {"partition": 1, "offset": 200}
  ]
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

### Fetch Earliest Messages

```http
GET /api/v1/messages/topic/{topicName}/earliest?partition=0&maxMessages=100&bootstrapServers=broker1:9092
```

### Fetch Latest Messages

```http
GET /api/v1/messages/topic/{topicName}/latest?partition=0&maxMessages=100&bootstrapServers=broker1:9092
```

### Fetch Messages by Timestamp

```http
GET /api/v1/messages/topic/{topicName}/by-timestamp?partition=0&timestamp=1704067200000&maxMessages=100&bootstrapServers=broker1:9092
```

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

### List Quotas

```http
GET /api/v1/cluster/quotas?bootstrapServers=broker1:9092
```

**Response:**
```json
[
  {
    "entityType": "user",
    "entityName": "admin",
    "configs": {
      "producer_byte_rate": "1048576"
    }
  }
]
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
