package com.kafka.admin.service;

import com.kafka.admin.client.KafkaAdminClientFactory;
import com.kafka.admin.model.request.CreateAclRequest;
import com.kafka.admin.model.request.GrantConsumerAclRequest;
import com.kafka.admin.model.request.GrantProducerAclRequest;
import com.kafka.admin.model.response.AclResponse;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.acl.*;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AclServiceTest {

    @Mock
    private KafkaAdminClientFactory adminClientFactory;

    @Mock
    private Admin admin;

    @Mock
    private DescribeAclsResult describeAclsResult;

    @Mock
    private CreateAclsResult createAclsResult;

    @Mock
    private DeleteAclsResult deleteAclsResult;

    @Mock
    private KafkaFuture<Collection<AclBinding>> describeAclsFuture;

    @Mock
    private KafkaFuture<Collection<AclBinding>> deleteAclsFuture;

    @Mock
    private KafkaFuture<Void> createAclsFuture;

    private AclService aclService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        aclService = new AclService(adminClientFactory);
        when(adminClientFactory.createAdminClient(anyString(), any(), any(), any(), any())).thenReturn(admin);
    }

    @Test
    void testListAclsSuccess() throws Exception {
        // Given
        Collection<AclBinding> aclBindings = createMockAclBindings();

        when(admin.describeAcls(AclBindingFilter.ANY)).thenReturn(describeAclsResult);
        when(describeAclsResult.values()).thenReturn(describeAclsFuture);
        when(describeAclsFuture.get()).thenReturn(aclBindings);

        // When
        List<AclResponse> result = aclService.listAcls("localhost:9092", null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        AclResponse firstAcl = result.get(0);
        assertEquals("TOPIC", firstAcl.getResourceType());
        assertEquals("test-topic", firstAcl.getResourceName());
        assertEquals("User:test-user", firstAcl.getPrincipal());
        assertEquals("*", firstAcl.getHost());
        assertEquals("READ", firstAcl.getOperation());
        assertEquals("ALLOW", firstAcl.getPermission());

        verify(admin).close();
    }

    @Test
    void testListAclsEmpty() throws Exception {
        // Given
        Collection<AclBinding> emptyBindings = Collections.emptyList();

        when(admin.describeAcls(AclBindingFilter.ANY)).thenReturn(describeAclsResult);
        when(describeAclsResult.values()).thenReturn(describeAclsFuture);
        when(describeAclsFuture.get()).thenReturn(emptyBindings);

        // When
        List<AclResponse> result = aclService.listAcls("localhost:9092", null, null, null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(admin).close();
    }

    @Test
    void testCreateAclSuccess() throws Exception {
        // Given
        CreateAclRequest request = new CreateAclRequest();
        request.setResourceType("TOPIC");
        request.setResourceName("test-topic");
        request.setPrincipal("User:test-user");
        request.setOperation("READ");
        request.setPermission("ALLOW");

        when(admin.createAcls(anyList())).thenReturn(createAclsResult);
        when(createAclsResult.all()).thenReturn(createAclsFuture);
        when(createAclsFuture.get()).thenReturn(null);

        // When
        aclService.createAcl(request, "localhost:9092", null, null, null, null);

        // Then
        verify(admin).createAcls(argThat(aclBindings -> {
            List<AclBinding> list = new ArrayList<>(aclBindings);
            assertEquals(1, list.size());
            AclBinding binding = list.get(0);
            assertEquals(ResourceType.TOPIC, binding.pattern().resourceType());
            assertEquals("test-topic", binding.pattern().name());
            assertEquals("User:test-user", binding.entry().principal());
            assertEquals("*", binding.entry().host());
            assertEquals(AclOperation.READ, binding.entry().operation());
            assertEquals(AclPermissionType.ALLOW, binding.entry().permissionType());
            return true;
        }));
        verify(admin).close();
    }

    @Test
    void testCreateAclWithHost() throws Exception {
        // Given
        CreateAclRequest request = new CreateAclRequest();
        request.setResourceType("TOPIC");
        request.setResourceName("test-topic");
        request.setPrincipal("User:test-user");
        request.setHost("192.168.1.1");
        request.setOperation("WRITE");
        request.setPermission("ALLOW");

        when(admin.createAcls(anyList())).thenReturn(createAclsResult);
        when(createAclsResult.all()).thenReturn(createAclsFuture);
        when(createAclsFuture.get()).thenReturn(null);

        // When
        aclService.createAcl(request, "localhost:9092", null, null, null, null);

        // Then
        verify(admin).createAcls(argThat(aclBindings -> {
            List<AclBinding> list = new ArrayList<>(aclBindings);
            assertEquals("192.168.1.1", list.get(0).entry().host());
            return true;
        }));
        verify(admin).close();
    }

    @Test
    void testCreateAclExecutionException() throws Exception {
        // Given
        CreateAclRequest request = new CreateAclRequest();
        request.setResourceType("TOPIC");
        request.setResourceName("test-topic");
        request.setPrincipal("User:test-user");
        request.setOperation("READ");
        request.setPermission("ALLOW");

        when(admin.createAcls(anyList())).thenReturn(createAclsResult);
        when(createAclsResult.all()).thenReturn(createAclsFuture);
        when(createAclsFuture.get()).thenThrow(new ExecutionException("ACL creation failed", new RuntimeException()));

        // When & Then
        assertThrows(ExecutionException.class, () ->
            aclService.createAcl(request, "localhost:9092", null, null, null, null)
        );
        verify(admin).close();
    }

    @Test
    void testDeleteAclSuccess() throws Exception {
        // Given
        Collection<AclBinding> aclBindings = Collections.emptyList();

        when(admin.deleteAcls(anyList())).thenReturn(deleteAclsResult);
        when(deleteAclsResult.all()).thenReturn(deleteAclsFuture);
        when(deleteAclsFuture.get()).thenReturn(aclBindings);

        // When
        aclService.deleteAcl("TOPIC", "test-topic", "User:test-user", "*", "READ", "ALLOW",
                "localhost:9092", null, null, null, null);

        // Then
        verify(admin).deleteAcls(anyList());
        verify(admin).close();
    }

    @Test
    void testDeleteAclExecutionException() throws Exception {
        // Given
        when(admin.deleteAcls(anyList())).thenReturn(deleteAclsResult);
        when(deleteAclsResult.all()).thenReturn(deleteAclsFuture);
        when(deleteAclsFuture.get()).thenThrow(new ExecutionException("ACL deletion failed", new RuntimeException()));

        // When & Then
        assertThrows(ExecutionException.class, () ->
            aclService.deleteAcl("TOPIC", "test-topic", "User:test-user", "*", "READ", "ALLOW",
                    "localhost:9092", null, null, null, null)
        );
        verify(admin).close();
    }

    @Test
    void testGrantConsumerAclWithGroup() throws Exception {
        // Given
        GrantConsumerAclRequest request = new GrantConsumerAclRequest();
        request.setTopic("test-topic");
        request.setGroup("test-group");

        when(admin.createAcls(anyList())).thenReturn(createAclsResult);
        when(createAclsResult.all()).thenReturn(createAclsFuture);
        when(createAclsFuture.get()).thenReturn(null);

        // When
        aclService.grantConsumerAcl("test-user", request, "localhost:9092", null, null, null, null);

        // Then
        verify(admin).createAcls(argThat(aclBindings -> {
            List<AclBinding> list = new ArrayList<>(aclBindings);
            assertEquals(3, list.size());
            assertEquals(ResourceType.TOPIC, list.get(0).pattern().resourceType());
            assertEquals(AclOperation.DESCRIBE, list.get(0).entry().operation());
            assertEquals(ResourceType.TOPIC, list.get(1).pattern().resourceType());
            assertEquals(AclOperation.READ, list.get(1).entry().operation());
            assertEquals(ResourceType.GROUP, list.get(2).pattern().resourceType());
            assertEquals(AclOperation.READ, list.get(2).entry().operation());
            return true;
        }));
        verify(admin).close();
    }

    @Test
    void testGrantConsumerAclWithoutGroup() throws Exception {
        // Given
        GrantConsumerAclRequest request = new GrantConsumerAclRequest();
        request.setTopic("test-topic");
        request.setGroup(null);

        when(admin.createAcls(anyList())).thenReturn(createAclsResult);
        when(createAclsResult.all()).thenReturn(createAclsFuture);
        when(createAclsFuture.get()).thenReturn(null);

        // When
        aclService.grantConsumerAcl("test-user", request, "localhost:9092", null, null, null, null);

        // Then
        verify(admin).createAcls(argThat(aclBindings -> {
            List<AclBinding> list = new ArrayList<>(aclBindings);
            assertEquals(2, list.size());
            return true;
        }));
        verify(admin).close();
    }

    @Test
    void testGrantConsumerAclWithEmptyGroup() throws Exception {
        // Given
        GrantConsumerAclRequest request = new GrantConsumerAclRequest();
        request.setTopic("test-topic");
        request.setGroup("");

        when(admin.createAcls(anyList())).thenReturn(createAclsResult);
        when(createAclsResult.all()).thenReturn(createAclsFuture);
        when(createAclsFuture.get()).thenReturn(null);

        // When
        aclService.grantConsumerAcl("test-user", request, "localhost:9092", null, null, null, null);

        // Then
        verify(admin).createAcls(argThat(aclBindings -> {
            List<AclBinding> list = new ArrayList<>(aclBindings);
            assertEquals(2, list.size());
            return true;
        }));
        verify(admin).close();
    }

    @Test
    void testGrantProducerAclWithTransactionId() throws Exception {
        // Given
        GrantProducerAclRequest request = new GrantProducerAclRequest();
        request.setTopic("test-topic");
        request.setTransactionId("test-txn");

        when(admin.createAcls(anyList())).thenReturn(createAclsResult);
        when(createAclsResult.all()).thenReturn(createAclsFuture);
        when(createAclsFuture.get()).thenReturn(null);

        // When
        aclService.grantProducerAcl("test-user", request, "localhost:9092", null, null, null, null);

        // Then
        verify(admin).createAcls(argThat(aclBindings -> {
            List<AclBinding> list = new ArrayList<>(aclBindings);
            assertEquals(4, list.size());
            assertEquals(AclOperation.DESCRIBE, list.get(0).entry().operation());
            assertEquals(AclOperation.WRITE, list.get(1).entry().operation());
            assertEquals(ResourceType.TRANSACTIONAL_ID, list.get(2).pattern().resourceType());
            assertEquals(AclOperation.WRITE, list.get(2).entry().operation());
            assertEquals(AclOperation.CREATE, list.get(3).entry().operation());
            return true;
        }));
        verify(admin).close();
    }

    @Test
    void testGrantProducerAclWithoutTransactionId() throws Exception {
        // Given
        GrantProducerAclRequest request = new GrantProducerAclRequest();
        request.setTopic("test-topic");
        request.setTransactionId(null);

        when(admin.createAcls(anyList())).thenReturn(createAclsResult);
        when(createAclsResult.all()).thenReturn(createAclsFuture);
        when(createAclsFuture.get()).thenReturn(null);

        // When
        aclService.grantProducerAcl("test-user", request, "localhost:9092", null, null, null, null);

        // Then
        verify(admin).createAcls(argThat(aclBindings -> {
            List<AclBinding> list = new ArrayList<>(aclBindings);
            assertEquals(3, list.size());
            assertEquals(AclOperation.DESCRIBE, list.get(0).entry().operation());
            assertEquals(AclOperation.WRITE, list.get(1).entry().operation());
            assertEquals(AclOperation.CREATE, list.get(2).entry().operation());
            return true;
        }));
        verify(admin).close();
    }

    @Test
    void testGrantProducerAclWithEmptyTransactionId() throws Exception {
        // Given
        GrantProducerAclRequest request = new GrantProducerAclRequest();
        request.setTopic("test-topic");
        request.setTransactionId("");

        when(admin.createAcls(anyList())).thenReturn(createAclsResult);
        when(createAclsResult.all()).thenReturn(createAclsFuture);
        when(createAclsFuture.get()).thenReturn(null);

        // When
        aclService.grantProducerAcl("test-user", request, "localhost:9092", null, null, null, null);

        // Then
        verify(admin).createAcls(argThat(aclBindings -> {
            List<AclBinding> list = new ArrayList<>(aclBindings);
            assertEquals(3, list.size());
            return true;
        }));
        verify(admin).close();
    }

    @Test
    void testCheckConsumerAclFound() throws Exception {
        // Given
        Collection<AclBinding> aclBindings = createMockAclBindings();

        when(admin.describeAcls(any(AclBindingFilter.class))).thenReturn(describeAclsResult);
        when(describeAclsResult.values()).thenReturn(describeAclsFuture);
        when(describeAclsFuture.get()).thenReturn(aclBindings);

        // When
        boolean result = aclService.checkConsumerAcl("test-user", "test-topic", "localhost:9092", null, null, null, null);

        // Then
        assertTrue(result);
        verify(admin).close();
    }

    @Test
    void testCheckConsumerAclNotFound() throws Exception {
        // Given
        Collection<AclBinding> emptyBindings = Collections.emptyList();

        when(admin.describeAcls(any(AclBindingFilter.class))).thenReturn(describeAclsResult);
        when(describeAclsResult.values()).thenReturn(describeAclsFuture);
        when(describeAclsFuture.get()).thenReturn(emptyBindings);

        // When
        boolean result = aclService.checkConsumerAcl("test-user", "test-topic", "localhost:9092", null, null, null, null);

        // Then
        assertFalse(result);
        verify(admin).close();
    }

    @Test
    void testCheckProducerAclFound() throws Exception {
        // Given
        Collection<AclBinding> aclBindings = createMockAclBindings();

        when(admin.describeAcls(any(AclBindingFilter.class))).thenReturn(describeAclsResult);
        when(describeAclsResult.values()).thenReturn(describeAclsFuture);
        when(describeAclsFuture.get()).thenReturn(aclBindings);

        // When
        boolean result = aclService.checkProducerAcl("test-user", "test-topic", "localhost:9092", null, null, null, null);

        // Then
        assertTrue(result);
        verify(admin).close();
    }

    @Test
    void testCheckProducerAclNotFound() throws Exception {
        // Given
        Collection<AclBinding> emptyBindings = Collections.emptyList();

        when(admin.describeAcls(any(AclBindingFilter.class))).thenReturn(describeAclsResult);
        when(describeAclsResult.values()).thenReturn(describeAclsFuture);
        when(describeAclsFuture.get()).thenReturn(emptyBindings);

        // When
        boolean result = aclService.checkProducerAcl("test-user", "test-topic", "localhost:9092", null, null, null, null);

        // Then
        assertFalse(result);
        verify(admin).close();
    }

    @Test
    void testRevokeConsumerAclWithGroup() throws Exception {
        // Given
        GrantConsumerAclRequest request = new GrantConsumerAclRequest();
        request.setTopic("test-topic");
        request.setGroup("test-group");

        Collection<AclBinding> aclBindings = Collections.emptyList();

        when(admin.deleteAcls(anyList())).thenReturn(deleteAclsResult);
        when(deleteAclsResult.all()).thenReturn(deleteAclsFuture);
        when(deleteAclsFuture.get()).thenReturn(aclBindings);

        // When
        aclService.revokeConsumerAcl("test-user", request, "localhost:9092", null, null, null, null);

        // Then
        verify(admin).deleteAcls(argThat(filters -> {
            List<AclBindingFilter> list = new ArrayList<>(filters);
            assertEquals(3, list.size());
            return true;
        }));
        verify(admin).close();
    }

    @Test
    void testRevokeConsumerAclWithoutGroup() throws Exception {
        // Given
        GrantConsumerAclRequest request = new GrantConsumerAclRequest();
        request.setTopic("test-topic");
        request.setGroup(null);

        Collection<AclBinding> aclBindings = Collections.emptyList();

        when(admin.deleteAcls(anyList())).thenReturn(deleteAclsResult);
        when(deleteAclsResult.all()).thenReturn(deleteAclsFuture);
        when(deleteAclsFuture.get()).thenReturn(aclBindings);

        // When
        aclService.revokeConsumerAcl("test-user", request, "localhost:9092", null, null, null, null);

        // Then
        verify(admin).deleteAcls(argThat(filters -> {
            List<AclBindingFilter> list = new ArrayList<>(filters);
            assertEquals(2, list.size());
            return true;
        }));
        verify(admin).close();
    }

    @Test
    void testRevokeProducerAclWithTransactionId() throws Exception {
        // Given
        GrantProducerAclRequest request = new GrantProducerAclRequest();
        request.setTopic("test-topic");
        request.setTransactionId("test-txn");

        Collection<AclBinding> aclBindings = Collections.emptyList();

        when(admin.deleteAcls(anyList())).thenReturn(deleteAclsResult);
        when(deleteAclsResult.all()).thenReturn(deleteAclsFuture);
        when(deleteAclsFuture.get()).thenReturn(aclBindings);

        // When
        aclService.revokeProducerAcl("test-user", request, "localhost:9092", null, null, null, null);

        // Then
        verify(admin).deleteAcls(argThat(filters -> {
            List<AclBindingFilter> list = new ArrayList<>(filters);
            assertEquals(4, list.size());
            return true;
        }));
        verify(admin).close();
    }

    @Test
    void testRevokeProducerAclWithoutTransactionId() throws Exception {
        // Given
        GrantProducerAclRequest request = new GrantProducerAclRequest();
        request.setTopic("test-topic");
        request.setTransactionId(null);

        Collection<AclBinding> aclBindings = Collections.emptyList();

        when(admin.deleteAcls(anyList())).thenReturn(deleteAclsResult);
        when(deleteAclsResult.all()).thenReturn(deleteAclsFuture);
        when(deleteAclsFuture.get()).thenReturn(aclBindings);

        // When
        aclService.revokeProducerAcl("test-user", request, "localhost:9092", null, null, null, null);

        // Then
        verify(admin).deleteAcls(argThat(filters -> {
            List<AclBindingFilter> list = new ArrayList<>(filters);
            assertEquals(3, list.size());
            return true;
        }));
        verify(admin).close();
    }

    @Test
    void testListAclsWithSecurityParams() throws Exception {
        // Given
        Collection<AclBinding> aclBindings = createMockAclBindings();

        when(admin.describeAcls(AclBindingFilter.ANY)).thenReturn(describeAclsResult);
        when(describeAclsResult.values()).thenReturn(describeAclsFuture);
        when(describeAclsFuture.get()).thenReturn(aclBindings);

        // When
        List<AclResponse> result = aclService.listAcls("localhost:9092", "SASL_SSL", "admin", "adminpass", "SCRAM-SHA-512");

        // Then
        assertNotNull(result);
        verify(adminClientFactory).createAdminClient("localhost:9092", "SASL_SSL", "admin", "adminpass", "SCRAM-SHA-512");
        verify(admin).close();
    }

    private Collection<AclBinding> createMockAclBindings() {
        ResourcePattern topicPattern = new ResourcePattern(ResourceType.TOPIC, "test-topic", PatternType.LITERAL);
        AccessControlEntry readEntry = new AccessControlEntry("User:test-user", "*", AclOperation.READ, AclPermissionType.ALLOW);
        AccessControlEntry writeEntry = new AccessControlEntry("User:test-user", "*", AclOperation.WRITE, AclPermissionType.ALLOW);

        return Arrays.asList(
                new AclBinding(topicPattern, readEntry),
                new AclBinding(topicPattern, writeEntry)
        );
    }
}
