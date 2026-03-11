package com.kafka.admin.service;

import com.kafka.admin.client.KafkaAdminClientFactory;
import com.kafka.admin.model.request.CopyConsumerOffsetsRequest;
import com.kafka.admin.model.request.ResetConsumerOffsetsByTimeRequest;
import com.kafka.admin.model.request.ResetConsumerOffsetsRequest;
import com.kafka.admin.model.response.ConsumerOffsetResponse;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
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

class ConsumerServiceTest {

    @Mock
    private KafkaAdminClientFactory adminClientFactory;

    @Mock
    private Admin admin;

    @Mock
    private ListConsumerGroupOffsetsResult listOffsetsResult;

    @Mock
    private AlterConsumerGroupOffsetsResult alterOffsetsResult;

    @Mock
    private DescribeTopicsResult describeTopicsResult;

    @Mock
    private ListOffsetsResult listTopicOffsetsResult;

    @Mock
    private KafkaFuture<Map<TopicPartition, OffsetAndMetadata>> offsetsFuture;

    @Mock
    private KafkaFuture<Void> voidFuture;

    @Mock
    private KafkaFuture<Map<String, TopicDescription>> topicDescriptionsFuture;

    @Mock
    private KafkaFuture<ListOffsetsResult.ListOffsetsResultInfo> offsetResultFuture;

    private ConsumerService consumerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        consumerService = new ConsumerService(adminClientFactory);
        when(adminClientFactory.createAdminClient(anyString(), any(), any(), any(), any())).thenReturn(admin);
    }

    @Test
    void testGetConsumerOffsetsSuccess() throws ExecutionException, InterruptedException {
        // Given
        String groupId = "test-group";
        String topic = "test-topic";
        Map<TopicPartition, OffsetAndMetadata> offsets = createMockOffsets();

        when(admin.listConsumerGroupOffsets(groupId)).thenReturn(listOffsetsResult);
        when(listOffsetsResult.partitionsToOffsetAndMetadata()).thenReturn(offsetsFuture);
        when(offsetsFuture.get()).thenReturn(offsets);

        // When
        List<ConsumerOffsetResponse> result = consumerService.getConsumerOffsets(
                groupId, topic, "localhost:9092", null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        ConsumerOffsetResponse partition0 = result.stream()
                .filter(r -> r.getPartition() == 0)
                .findFirst()
                .orElse(null);
        assertNotNull(partition0);
        assertEquals(topic, partition0.getTopic());
        assertEquals(0, partition0.getPartition());
        assertEquals(100L, partition0.getCurrentOffset());

        verify(admin).close();
    }

    @Test
    void testResetConsumerOffsetsToEarliest() throws ExecutionException, InterruptedException {
        // Given
        String groupId = "test-group";
        ResetConsumerOffsetsRequest request = new ResetConsumerOffsetsRequest();
        request.setTopic("test-topic");
        request.setPartition(0);
        request.setResetStrategy("earliest");

        TopicPartition tp = new TopicPartition("test-topic", 0);
        ListOffsetsResult.ListOffsetsResultInfo offsetInfo = 
                new ListOffsetsResult.ListOffsetsResultInfo(0L, System.currentTimeMillis(), Optional.empty());

        when(admin.listOffsets(any())).thenReturn(listTopicOffsetsResult);
        when(listTopicOffsetsResult.partitionResult(tp)).thenReturn(offsetResultFuture);
        when(offsetResultFuture.get()).thenReturn(offsetInfo);
        when(admin.alterConsumerGroupOffsets(eq(groupId), any())).thenReturn(alterOffsetsResult);
        when(alterOffsetsResult.all()).thenReturn(voidFuture);
        when(voidFuture.get()).thenReturn(null);

        // When & Then
        assertDoesNotThrow(() -> 
            consumerService.resetConsumerOffsets(groupId, request, "localhost:9092", null, null, null, null)
        );

        verify(admin).alterConsumerGroupOffsets(eq(groupId), argThat(offsets -> 
            offsets.containsKey(tp) && offsets.get(tp).offset() == 0L
        ));
        verify(admin).close();
    }

    @Test
    void testResetConsumerOffsetsByTimestamp() throws ExecutionException, InterruptedException {
        // Given
        String groupId = "test-group";
        ResetConsumerOffsetsByTimeRequest request = new ResetConsumerOffsetsByTimeRequest();
        request.setTopic("test-topic");
        request.setPartition(0);
        request.setTimestamp(1704067200000L);

        TopicPartition tp = new TopicPartition("test-topic", 0);
        ListOffsetsResult.ListOffsetsResultInfo offsetInfo = 
                new ListOffsetsResult.ListOffsetsResultInfo(750L, 1704067200000L, Optional.empty());

        when(admin.listOffsets(any())).thenReturn(listTopicOffsetsResult);
        when(listTopicOffsetsResult.partitionResult(tp)).thenReturn(offsetResultFuture);
        when(offsetResultFuture.get()).thenReturn(offsetInfo);
        when(admin.alterConsumerGroupOffsets(eq(groupId), any())).thenReturn(alterOffsetsResult);
        when(alterOffsetsResult.all()).thenReturn(voidFuture);
        when(voidFuture.get()).thenReturn(null);

        // When & Then
        assertDoesNotThrow(() -> 
            consumerService.resetConsumerOffsetsByTimestamp(groupId, request, "localhost:9092", null, null, null, null)
        );

        verify(admin).alterConsumerGroupOffsets(eq(groupId), argThat(offsets -> 
            offsets.containsKey(tp) && offsets.get(tp).offset() == 750L
        ));
        verify(admin).close();
    }

    @Test
    void testCopyConsumerOffsets() throws ExecutionException, InterruptedException {
        // Given
        String targetGroupId = "target-group";
        CopyConsumerOffsetsRequest request = new CopyConsumerOffsetsRequest();
        request.setSourceGroup("source-group");
        request.setTopic("test-topic");

        Map<TopicPartition, OffsetAndMetadata> sourceOffsets = createMockOffsets();

        when(admin.listConsumerGroupOffsets("source-group")).thenReturn(listOffsetsResult);
        when(listOffsetsResult.partitionsToOffsetAndMetadata()).thenReturn(offsetsFuture);
        when(offsetsFuture.get()).thenReturn(sourceOffsets);
        when(admin.alterConsumerGroupOffsets(eq(targetGroupId), any())).thenReturn(alterOffsetsResult);
        when(alterOffsetsResult.all()).thenReturn(voidFuture);
        when(voidFuture.get()).thenReturn(null);

        // When & Then
        assertDoesNotThrow(() -> 
            consumerService.copyConsumerOffsets(targetGroupId, request, "localhost:9092", null, null, null, null)
        );

        verify(admin).alterConsumerGroupOffsets(eq(targetGroupId), argThat(offsets -> 
            offsets.size() == 2 && // Both partitions of test-topic
            offsets.containsKey(new TopicPartition("test-topic", 0)) &&
            offsets.containsKey(new TopicPartition("test-topic", 1))
        ));
        verify(admin).close();
    }

    private Map<TopicPartition, OffsetAndMetadata> createMockOffsets() {
        TopicPartition tp0 = new TopicPartition("test-topic", 0);
        TopicPartition tp1 = new TopicPartition("test-topic", 1);
        
        return Map.of(
            tp0, new OffsetAndMetadata(100L),
            tp1, new OffsetAndMetadata(200L)
        );
    }
}
