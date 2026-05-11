package com.kafka.admin.service;

import com.kafka.admin.client.KafkaAdminClientFactory;
import com.kafka.admin.model.request.CreateUserRequest;
import com.kafka.admin.model.response.UserResponse;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.acl.*;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private KafkaAdminClientFactory adminClientFactory;

    @Mock
    private Admin admin;

    @Mock
    private DescribeUserScramCredentialsResult describeUserScramCredentialsResult;

    @Mock
    private AlterUserScramCredentialsResult alterUserScramCredentialsResult;

    @Mock
    private DescribeClusterResult describeClusterResult;

    @Mock
    private DescribeAclsResult describeAclsResult;

    @Mock
    private KafkaFuture<Map<String, UserScramCredentialsDescription>> describeUserScramFuture;

    @Mock
    private KafkaFuture<Void> alterUserScramFuture;

    @Mock
    private KafkaFuture<String> clusterIdFuture;

    @Mock
    private KafkaFuture<Collection<AclBinding>> describeAclsFuture;

    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(adminClientFactory);
        when(adminClientFactory.createAdminClient(anyString(), any(), any(), any(), any())).thenReturn(admin);
    }

    @Test
    void testListUsersSuccess() throws Exception {
        // Given
        Map<String, UserScramCredentialsDescription> users = createMockUserCredentials();

        when(admin.describeUserScramCredentials()).thenReturn(describeUserScramCredentialsResult);
        when(describeUserScramCredentialsResult.all()).thenReturn(describeUserScramFuture);
        when(describeUserScramFuture.get()).thenReturn(users);

        // When
        List<UserResponse> result = userService.listUsers("localhost:9092", null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        UserResponse user1 = result.stream()
                .filter(u -> "test-user-1".equals(u.getUsername()))
                .findFirst()
                .orElse(null);
        assertNotNull(user1);
        assertTrue(user1.getMechanisms().contains("SCRAM_SHA_512"));

        verify(admin).close();
    }

    @Test
    void testListUsersEmpty() throws Exception {
        // Given
        Map<String, UserScramCredentialsDescription> emptyUsers = Collections.emptyMap();

        when(admin.describeUserScramCredentials()).thenReturn(describeUserScramCredentialsResult);
        when(describeUserScramCredentialsResult.all()).thenReturn(describeUserScramFuture);
        when(describeUserScramFuture.get()).thenReturn(emptyUsers);

        // When
        List<UserResponse> result = userService.listUsers("localhost:9092", null, null, null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(admin).close();
    }

    @Test
    void testCreateUserWithScramSha512() throws Exception {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("test-user");
        request.setPassword("test-password");
        request.setMechanism("SCRAM-SHA-512");

        when(admin.alterUserScramCredentials(anyList())).thenReturn(alterUserScramCredentialsResult);
        when(alterUserScramCredentialsResult.all()).thenReturn(alterUserScramFuture);
        when(alterUserScramFuture.get()).thenReturn(null);

        // When
        userService.createUser(request, "localhost:9092", null, null, null, null);

        // Then
        verify(admin).alterUserScramCredentials(anyList());
        verify(admin).close();
    }

    @Test
    void testCreateUserWithScramSha256() throws Exception {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("test-user");
        request.setPassword("test-password");
        request.setMechanism("SCRAM-SHA-256");

        when(admin.alterUserScramCredentials(anyList())).thenReturn(alterUserScramCredentialsResult);
        when(alterUserScramCredentialsResult.all()).thenReturn(alterUserScramFuture);
        when(alterUserScramFuture.get()).thenReturn(null);

        // When
        userService.createUser(request, "localhost:9092", null, null, null, null);

        // Then
        verify(admin).alterUserScramCredentials(anyList());
        verify(admin).close();
    }

    @Test
    void testCreateUserWithDefaultMechanism() throws Exception {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("test-user");
        request.setPassword("test-password");
        request.setMechanism(null);

        when(admin.alterUserScramCredentials(anyList())).thenReturn(alterUserScramCredentialsResult);
        when(alterUserScramCredentialsResult.all()).thenReturn(alterUserScramFuture);
        when(alterUserScramFuture.get()).thenReturn(null);

        // When
        userService.createUser(request, "localhost:9092", null, null, null, null);

        // Then
        verify(admin).alterUserScramCredentials(anyList());
        verify(admin).close();
    }

    @Test
    void testCreateUserExecutionException() throws Exception {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("test-user");
        request.setPassword("test-password");

        when(admin.alterUserScramCredentials(anyList())).thenReturn(alterUserScramCredentialsResult);
        when(alterUserScramCredentialsResult.all()).thenReturn(alterUserScramFuture);
        when(alterUserScramFuture.get()).thenThrow(new ExecutionException("User creation failed", new RuntimeException()));

        // When & Then
        assertThrows(ExecutionException.class, () ->
            userService.createUser(request, "localhost:9092", null, null, null, null)
        );
        verify(admin).close();
    }

    @Test
    void testDeleteUserSuccess() throws Exception {
        // Given
        String username = "test-user-1";
        Map<String, UserScramCredentialsDescription> users = createMockUserCredentials();

        when(admin.describeUserScramCredentials(anyList())).thenReturn(describeUserScramCredentialsResult);
        when(describeUserScramCredentialsResult.all()).thenReturn(describeUserScramFuture);
        when(describeUserScramFuture.get()).thenReturn(users);

        when(admin.alterUserScramCredentials(anyList())).thenReturn(alterUserScramCredentialsResult);
        when(alterUserScramCredentialsResult.all()).thenReturn(alterUserScramFuture);
        when(alterUserScramFuture.get()).thenReturn(null);

        // When
        userService.deleteUser(username, "localhost:9092", null, null, null, null);

        // Then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UserScramCredentialAlteration>> captor = ArgumentCaptor.forClass((Class) List.class);
        verify(admin).alterUserScramCredentials(captor.capture());
        List<UserScramCredentialAlteration> deletions = captor.getValue();
        assertEquals(1, deletions.size());
        UserScramCredentialDeletion deletion = (UserScramCredentialDeletion) deletions.get(0);
        assertEquals(username, deletion.user());
        assertEquals(ScramMechanism.SCRAM_SHA_512, deletion.mechanism());
        verify(admin).close();
    }

    @Test
    void testDeleteUserUserNotFound() throws Exception {
        // Given
        String username = "non-existent-user";
        Map<String, UserScramCredentialsDescription> emptyUsers = Collections.emptyMap();

        when(admin.describeUserScramCredentials(anyList())).thenReturn(describeUserScramCredentialsResult);
        when(describeUserScramCredentialsResult.all()).thenReturn(describeUserScramFuture);
        when(describeUserScramFuture.get()).thenReturn(emptyUsers);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            userService.deleteUser(username, "localhost:9092", null, null, null, null)
        );
        verify(admin).close();
    }

    @Test
    void testUserExistsSuccess() throws Exception {
        // Given
        Map<String, UserScramCredentialsDescription> users = createMockUserCredentials();

        when(admin.describeUserScramCredentials()).thenReturn(describeUserScramCredentialsResult);
        when(describeUserScramCredentialsResult.all()).thenReturn(describeUserScramFuture);
        when(describeUserScramFuture.get()).thenReturn(users);

        // When
        boolean result = userService.userExists("test-user-1", "localhost:9092", null, null, null, null);

        // Then
        assertTrue(result);
        verify(admin).close();
    }

    @Test
    void testUserDoesNotExist() throws Exception {
        // Given
        Map<String, UserScramCredentialsDescription> users = createMockUserCredentials();

        when(admin.describeUserScramCredentials()).thenReturn(describeUserScramCredentialsResult);
        when(describeUserScramCredentialsResult.all()).thenReturn(describeUserScramFuture);
        when(describeUserScramFuture.get()).thenReturn(users);

        // When
        boolean result = userService.userExists("non-existent-user", "localhost:9092", null, null, null, null);

        // Then
        assertFalse(result);
        verify(admin).close();
    }

    @Test
    void testUserExistsEmptyUsers() throws Exception {
        // Given
        Map<String, UserScramCredentialsDescription> users = Collections.emptyMap();

        when(admin.describeUserScramCredentials()).thenReturn(describeUserScramCredentialsResult);
        when(describeUserScramCredentialsResult.all()).thenReturn(describeUserScramFuture);
        when(describeUserScramFuture.get()).thenReturn(users);

        // When
        boolean result = userService.userExists("test-user", "localhost:9092", null, null, null, null);

        // Then
        assertFalse(result);
        verify(admin).close();
    }

    @Test
    void testCheckAuthenticationSuccessConsumer() throws Exception {
        // Given
        when(admin.describeCluster()).thenReturn(describeClusterResult);
        when(describeClusterResult.clusterId()).thenReturn(clusterIdFuture);
        when(clusterIdFuture.get()).thenReturn("test-cluster-id");

        when(admin.describeAcls(any(AclBindingFilter.class))).thenReturn(describeAclsResult);
        when(describeAclsResult.values()).thenReturn(describeAclsFuture);
        when(describeAclsFuture.get()).thenReturn(createMockAclBindings());

        // When
        Map<String, Object> result = userService.checkAuthentication(
                "test-user", "test-password", "test-topic", "localhost:9092", null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(true, result.get("authenticated"));
        assertEquals("test-user", result.get("username"));
        assertEquals("both", result.get("role"));
        assertEquals(true, result.get("isConsumer"));
        assertEquals(true, result.get("isProducer"));
        verify(admin).close();
    }

    @Test
    void testCheckAuthenticationSuccessNone() throws Exception {
        // Given
        when(admin.describeCluster()).thenReturn(describeClusterResult);
        when(describeClusterResult.clusterId()).thenReturn(clusterIdFuture);
        when(clusterIdFuture.get()).thenReturn("test-cluster-id");

        when(admin.describeAcls(any(AclBindingFilter.class))).thenReturn(describeAclsResult);
        when(describeAclsResult.values()).thenReturn(describeAclsFuture);
        when(describeAclsFuture.get()).thenReturn(Collections.emptyList());

        // When
        Map<String, Object> result = userService.checkAuthentication(
                "test-user", "test-password", "test-topic", "localhost:9092", null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(true, result.get("authenticated"));
        assertEquals("none", result.get("role"));
        assertEquals(false, result.get("isConsumer"));
        assertEquals(false, result.get("isProducer"));
        verify(admin).close();
    }

    @Test
    void testCheckAuthenticationFailure() throws Exception {
        // Given
        when(admin.describeCluster()).thenReturn(describeClusterResult);
        when(describeClusterResult.clusterId()).thenReturn(clusterIdFuture);
        when(clusterIdFuture.get()).thenThrow(new ExecutionException("Connection failed", new RuntimeException()));

        // When
        Map<String, Object> result = userService.checkAuthentication(
                "test-user", "test-password", "test-topic", "localhost:9092", null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(false, result.get("authenticated"));
        assertEquals("test-user", result.get("username"));
        assertEquals("none", result.get("role"));
        assertEquals(false, result.get("isConsumer"));
        assertEquals(false, result.get("isProducer"));
        assertNotNull(result.get("error"));
        verify(admin).close();
    }

    @Test
    void testListUsersWithSecurityParams() throws Exception {
        // Given
        Map<String, UserScramCredentialsDescription> users = createMockUserCredentials();

        when(admin.describeUserScramCredentials()).thenReturn(describeUserScramCredentialsResult);
        when(describeUserScramCredentialsResult.all()).thenReturn(describeUserScramFuture);
        when(describeUserScramFuture.get()).thenReturn(users);

        // When
        List<UserResponse> result = userService.listUsers("localhost:9092", "SASL_SSL", "admin", "adminpass", "SCRAM-SHA-512");

        // Then
        assertNotNull(result);
        verify(adminClientFactory).createAdminClient("localhost:9092", "SASL_SSL", "admin", "adminpass", "SCRAM-SHA-512");
        verify(admin).close();
    }

    private Map<String, UserScramCredentialsDescription> createMockUserCredentials() {
        ScramCredentialInfo info1 = new ScramCredentialInfo(ScramMechanism.SCRAM_SHA_512, 4096);
        ScramCredentialInfo info2 = new ScramCredentialInfo(ScramMechanism.SCRAM_SHA_256, 4096);

        UserScramCredentialsDescription desc1 = new UserScramCredentialsDescription("test-user-1", List.of(info1));
        UserScramCredentialsDescription desc2 = new UserScramCredentialsDescription("test-user-2", List.of(info2));

        return Map.of(
                "test-user-1", desc1,
                "test-user-2", desc2
        );
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
