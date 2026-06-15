package com.kafka.admin.service;

import com.kafka.admin.client.KafkaAdminClientFactory;
import com.kafka.admin.model.request.CreateQuotaRequest;
import com.kafka.admin.model.response.QuotaResponse;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.quota.ClientQuotaAlteration;
import org.apache.kafka.common.quota.ClientQuotaEntity;
import org.apache.kafka.common.quota.ClientQuotaFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class QuotaServiceTest {

    @Mock
    private KafkaAdminClientFactory adminClientFactory;

    @Mock
    private Admin admin;

    @Mock
    private DescribeClientQuotasResult describeClientQuotasResult;

    @Mock
    private AlterClientQuotasResult alterClientQuotasResult;

    @Mock
    private KafkaFuture<Map<ClientQuotaEntity, Map<String, Double>>> describeQuotasFuture;

    @Mock
    private KafkaFuture<Void> alterQuotasFuture;

    private QuotaService quotaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        quotaService = new QuotaService(adminClientFactory);
        when(adminClientFactory.createAdminClient(anyString(), any(), any(), any(), any())).thenReturn(admin);
    }

    @Test
    void testListQuotasUserOnly() throws Exception {
        // Given
        Map<ClientQuotaEntity, Map<String, Double>> quotas = createMockUserQuotas();

        when(admin.describeClientQuotas(ClientQuotaFilter.all())).thenReturn(describeClientQuotasResult);
        when(describeClientQuotasResult.entities()).thenReturn(describeQuotasFuture);
        when(describeQuotasFuture.get()).thenReturn(quotas);

        // When
        List<QuotaResponse> result = quotaService.listQuotas("localhost:9092", null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        QuotaResponse userQuota = result.get(0);
        assertEquals("user", userQuota.getEntityType());
        assertEquals("test-user", userQuota.getEntityName());
        assertNotNull(userQuota.getConfigs());

        verify(admin).close();
    }

    @Test
    void testListQuotasEmpty() throws Exception {
        // Given
        Map<ClientQuotaEntity, Map<String, Double>> emptyQuotas = Collections.emptyMap();

        when(admin.describeClientQuotas(ClientQuotaFilter.all())).thenReturn(describeClientQuotasResult);
        when(describeClientQuotasResult.entities()).thenReturn(describeQuotasFuture);
        when(describeQuotasFuture.get()).thenReturn(emptyQuotas);

        // When
        List<QuotaResponse> result = quotaService.listQuotas("localhost:9092", null, null, null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(admin).close();
    }

    @Test
    void testListQuotasCombinedEntity() throws Exception {
        // Given
        Map<ClientQuotaEntity, Map<String, Double>> quotas = createMockCombinedQuotas();

        when(admin.describeClientQuotas(ClientQuotaFilter.all())).thenReturn(describeClientQuotasResult);
        when(describeClientQuotasResult.entities()).thenReturn(describeQuotasFuture);
        when(describeQuotasFuture.get()).thenReturn(quotas);

        // When
        List<QuotaResponse> result = quotaService.listQuotas("localhost:9092", null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        verify(admin).close();
    }

    @Test
    void testListQuotasDefaultEntity() throws Exception {
        // Given
        Map<ClientQuotaEntity, Map<String, Double>> quotas = createMockDefaultQuotas();

        when(admin.describeClientQuotas(ClientQuotaFilter.all())).thenReturn(describeClientQuotasResult);
        when(describeClientQuotasResult.entities()).thenReturn(describeQuotasFuture);
        when(describeQuotasFuture.get()).thenReturn(quotas);

        // When
        List<QuotaResponse> result = quotaService.listQuotas("localhost:9092", null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        QuotaResponse defaultQuota = result.get(0);
        assertEquals("default", defaultQuota.getEntityType());
        assertEquals("", defaultQuota.getEntityName());

        verify(admin).close();
    }

    @Test
    void testListQuotasClientIdOnly() throws Exception {
        // Given
        Map<ClientQuotaEntity, Map<String, Double>> quotas = createMockClientIdQuotas();

        when(admin.describeClientQuotas(ClientQuotaFilter.all())).thenReturn(describeClientQuotasResult);
        when(describeClientQuotasResult.entities()).thenReturn(describeQuotasFuture);
        when(describeQuotasFuture.get()).thenReturn(quotas);

        // When
        List<QuotaResponse> result = quotaService.listQuotas("localhost:9092", null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        QuotaResponse clientQuota = result.get(0);
        assertEquals("client-id", clientQuota.getEntityType());
        assertEquals("test-client", clientQuota.getEntityName());

        verify(admin).close();
    }

    @Test
    void testCreateOrAlterQuotaSuccess() throws Exception {
        // Given
        CreateQuotaRequest request = new CreateQuotaRequest();
        request.setUsername("test-user");
        request.setBytesInQuota(1000000L);
        request.setBytesOutQuota(2000000L);

        when(admin.alterClientQuotas(anyCollection())).thenReturn(alterClientQuotasResult);
        when(alterClientQuotasResult.all()).thenReturn(alterQuotasFuture);
        when(alterQuotasFuture.get()).thenReturn(null);

        // When
        quotaService.createOrAlterQuota(request, "localhost:9092", null, null, null, null);

        // Then
        verify(admin).alterClientQuotas(anyCollection());
        verify(admin).close();
    }

    @Test
    void testCreateOrAlterQuotaWithOnlyBytesIn() throws Exception {
        // Given
        CreateQuotaRequest request = new CreateQuotaRequest();
        request.setUsername("test-user");
        request.setBytesInQuota(1000000L);
        request.setBytesOutQuota(null);

        when(admin.alterClientQuotas(anyCollection())).thenReturn(alterClientQuotasResult);
        when(alterClientQuotasResult.all()).thenReturn(alterQuotasFuture);
        when(alterQuotasFuture.get()).thenReturn(null);

        // When
        quotaService.createOrAlterQuota(request, "localhost:9092", null, null, null, null);

        // Then
        verify(admin).alterClientQuotas(anyCollection());
        verify(admin).close();
    }

    @Test
    void testCreateOrAlterQuotaWithOnlyBytesOut() throws Exception {
        // Given
        CreateQuotaRequest request = new CreateQuotaRequest();
        request.setUsername("test-user");
        request.setBytesInQuota(null);
        request.setBytesOutQuota(2000000L);

        when(admin.alterClientQuotas(anyCollection())).thenReturn(alterClientQuotasResult);
        when(alterClientQuotasResult.all()).thenReturn(alterQuotasFuture);
        when(alterQuotasFuture.get()).thenReturn(null);

        // When
        quotaService.createOrAlterQuota(request, "localhost:9092", null, null, null, null);

        // Then
        verify(admin).alterClientQuotas(anyCollection());
        verify(admin).close();
    }

    @Test
    void testCreateOrAlterQuotaExecutionException() throws Exception {
        // Given
        CreateQuotaRequest request = new CreateQuotaRequest();
        request.setUsername("test-user");
        request.setBytesInQuota(1000000L);
        request.setBytesOutQuota(2000000L);

        when(admin.alterClientQuotas(anyCollection())).thenReturn(alterClientQuotasResult);
        when(alterClientQuotasResult.all()).thenReturn(alterQuotasFuture);
        when(alterQuotasFuture.get()).thenThrow(new ExecutionException("Quota creation failed", new RuntimeException()));

        // When & Then
        assertThrows(ExecutionException.class, () ->
            quotaService.createOrAlterQuota(request, "localhost:9092", null, null, null, null)
        );
        verify(admin).close();
    }

    @Test
    void testDeleteQuotaSuccess() throws Exception {
        // Given
        when(admin.alterClientQuotas(anyCollection())).thenReturn(alterClientQuotasResult);
        when(alterClientQuotasResult.all()).thenReturn(alterQuotasFuture);
        when(alterQuotasFuture.get()).thenReturn(null);

        // When
        quotaService.deleteQuota("test-user", "localhost:9092", null, null, null, null);

        // Then
        verify(admin).alterClientQuotas(anyCollection());
        verify(admin).close();
    }

    @Test
    void testDeleteQuotaNullUsername() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            quotaService.deleteQuota(null, "localhost:9092", null, null, null, null)
        );
        verify(admin, never()).alterClientQuotas(anyCollection());
    }

    @Test
    void testDeleteQuotaEmptyUsername() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            quotaService.deleteQuota("", "localhost:9092", null, null, null, null)
        );
        verify(admin, never()).alterClientQuotas(anyCollection());
    }

    @Test
    void testDeleteQuotaExecutionException() throws Exception {
        // Given
        when(admin.alterClientQuotas(anyCollection())).thenReturn(alterClientQuotasResult);
        when(alterClientQuotasResult.all()).thenReturn(alterQuotasFuture);
        when(alterQuotasFuture.get()).thenThrow(new ExecutionException("Quota deletion failed", new RuntimeException()));

        // When & Then
        assertThrows(ExecutionException.class, () ->
            quotaService.deleteQuota("test-user", "localhost:9092", null, null, null, null)
        );
        verify(admin).close();
    }

    @Test
    void testGetUserQuotaSuccess() throws Exception {
        // Given
        Map<ClientQuotaEntity, Map<String, Double>> quotas = createMockUserQuotas();

        when(admin.describeClientQuotas(any(ClientQuotaFilter.class))).thenReturn(describeClientQuotasResult);
        when(describeClientQuotasResult.entities()).thenReturn(describeQuotasFuture);
        when(describeQuotasFuture.get()).thenReturn(quotas);

        // When
        QuotaResponse result = quotaService.getUserQuota("test-user", "localhost:9092", null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals("user", result.getEntityType());
        assertEquals("test-user", result.getEntityName());
        assertNotNull(result.getConfigs());

        verify(admin).close();
    }

    @Test
    void testGetUserQuotaNotFound() throws Exception {
        // Given
        Map<ClientQuotaEntity, Map<String, Double>> emptyQuotas = Collections.emptyMap();

        when(admin.describeClientQuotas(any(ClientQuotaFilter.class))).thenReturn(describeClientQuotasResult);
        when(describeClientQuotasResult.entities()).thenReturn(describeQuotasFuture);
        when(describeQuotasFuture.get()).thenReturn(emptyQuotas);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            quotaService.getUserQuota("non-existent-user", "localhost:9092", null, null, null, null)
        );
        verify(admin).close();
    }

    @Test
    void testGetUserQuotaExecutionException() throws Exception {
        // Given
        when(admin.describeClientQuotas(any(ClientQuotaFilter.class))).thenReturn(describeClientQuotasResult);
        when(describeClientQuotasResult.entities()).thenReturn(describeQuotasFuture);
        when(describeQuotasFuture.get()).thenThrow(new ExecutionException("Failed to get quota", new RuntimeException()));

        // When & Then
        assertThrows(ExecutionException.class, () ->
            quotaService.getUserQuota("test-user", "localhost:9092", null, null, null, null)
        );
        verify(admin).close();
    }

    @Test
    void testListQuotasWithSecurityParams() throws Exception {
        // Given
        Map<ClientQuotaEntity, Map<String, Double>> quotas = createMockUserQuotas();

        when(admin.describeClientQuotas(ClientQuotaFilter.all())).thenReturn(describeClientQuotasResult);
        when(describeClientQuotasResult.entities()).thenReturn(describeQuotasFuture);
        when(describeQuotasFuture.get()).thenReturn(quotas);

        // When
        List<QuotaResponse> result = quotaService.listQuotas("localhost:9092", "SASL_SSL", "admin", "adminpass", "SCRAM-SHA-512");

        // Then
        assertNotNull(result);
        verify(adminClientFactory).createAdminClient("localhost:9092", "SASL_SSL", "admin", "adminpass", "SCRAM-SHA-512");
        verify(admin).close();
    }

    private Map<ClientQuotaEntity, Map<String, Double>> createMockUserQuotas() {
        Map<String, String> entityMap = new HashMap<>();
        entityMap.put("user", "test-user");

        ClientQuotaEntity entity = new ClientQuotaEntity(entityMap);
        Map<String, Double> values = new HashMap<>();
        values.put("producer_byte_rate", 1000000.0);
        values.put("consumer_byte_rate", 2000000.0);

        return Map.of(entity, values);
    }

    private Map<ClientQuotaEntity, Map<String, Double>> createMockCombinedQuotas() {
        Map<String, String> entityMap = new HashMap<>();
        entityMap.put("user", "test-user");
        entityMap.put("client-id", "test-client");

        ClientQuotaEntity entity = new ClientQuotaEntity(entityMap);
        Map<String, Double> values = new HashMap<>();
        values.put("producer_byte_rate", 1000000.0);
        values.put("consumer_byte_rate", 2000000.0);

        return Map.of(entity, values);
    }

    private Map<ClientQuotaEntity, Map<String, Double>> createMockDefaultQuotas() {
        ClientQuotaEntity entity = new ClientQuotaEntity(Collections.emptyMap());
        Map<String, Double> values = new HashMap<>();
        values.put("producer_byte_rate", 500000.0);

        return Map.of(entity, values);
    }

    private Map<ClientQuotaEntity, Map<String, Double>> createMockClientIdQuotas() {
        Map<String, String> entityMap = new HashMap<>();
        entityMap.put("client-id", "test-client");

        ClientQuotaEntity entity = new ClientQuotaEntity(entityMap);
        Map<String, Double> values = new HashMap<>();
        values.put("producer_byte_rate", 1500000.0);

        return Map.of(entity, values);
    }
}
