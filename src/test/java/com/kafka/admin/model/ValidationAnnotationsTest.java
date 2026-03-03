package com.kafka.admin.model;

import com.kafka.admin.model.request.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ValidationAnnotationsTest {

    @Test
    void testCreateTopicRequestValidation() {
        CreateTopicRequest request = new CreateTopicRequest();
        request.setName("test-topic");
        request.setPartitions(3);
        request.setReplicationFactor((short) 1);
        request.setConfigs(Map.of("cleanup.policy", "delete"));
        
        assertEquals("test-topic", request.getName());
        assertEquals(3, (int) request.getPartitions());
        assertEquals(1, (int) request.getReplicationFactor());
        assertEquals("delete", request.getConfigs().get("cleanup.policy"));
    }

    @Test
    void testGrantProducerAclRequest() {
        GrantProducerAclRequest request = new GrantProducerAclRequest();
        request.setTopic("test-topic");
        request.setTransactionId("tx-123");
        
        assertEquals("test-topic", request.getTopic());
        assertEquals("tx-123", request.getTransactionId());
    }

    @Test
    void testGrantConsumerAclRequest() {
        GrantConsumerAclRequest request = new GrantConsumerAclRequest();
        request.setTopic("test-topic");
        request.setGroup("consumer-group");
        
        assertEquals("test-topic", request.getTopic());
        assertEquals("consumer-group", request.getGroup());
    }

    @Test
    void testCreateUserRequest() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setPassword("password");
        request.setMechanism("SCRAM-SHA-512");
        
        assertEquals("testuser", request.getUsername());
        assertEquals("password", request.getPassword());
        assertEquals("SCRAM-SHA-512", request.getMechanism());
    }

    @Test
    void testCreateAclRequest() {
        CreateAclRequest request = new CreateAclRequest();
        request.setResourceType("TOPIC");
        request.setResourceName("test-topic");
        request.setPrincipal("User:testuser");
        request.setHost("*");
        request.setOperation("READ");
        request.setPermission("ALLOW");
        
        assertEquals("TOPIC", request.getResourceType());
        assertEquals("test-topic", request.getResourceName());
        assertEquals("User:testuser", request.getPrincipal());
        assertEquals("*", request.getHost());
        assertEquals("READ", request.getOperation());
        assertEquals("ALLOW", request.getPermission());
    }

    @Test
    void testCreateClusterLinkRequest() {
        CreateClusterLinkRequest request = new CreateClusterLinkRequest();
        request.setLinkName("link-1");
        request.setSourceBootstrapServers("source:9092");
        
        assertEquals("link-1", request.getLinkName());
        assertEquals("source:9092", request.getSourceBootstrapServers());
    }

    @Test
    void testCreateMirrorTopicsRequest() {
        CreateMirrorTopicsRequest request = new CreateMirrorTopicsRequest();
        request.setTopics(List.of("topic1", "topic2"));
        
        assertEquals(2, request.getTopics().size());
    }

    @Test
    void testFailoverRequest() {
        FailoverRequest request = new FailoverRequest();
        request.setPrimaryClusterId("cluster-1");
        
        assertEquals("cluster-1", request.getPrimaryClusterId());
    }
}
