package com.kafka.admin.model;

import com.kafka.admin.model.request.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreateUserRequestTest {

    @Test
    void testSettersAndGetters() {
        CreateUserRequest request = new CreateUserRequest();
        
        request.setUsername("testuser");
        request.setPassword("testpass");
        request.setMechanism("SCRAM-SHA-512");
        
        assertEquals("testuser", request.getUsername());
        assertEquals("testpass", request.getPassword());
        assertEquals("SCRAM-SHA-512", request.getMechanism());
    }

    @Test
    void testDefaultMechanism() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setPassword("testpass");
        
        assertNull(request.getMechanism());
    }
}
