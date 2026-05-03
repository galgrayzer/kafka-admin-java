package com.kafka.admin.model;

import com.kafka.admin.model.request.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ValidationAnnotationsTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testCreateTopicRequestValidInput() {
        // Given
        CreateTopicRequest request = new CreateTopicRequest();
        request.setName("test-topic");
        request.setPartitions(3);
        request.setReplicationFactor((short) 1);
        request.setConfigs(Map.of("cleanup.policy", "delete"));

        // When
        Set<ConstraintViolation<CreateTopicRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testCreateTopicRequestBlankName() {
        // Given
        CreateTopicRequest request = new CreateTopicRequest();
        request.setName("");
        request.setPartitions(3);
        request.setReplicationFactor((short) 1);

        // When
        Set<ConstraintViolation<CreateTopicRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<CreateTopicRequest> violation = violations.iterator().next();
        assertEquals("Topic name is required", violation.getMessage());
        assertEquals("name", violation.getPropertyPath().toString());
    }

    @Test
    void testCreateTopicRequestNullName() {
        // Given
        CreateTopicRequest request = new CreateTopicRequest();
        request.setName(null);
        request.setPartitions(3);
        request.setReplicationFactor((short) 1);

        // When
        Set<ConstraintViolation<CreateTopicRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<CreateTopicRequest> violation = violations.iterator().next();
        assertEquals("Topic name is required", violation.getMessage());
    }

    @Test
    void testCreateTopicRequestNullPartitions() {
        // Given
        CreateTopicRequest request = new CreateTopicRequest();
        request.setName("test-topic");
        request.setPartitions(null);
        request.setReplicationFactor((short) 1);

        // When
        Set<ConstraintViolation<CreateTopicRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<CreateTopicRequest> violation = violations.iterator().next();
        assertEquals("Partitions is required", violation.getMessage());
    }

    @Test
    void testCreateTopicRequestZeroPartitions() {
        // Given
        CreateTopicRequest request = new CreateTopicRequest();
        request.setName("test-topic");
        request.setPartitions(0);
        request.setReplicationFactor((short) 1);

        // When
        Set<ConstraintViolation<CreateTopicRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<CreateTopicRequest> violation = violations.iterator().next();
        assertEquals("At least 1 partition is required", violation.getMessage());
    }

    @Test
    void testCreateTopicRequestNullReplicationFactor() {
        // Given
        CreateTopicRequest request = new CreateTopicRequest();
        request.setName("test-topic");
        request.setPartitions(3);
        request.setReplicationFactor(null);

        // When
        Set<ConstraintViolation<CreateTopicRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<CreateTopicRequest> violation = violations.iterator().next();
        assertEquals("Replication factor is required", violation.getMessage());
    }

    @Test
    void testCreateTopicRequestZeroReplicationFactor() {
        // Given
        CreateTopicRequest request = new CreateTopicRequest();
        request.setName("test-topic");
        request.setPartitions(3);
        request.setReplicationFactor((short) 0);

        // When
        Set<ConstraintViolation<CreateTopicRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<CreateTopicRequest> violation = violations.iterator().next();
        assertEquals("Replication factor must be at least 1", violation.getMessage());
    }

    @Test
    void testCreateTopicRequestMultipleViolations() {
        // Given
        CreateTopicRequest request = new CreateTopicRequest();
        request.setName("");
        request.setPartitions(0);
        request.setReplicationFactor((short) 0);

        // When
        Set<ConstraintViolation<CreateTopicRequest>> violations = validator.validate(request);

        // Then
        assertEquals(3, violations.size());
        
        List<String> messages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .sorted()
                .toList();
        
        assertTrue(messages.contains("Topic name is required"));
        assertTrue(messages.contains("At least 1 partition is required"));
        assertTrue(messages.contains("Replication factor must be at least 1"));
    }

    @Test
    void testGrantProducerAclRequestValidInput() {
        // Given
        GrantProducerAclRequest request = new GrantProducerAclRequest();
        request.setTopic("test-topic");
        request.setTransactionId("tx-123");

        // When
        Set<ConstraintViolation<GrantProducerAclRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testGrantConsumerAclRequestValidInput() {
        // Given
        GrantConsumerAclRequest request = new GrantConsumerAclRequest();
        request.setTopic("test-topic");
        request.setGroup("consumer-group");

        // When
        Set<ConstraintViolation<GrantConsumerAclRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testCreateUserRequestValidInput() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setPassword("password");
        request.setMechanism("SCRAM-SHA-512");

        // When
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testCreateAclRequestValidInput() {
        // Given
        CreateAclRequest request = new CreateAclRequest();
        request.setResourceType("TOPIC");
        request.setResourceName("test-topic");
        request.setPrincipal("User:testuser");
        request.setHost("*");
        request.setOperation("READ");
        request.setPermission("ALLOW");

        // When
        Set<ConstraintViolation<CreateAclRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testCreateClusterLinkRequestValidInput() {
        // Given
        CreateClusterLinkRequest request = new CreateClusterLinkRequest();
        request.setLinkName("link-1");
        request.setSourceBootstrapServers("source:9092");

        // When
        Set<ConstraintViolation<CreateClusterLinkRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testCreateMirrorTopicsRequestValidInput() {
        // Given
        CreateMirrorTopicsRequest request = new CreateMirrorTopicsRequest();
        request.setTopics(List.of("topic1", "topic2"));

        // When
        Set<ConstraintViolation<CreateMirrorTopicsRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testFailoverRequestValidInput() {
        // Given
        FailoverRequest request = new FailoverRequest();
        request.setPrimaryClusterId("cluster-1");

        // When
        Set<ConstraintViolation<FailoverRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }
}
