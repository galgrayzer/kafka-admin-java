package com.kafka.admin.model;

import com.kafka.admin.model.response.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void testSuccessResponse() {
        ApiResponse response = ApiResponse.success("Operation successful");
        
        assertTrue(response.isSuccess());
        assertEquals("Operation successful", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testSuccessResponseWithData() {
        Map<String, String> data = Map.of("key", "value");
        ApiResponse response = ApiResponse.success("Created", data);
        
        assertTrue(response.isSuccess());
        assertEquals("Created", response.getMessage());
        assertNotNull(response.getData());
    }

    @Test
    void testErrorResponse() {
        ApiResponse response = ApiResponse.error("Error occurred");
        
        assertFalse(response.isSuccess());
        assertEquals("Error occurred", response.getMessage());
        assertNull(response.getData());
    }
}
