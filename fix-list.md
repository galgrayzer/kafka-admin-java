# Needed Fixes - Progress
1. [x] Remove old fetch messages route
2. [x] Remove unused fields in topicOffsets response dto
3. [ ] Default bootstrap.servers should be auto used on swagger
4. [ ] Bootstrap.servers should be a required param on all routes
5. [ ] Default SASL mechanism not used
6. [ ] User deletion doesnt work because SASL mechanism is null
7. [x] Remove describe permissions from Consumer Group ACL on grant consumer
8. [x] Add Cluster Linking describe mirror route
9. [ ] Reset offset with live consumer instances reading from the requested topic should result with more indicative error message
10. [x] Error handler not returning all details (ex.getMessage() + ex.getCause().getMessage() should be returned)
11. [ ] Reset offset by time should be used with Kafka admin client operation not self implementation logic
12. [x] Reset offset by time DTO contains NotBlank on a long param resulting with validation error
13. [x] List all quotas route should be under /cluster endpoint
14. [x] Remove list all topics from topics endpoint
15. [x] Quota response should return the value in a string in bytes, without any `E`
16. [x] OpenAPI config should be without pre given servers
17. [ ] Need to add authenticate check route that check if a user can authenticate to the cluster with a given username and password and result with false if not 
        and true if it does and if the user is consumer or producer or both.
18. [x] **CRITICAL BUG** - `RequestContextExtractor` uses wrong header for bootstrapServers (uses `X-Kafka-Security-Protocol` instead of a bootstrap servers header)
19. [x] Remove unused `FailoverRequest` DTO (dead code - not referenced by any controller or service)
20. [x] Remove unused enums `AclOperationType` and `AclPermissionType` (services use Kafka's own classes directly)
21. [x] Add validation to `UpdateTopicConfigRequest` - `configs` map needs `@NotNull` and `@NotEmpty` to prevent NullPointerException
22. [x] Add validation to `ProduceMessagesRequest` - `records` list needs `@NotNull` and `@NotEmpty` to prevent NullPointerException
23. [x] Fix `QuotaService.getUserQuota()` returning `null` instead of throwing 404 when no quota found
24. [x] Fix `AclController.revokeProducerAcl` garbled `@Operation` annotation (summary and description are mangled together)
25. [x] Fix `RequestContextExtractor` to check environment variables as documented in priority chain (HTTP Headers > Query Params > Env Vars > Properties)
26. [x] Consolidate duplicate default value logic between `RequestContextExtractor` `@Value` annotations and `KafkaAdminConfig`
27. [ ] DRY up `KafkaAdminClientFactory` - `createAdminClient()` and `createProperties()` contain nearly identical security property building code
28. [ ] Handle unsupported security protocols in `KafkaAdminClientFactory` (currently silently ignores and uses Kafka defaults)
29. [ ] Fix `QuotaService.listQuotas` silently ignoring `client-id` entity quotas when both user and client-id entities exist
30. [x] Remove unused `serverPort` field from `OpenApiConfig` (dead code - read via `@Value` but never used)