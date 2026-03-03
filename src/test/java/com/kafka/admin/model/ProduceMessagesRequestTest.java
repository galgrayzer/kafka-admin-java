package com.kafka.admin.model;

import com.kafka.admin.model.request.ProduceMessagesRequest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProduceMessagesRequestTest {

    @Test
    void testProducerRecordSettersAndGetters() {
        ProduceMessagesRequest.ProducerRecord record = new ProduceMessagesRequest.ProducerRecord();
        
        record.setKey("key1");
        record.setValue("value1");
        record.setTimestamp(1234567890L);
        
        assertEquals("key1", record.getKey());
        assertEquals("value1", record.getValue());
        assertEquals(1234567890L, record.getTimestamp());
    }

    @Test
    void testRequestSettersAndGetters() {
        ProduceMessagesRequest request = new ProduceMessagesRequest();
        
        request.setTopic("test-topic");
        request.setPartition(0);
        
        ProduceMessagesRequest.ProducerRecord record = new ProduceMessagesRequest.ProducerRecord();
        record.setKey("key");
        record.setValue("value");
        request.setRecords(java.util.List.of(record));
        
        assertEquals("test-topic", request.getTopic());
        assertEquals(0, request.getPartition());
        assertEquals(1, request.getRecords().size());
    }

    @Test
    void testHeadersInProducerRecord() {
        ProduceMessagesRequest.ProducerRecord record = new ProduceMessagesRequest.ProducerRecord();
        Map<String, String> headers = Map.of("header1", "value1");
        record.setHeaders(headers);
        
        assertEquals("value1", record.getHeaders().get("header1"));
    }
}
