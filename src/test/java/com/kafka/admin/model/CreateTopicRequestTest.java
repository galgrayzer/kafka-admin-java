package com.kafka.admin.model;

import com.kafka.admin.model.request.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CreateTopicRequestTest {

    @Test
    void testSettersAndGetters() {
        CreateTopicRequest request = new CreateTopicRequest();
        
        request.setName("test-topic");
        request.setPartitions(3);
        request.setReplicationFactor((short) 1);
        request.setConfigs(Map.of("retention.ms", "86400000"));
        
        assertEquals("test-topic", request.getName());
        assertEquals(3, request.getPartitions());
        assertEquals((short) 1, request.getReplicationFactor());
        assertEquals("86400000", request.getConfigs().get("retention.ms"));
    }

    @Test
    void testDefaultValues() {
        CreateTopicRequest request = new CreateTopicRequest();
        
        assertNull(request.getName());
        assertNull(request.getConfigs());
    }
}
