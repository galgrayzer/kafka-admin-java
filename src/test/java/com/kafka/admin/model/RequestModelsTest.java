package com.kafka.admin.model;

import com.kafka.admin.model.request.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestModelsTest {

    @Test
    void testResetConsumerOffsetsRequest() {
        ResetConsumerOffsetsRequest request = new ResetConsumerOffsetsRequest();
        
        request.setTopic("test-topic");
        request.setPartition(0);
        request.setOffset("100");
        request.setResetStrategy("earliest");
        
        assertEquals("test-topic", request.getTopic());
        assertEquals(0, request.getPartition());
        assertEquals("100", request.getOffset());
        assertEquals("earliest", request.getResetStrategy());
    }

    @Test
    void testResetConsumerOffsetsByTimeRequest() {
        ResetConsumerOffsetsByTimeRequest request = new ResetConsumerOffsetsByTimeRequest();
        
        request.setTopic("test-topic");
        request.setPartition(0);
        request.setTimestamp(1704067200000L);
        
        assertEquals("test-topic", request.getTopic());
        assertEquals(0, request.getPartition());
        assertEquals(1704067200000L, request.getTimestamp());
    }

    @Test
    void testCopyConsumerOffsetsRequest() {
        CopyConsumerOffsetsRequest request = new CopyConsumerOffsetsRequest();
        
        request.setSourceGroup("source-group");
        request.setTopic("test-topic");
        
        assertEquals("source-group", request.getSourceGroup());
        assertEquals("test-topic", request.getTopic());
    }

    @Test
    void testFetchMessagesRequest() {
        FetchMessagesRequest request = new FetchMessagesRequest();
        
        request.setTopic("test-topic");
        request.setPartition(0);
        request.setOffset(100L);
        request.setTimestamp(1704067200000L);
        request.setMaxMessages(50);
        request.setStartingPosition("earliest");
        
        assertEquals("test-topic", request.getTopic());
        assertEquals(0, request.getPartition());
        assertEquals(100L, request.getOffset());
        assertEquals(1704067200000L, request.getTimestamp());
        assertEquals(50, request.getMaxMessages());
        assertEquals("earliest", request.getStartingPosition());
    }
}
