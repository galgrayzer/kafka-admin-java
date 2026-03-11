package com.kafka.admin.service;

import com.kafka.admin.client.KafkaAdminClientFactory;
import com.kafka.admin.model.request.CreateTopicRequest;
import com.kafka.admin.model.request.UpdateTopicConfigRequest;
import com.kafka.admin.model.response.TopicResponse;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.ConfigResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TopicServiceTest {

    @Mock
    private KafkaAdminClientFactory adminClientFactory;

    @Mock
    private Admin admin;

    @Mock
    private ListTopicsResult listTopicsResult;

    @Mock
    private DescribeTopicsResult describeTopicsResult;

    @Mock
    private CreateTopicsResult createTopicsResult;

    @Mock
    private DeleteTopicsResult deleteTopicsResult;

    @Mock
    private AlterConfigsResult alterConfigsResult;

    @Mock
    private DescribeConfigsResult describeConfigsResult;

    @Mock
    private KafkaFuture<Set<String>> topicNamesFuture;

    @Mock
    private KafkaFuture<Map<String, TopicDescription>> topicDescriptionsFuture;

    @Mock
    private KafkaFuture<Void> voidFuture;

    @Mock
    private KafkaFuture<Map<ConfigResource, Config>> configsFuture;

    private TopicService topicService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        topicService = new TopicService(adminClientFactory);
        when(adminClientFactory.createAdminClient(anyString(), any(), any(), any(), any())).thenReturn(admin);
    }

    @Test
    void testListTopicsSuccess() throws ExecutionException, InterruptedException {
        // Given
        Set<String> topicNames = Set.of("topic1", "topic2");
        Map<String, TopicDescription> topicDescriptions = createMockTopicDescriptions();

        when(admin.listTopics()).thenReturn(listTopicsResult);
        when(listTopicsResult.names()).thenReturn(topicNamesFuture);
        when(topicNamesFuture.get()).thenReturn(topicNames);
        when(admin.describeTopics(eq(topicNames))).thenReturn(describeTopicsResult);
        when(describeTopicsResult.allTopicNames()).thenReturn(topicDescriptionsFuture);
        when(topicDescriptionsFuture.get()).thenReturn(topicDescriptions);

        // When
        List<TopicResponse> result = topicService.listTopics("localhost:9092", null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        Set<String> resultTopicNames = result.stream()
                .map(TopicResponse::getName)
                .collect(Collectors.toSet());
        assertTrue(resultTopicNames.contains("topic1"));
        assertTrue(resultTopicNames.contains("topic2"));
        verify(admin).close();
    }

    @Test
    void testListTopicsEmptyResult() throws ExecutionException, InterruptedException {
        // Given
        Set<String> emptyTopicNames = Collections.emptySet();

        when(admin.listTopics()).thenReturn(listTopicsResult);
        when(listTopicsResult.names()).thenReturn(topicNamesFuture);
        when(topicNamesFuture.get()).thenReturn(emptyTopicNames);

        // When
        List<TopicResponse> result = topicService.listTopics("localhost:9092", null, null, null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(admin, never()).describeTopics((Collection<String>) any());
        verify(admin).close();
    }

    @Test
    void testGetTopicSuccess() throws ExecutionException, InterruptedException {
        // Given
        String topicName = "test-topic";
        TopicDescription topicDescription = createMockTopicDescription(topicName);
        Map<String, TopicDescription> topicDescriptions = Map.of(topicName, topicDescription);
        Map<ConfigResource, Config> configs = createMockConfigs();

        when(admin.describeTopics(eq(Collections.singletonList(topicName)))).thenReturn(describeTopicsResult);
        when(describeTopicsResult.allTopicNames()).thenReturn(topicDescriptionsFuture);
        when(topicDescriptionsFuture.get()).thenReturn(topicDescriptions);
        when(admin.describeConfigs(any())).thenReturn(describeConfigsResult);
        when(describeConfigsResult.all()).thenReturn(configsFuture);
        when(configsFuture.get()).thenReturn(configs);

        // When
        TopicResponse result = topicService.getTopic(topicName, "localhost:9092", null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(topicName, result.getName());
        assertEquals(3, result.getPartitions());
        assertEquals((short) 2, result.getReplicationFactor());
        assertNotNull(result.getConfigs());
        verify(admin).close();
    }

    @Test
    void testCreateTopicSuccess() throws ExecutionException, InterruptedException {
        // Given
        CreateTopicRequest request = new CreateTopicRequest();
        request.setName("new-topic");
        request.setPartitions(3);
        request.setReplicationFactor((short) 2);
        request.setConfigs(Map.of("retention.ms", "86400000"));

        when(admin.createTopics(any())).thenReturn(createTopicsResult);
        when(createTopicsResult.all()).thenReturn(voidFuture);
        when(voidFuture.get()).thenReturn(null);

        // When & Then
        assertDoesNotThrow(() -> 
            topicService.createTopic(request, "localhost:9092", null, null, null, null)
        );
        
        verify(admin).createTopics(argThat(topics -> {
            NewTopic newTopic = topics.iterator().next();
            return "new-topic".equals(newTopic.name()) &&
                   newTopic.numPartitions() == 3 &&
                   newTopic.replicationFactor() == 2 &&
                   newTopic.configs().containsKey("retention.ms");
        }));
        verify(admin).close();
    }

    @Test
    void testCreateTopicWithoutConfigs() throws ExecutionException, InterruptedException {
        // Given
        CreateTopicRequest request = new CreateTopicRequest();
        request.setName("simple-topic");
        request.setPartitions(1);
        request.setReplicationFactor((short) 1);

        when(admin.createTopics(any())).thenReturn(createTopicsResult);
        when(createTopicsResult.all()).thenReturn(voidFuture);
        when(voidFuture.get()).thenReturn(null);

        // When & Then
        assertDoesNotThrow(() -> 
            topicService.createTopic(request, "localhost:9092", null, null, null, null)
        );
        
        verify(admin).createTopics(argThat(topics -> {
            NewTopic newTopic = topics.iterator().next();
            return "simple-topic".equals(newTopic.name()) &&
                   newTopic.numPartitions() == 1 &&
                   newTopic.replicationFactor() == 1;
        }));
        verify(admin).close();
    }

    @Test
    void testDeleteTopicSuccess() throws ExecutionException, InterruptedException {
        // Given
        String topicName = "topic-to-delete";

        when(admin.deleteTopics(Collections.singletonList(topicName))).thenReturn(deleteTopicsResult);
        when(deleteTopicsResult.all()).thenReturn(voidFuture);
        when(voidFuture.get()).thenReturn(null);

        // When & Then
        assertDoesNotThrow(() -> 
            topicService.deleteTopic(topicName, "localhost:9092", null, null, null, null)
        );
        
        verify(admin).deleteTopics(Collections.singletonList(topicName));
        verify(admin).close();
    }

    @Test
    void testUpdateTopicConfigSuccess() throws ExecutionException, InterruptedException {
        // Given
        String topicName = "test-topic";
        UpdateTopicConfigRequest request = new UpdateTopicConfigRequest();
        request.setConfigs(Map.of("retention.ms", "172800000"));

        when(admin.incrementalAlterConfigs(any())).thenReturn(alterConfigsResult);
        when(alterConfigsResult.all()).thenReturn(voidFuture);
        when(voidFuture.get()).thenReturn(null);

        // When & Then
        assertDoesNotThrow(() -> 
            topicService.updateTopicConfig(topicName, request, "localhost:9092", null, null, null, null)
        );
        
        verify(admin).incrementalAlterConfigs(any());
        verify(admin).close();
    }

    @Test
    void testListTopicsExecutionException() throws ExecutionException, InterruptedException {
        // Given
        when(admin.listTopics()).thenReturn(listTopicsResult);
        when(listTopicsResult.names()).thenReturn(topicNamesFuture);
        when(topicNamesFuture.get()).thenThrow(new ExecutionException("Kafka error", new RuntimeException()));

        // When & Then
        assertThrows(ExecutionException.class, () -> 
            topicService.listTopics("localhost:9092", null, null, null, null)
        );
        verify(admin).close();
    }

    @Test
    void testCreateTopicExecutionException() throws ExecutionException, InterruptedException {
        // Given
        CreateTopicRequest request = new CreateTopicRequest();
        request.setName("failing-topic");
        request.setPartitions(1);
        request.setReplicationFactor((short) 1);

        when(admin.createTopics(any())).thenReturn(createTopicsResult);
        when(createTopicsResult.all()).thenReturn(voidFuture);
        when(voidFuture.get()).thenThrow(new ExecutionException("Topic already exists", new RuntimeException()));

        // When & Then
        assertThrows(ExecutionException.class, () -> 
            topicService.createTopic(request, "localhost:9092", null, null, null, null)
        );
        verify(admin).close();
    }

    private Map<String, TopicDescription> createMockTopicDescriptions() {
        return Map.of(
            "topic1", createMockTopicDescription("topic1"),
            "topic2", createMockTopicDescription("topic2")
        );
    }

    private TopicDescription createMockTopicDescription(String name) {
        Node leader = new Node(0, "broker0", 9092);
        Node replica1 = new Node(1, "broker1", 9092);
        Node replica2 = new Node(2, "broker2", 9092);

        List<TopicPartitionInfo> partitions = Arrays.asList(
            new TopicPartitionInfo(0, leader, Arrays.asList(leader, replica1), Arrays.asList(leader, replica1)),
            new TopicPartitionInfo(1, replica1, Arrays.asList(replica1, replica2), Arrays.asList(replica1, replica2)),
            new TopicPartitionInfo(2, replica2, Arrays.asList(replica2, leader), Arrays.asList(replica2, leader))
        );

        return new TopicDescription(name, false, partitions);
    }

    private Map<ConfigResource, Config> createMockConfigs() {
        ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, "test-topic");
        List<ConfigEntry> entries = Arrays.asList(
            new ConfigEntry("retention.ms", "86400000"),
            new ConfigEntry("cleanup.policy", "delete")
        );
        Config config = new Config(entries);
        return Map.of(resource, config);
    }
}
