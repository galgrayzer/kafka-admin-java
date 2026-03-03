package com.kafka.admin.model;

import com.kafka.admin.model.response.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ResponseModelsTest {

    @Test
    void testConsumerOffsetResponse() {
        ConsumerOffsetResponse response = new ConsumerOffsetResponse();
        
        response.setTopic("test-topic");
        response.setPartition(0);
        response.setCurrentOffset(100L);
        response.setLogEndOffset(200L);
        response.setLag(100L);
        
        assertEquals("test-topic", response.getTopic());
        assertEquals(0, response.getPartition());
        assertEquals(100L, response.getCurrentOffset());
        assertEquals(200L, response.getLogEndOffset());
        assertEquals(100L, response.getLag());
    }

    @Test
    void testMessageResponse() {
        MessageResponse response = new MessageResponse();
        
        response.setTopic("test-topic");
        response.setPartition(0);
        response.setOffset(100L);
        response.setTimestamp(1704067200000L);
        response.setKey("key1");
        response.setValue("value1");
        
        assertEquals("test-topic", response.getTopic());
        assertEquals(0, response.getPartition());
        assertEquals(100L, response.getOffset());
        assertEquals(1704067200000L, response.getTimestamp());
        assertEquals("key1", response.getKey());
        assertEquals("value1", response.getValue());
    }

    @Test
    void testMessageResponseWithHeaders() {
        MessageResponse response = new MessageResponse();
        Map<String, String> headers = Map.of("header1", "value1");
        response.setHeaders(headers);
        
        assertEquals("value1", response.getHeaders().get("header1"));
    }

    @Test
    void testClusterMetadataResponse() {
        ClusterMetadataResponse response = new ClusterMetadataResponse();
        
        response.setClusterId("cluster-123");
        
        ClusterMetadataResponse.Broker broker = new ClusterMetadataResponse.Broker();
        broker.setId(0);
        broker.setHost("localhost");
        broker.setPort(9092);
        
        ClusterMetadataResponse.TopicMetadata topic = new ClusterMetadataResponse.TopicMetadata();
        topic.setName("test-topic");
        topic.setPartitionCount(3);
        topic.setReplicationFactor(1);
        topic.setIsInternal(false);
        
        response.setBrokers(List.of(broker));
        response.setTopics(List.of(topic));
        
        assertEquals("cluster-123", response.getClusterId());
        assertEquals(1, response.getBrokers().size());
        assertEquals(1, response.getTopics().size());
        assertEquals("localhost", response.getBrokers().get(0).getHost());
        assertEquals("test-topic", response.getTopics().get(0).getName());
    }
}
