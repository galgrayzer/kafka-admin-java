package com.kafka.admin.service;

import com.kafka.admin.client.KafkaAdminClientFactory;
import com.kafka.admin.model.response.ClusterMetadataResponse;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ClusterServiceTest {

    @Mock
    private KafkaAdminClientFactory adminClientFactory;

    @Mock
    private Admin admin;

    @Mock
    private DescribeClusterResult describeClusterResult;

    @Mock
    private ListTopicsResult listTopicsResult;

    @Mock
    private DescribeTopicsResult describeTopicsResult;

    @Mock
    private KafkaFuture<String> clusterIdFuture;

    @Mock
    private KafkaFuture<Collection<Node>> nodesFuture;

    @Mock
    private KafkaFuture<Set<String>> topicNamesFuture;

    @Mock
    private KafkaFuture<Map<String, TopicDescription>> topicDescriptionsFuture;

    private ClusterService clusterService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        clusterService = new ClusterService(adminClientFactory);
        when(adminClientFactory.createAdminClient(anyString(), any(), any(), any(), any())).thenReturn(admin);
    }

    @Test
    void testGetClusterMetadataSuccess() throws Exception {
        // Given
        String clusterId = "test-cluster-123";
        Collection<Node> nodes = createMockNodes();
        Set<String> topicNames = Set.of("topic1", "topic2");
        Map<String, TopicDescription> topicDescriptions = createMockTopicDescriptions();

        when(admin.describeCluster()).thenReturn(describeClusterResult);
        when(describeClusterResult.clusterId()).thenReturn(clusterIdFuture);
        when(clusterIdFuture.get()).thenReturn(clusterId);
        when(describeClusterResult.nodes()).thenReturn(nodesFuture);
        when(nodesFuture.get()).thenReturn(nodes);

        when(admin.listTopics()).thenReturn(listTopicsResult);
        when(listTopicsResult.names()).thenReturn(topicNamesFuture);
        when(topicNamesFuture.get()).thenReturn(topicNames);
        when(admin.describeTopics(eq(topicNames))).thenReturn(describeTopicsResult);
        when(describeTopicsResult.allTopicNames()).thenReturn(topicDescriptionsFuture);
        when(topicDescriptionsFuture.get()).thenReturn(topicDescriptions);

        // When
        ClusterMetadataResponse result = clusterService.getClusterMetadata("localhost:9092", null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(clusterId, result.getClusterId());
        assertEquals(3, result.getBrokers().size());
        assertEquals(2, result.getTopics().size());

        // Verify broker details
        ClusterMetadataResponse.Broker broker0 = result.getBrokers().get(0);
        assertEquals(0, broker0.getId());
        assertEquals("broker0", broker0.getHost());
        assertEquals(9092, broker0.getPort());
        assertEquals("rack1", broker0.getRack());

        // Verify topic details
        ClusterMetadataResponse.TopicMetadata topic1 = result.getTopics().stream()
                .filter(t -> "topic1".equals(t.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(topic1);
        assertEquals(3, topic1.getPartitionCount());
        assertEquals(Integer.valueOf(2), topic1.getReplicationFactor());
        assertFalse(topic1.getIsInternal());

        verify(admin).close();
    }

    @Test
    void testGetClusterMetadataWithNoTopics() throws Exception {
        // Given
        String clusterId = "empty-cluster";
        Collection<Node> nodes = createMockNodes();
        Set<String> emptyTopicNames = Collections.emptySet();

        when(admin.describeCluster()).thenReturn(describeClusterResult);
        when(describeClusterResult.clusterId()).thenReturn(clusterIdFuture);
        when(clusterIdFuture.get()).thenReturn(clusterId);
        when(describeClusterResult.nodes()).thenReturn(nodesFuture);
        when(nodesFuture.get()).thenReturn(nodes);

        when(admin.listTopics()).thenReturn(listTopicsResult);
        when(listTopicsResult.names()).thenReturn(topicNamesFuture);
        when(topicNamesFuture.get()).thenReturn(emptyTopicNames);

        // When
        ClusterMetadataResponse result = clusterService.getClusterMetadata("localhost:9092", null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(clusterId, result.getClusterId());
        assertEquals(3, result.getBrokers().size());
        assertTrue(result.getTopics().isEmpty());
        verify(admin, never()).describeTopics((Collection<String>) any());
        verify(admin).close();
    }

    @Test
    void testListTopicsSuccess() throws Exception {
        // Given
        Set<String> topicNames = Set.of("topic1", "topic2", "topic3");

        when(admin.listTopics()).thenReturn(listTopicsResult);
        when(listTopicsResult.names()).thenReturn(topicNamesFuture);
        when(topicNamesFuture.get()).thenReturn(topicNames);

        // When
        List<String> result = clusterService.listTopics("localhost:9092", null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.containsAll(topicNames));
        verify(admin).close();
    }

    @Test
    void testListTopicsEmpty() throws Exception {
        // Given
        Set<String> emptyTopicNames = Collections.emptySet();

        when(admin.listTopics()).thenReturn(listTopicsResult);
        when(listTopicsResult.names()).thenReturn(topicNamesFuture);
        when(topicNamesFuture.get()).thenReturn(emptyTopicNames);

        // When
        List<String> result = clusterService.listTopics("localhost:9092", null, null, null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(admin).close();
    }

    @Test
    void testGetClusterMetadataExecutionException() throws Exception {
        // Given
        when(admin.describeCluster()).thenReturn(describeClusterResult);
        when(describeClusterResult.clusterId()).thenReturn(clusterIdFuture);
        when(clusterIdFuture.get()).thenThrow(new ExecutionException("Connection failed", new RuntimeException()));

        // When & Then
        assertThrows(ExecutionException.class, () -> 
            clusterService.getClusterMetadata("localhost:9092", null, null, null, null)
        );
        verify(admin).close();
    }

    @Test
    void testListTopicsExecutionException() throws Exception {
        // Given
        when(admin.listTopics()).thenReturn(listTopicsResult);
        when(listTopicsResult.names()).thenReturn(topicNamesFuture);
        when(topicNamesFuture.get()).thenThrow(new ExecutionException("Timeout", new RuntimeException()));

        // When & Then
        assertThrows(ExecutionException.class, () -> 
            clusterService.listTopics("localhost:9092", null, null, null, null)
        );
        verify(admin).close();
    }

    @Test
    void testGetClusterMetadataWithSecurityParams() throws Exception {
        // Given
        String clusterId = "secure-cluster";
        Collection<Node> nodes = createMockNodes();
        Set<String> topicNames = Collections.emptySet();

        when(admin.describeCluster()).thenReturn(describeClusterResult);
        when(describeClusterResult.clusterId()).thenReturn(clusterIdFuture);
        when(clusterIdFuture.get()).thenReturn(clusterId);
        when(describeClusterResult.nodes()).thenReturn(nodesFuture);
        when(nodesFuture.get()).thenReturn(nodes);

        when(admin.listTopics()).thenReturn(listTopicsResult);
        when(listTopicsResult.names()).thenReturn(topicNamesFuture);
        when(topicNamesFuture.get()).thenReturn(topicNames);

        // When
        ClusterMetadataResponse result = clusterService.getClusterMetadata(
            "localhost:9092", "SASL_SSL", "testuser", "testpass", "SCRAM-SHA-256"
        );

        // Then
        assertNotNull(result);
        assertEquals(clusterId, result.getClusterId());
        verify(adminClientFactory).createAdminClient("localhost:9092", "SASL_SSL", "testuser", "testpass", "SCRAM-SHA-256");
        verify(admin).close();
    }

    private Collection<Node> createMockNodes() {
        return Arrays.asList(
            new Node(0, "broker0", 9092, "rack1"),
            new Node(1, "broker1", 9092, "rack2"),
            new Node(2, "broker2", 9092, "rack3")
        );
    }

    private Map<String, TopicDescription> createMockTopicDescriptions() {
        return Map.of(
            "topic1", createMockTopicDescription("topic1", false),
            "topic2", createMockTopicDescription("topic2", true)
        );
    }

    private TopicDescription createMockTopicDescription(String name, boolean isInternal) {
        Node leader = new Node(0, "broker0", 9092);
        Node replica1 = new Node(1, "broker1", 9092);

        List<TopicPartitionInfo> partitions = Arrays.asList(
            new TopicPartitionInfo(0, leader, Arrays.asList(leader, replica1), Arrays.asList(leader, replica1)),
            new TopicPartitionInfo(1, replica1, Arrays.asList(replica1, leader), Arrays.asList(replica1, leader)),
            new TopicPartitionInfo(2, leader, Arrays.asList(leader, replica1), Arrays.asList(leader, replica1))
        );

        return new TopicDescription(name, isInternal, partitions);
    }
}
