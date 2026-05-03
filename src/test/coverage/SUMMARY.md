# Test Coverage Summary

**Generated:** 2026-05-03  
**Total Tests:** 61  
**Tests Passed:** 61  
**Tests Failed:** 0  
**Tests Skipped:** 0  

## Overall Coverage

| Metric | Missed | Covered | Percentage |
|--------|--------|:-------:|------------|
| Instructions | 4,733 | 1,619 | 25.5% |
| Branches | 234 | 34 | 12.7% |
| Lines | 896 | 363 | 28.9% |
| Complexity | 276 | 63 | 18.6% |
| Methods | 166 | 51 | 23.5% |

## Coverage by Package

### Services (Best Coverage)

| Class | Instructions | Branches | Lines | Methods |
|-------|-------------|----------|-------|---------|
| ClusterService | 100% (181/181) | 87.5% (7/8) | 100% (39/39) | 100% (3/3) |
| TopicService | 71.0% (314/442) | 50.0% (9/18) | 72.4% (63/87) | 93.3% (12/13) |
| ConsumerService | 61.8% (308/498) | 41.2% (14/34) | 64.7% (55/85) | 83.3% (5/6) |
| ClusterLinkService | 19.0% (95/498) | 6.7% (2/30) | 18.9% (18/95) | 31.3% (5/16) |

### Uncovered Services

| Class | Instructions | Branches | Lines | Methods |
|-------|-------------|----------|-------|---------|
| AclService | 0% (0/647) | 0% (0/20) | 0% (0/110) | 0% (0/12) |
| UserService | 0% (0/285) | 0% (0/16) | 0% (0/60) | 0% (0/9) |
| QuotaService | 0% (0/381) | 0% (0/22) | 0% (0/75) | 0% (0/9) |
| MessageService | 0% (0/812) | 0% (0/62) | 0% (0/163) | 0% (0/16) |

### Controllers (No Coverage)

All controllers have **0% coverage**:
- AclController
- ClusterController
- ClusterLinkController
- ConsumerGroupController
- MessageController
- QuotaController
- TopicController
- UserController

### Other Components

| Class | Instructions | Branches | Lines | Methods |
|-------|-------------|----------|-------|---------|
| KafkaAdminClientFactory | 9.8% (17/174) | 0% (0/12) | 17.4% (8/46) | 11.1% (1/9) |
| KafkaAdminExceptionHandler | 0% (0/120) | 0% (0/8) | 0% (0/30) | 0% (0/11) |
| RequestContextExtractor | 0% (0/123) | 0% (0/18) | 0% (0/28) | 0% (0/4) |

## Key Findings

1. **ClusterService** has the best coverage at ~100%
2. **TopicService** and **ConsumerService** have moderate coverage (~60-70%)
3. **4 services** have zero test coverage (AclService, UserService, QuotaService, MessageService)
4. **All controllers** have zero test coverage
5. **Exception handler** has zero test coverage

## Recommendations

1. Add controller tests for all REST endpoints
2. Add service tests for uncovered services (AclService, UserService, QuotaService, MessageService)
3. Improve branch coverage in existing tests
4. Add tests for exception handling scenarios

---

*Full HTML report available at: `target/site/jacoco/index.html`*
