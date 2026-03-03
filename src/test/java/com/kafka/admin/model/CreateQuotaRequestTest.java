package com.kafka.admin.model;

import com.kafka.admin.model.request.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreateQuotaRequestTest {

    @Test
    void testDefaultValues() {
        CreateQuotaRequest request = new CreateQuotaRequest();
        
        assertNull(request.getBytesInQuota());
        assertNull(request.getBytesOutQuota());
        assertNull(request.getUsername());
    }

    @Test
    void testSettersAndGetters() {
        CreateQuotaRequest request = new CreateQuotaRequest();
        
        request.setUsername("testuser");
        request.setBytesInQuota(1024L);
        request.setBytesOutQuota(2048L);
        
        assertEquals("testuser", request.getUsername());
        assertEquals(1024L, request.getBytesInQuota());
        assertEquals(2048L, request.getBytesOutQuota());
    }
}
