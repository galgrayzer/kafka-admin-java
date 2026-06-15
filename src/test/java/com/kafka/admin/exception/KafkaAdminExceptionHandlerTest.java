package com.kafka.admin.exception;

import com.kafka.admin.model.response.ApiResponse;
import org.apache.kafka.common.errors.*;
import org.apache.kafka.common.errors.GroupNotEmptyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KafkaAdminExceptionHandlerTest {

    private KafkaAdminExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new KafkaAdminExceptionHandler();
    }

    @Test
    void testHandleTopicExistsException() {
        // Given
        TopicExistsException ex = new TopicExistsException("Topic test-topic already exists");

        // When
        ResponseEntity<ApiResponse> response = exceptionHandler.handleTopicExistsException(ex);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Topic already exists"));
    }

    @Test
    void testHandleUnknownTopicOrPartitionException() {
        // Given
        UnknownTopicOrPartitionException ex = new UnknownTopicOrPartitionException("Topic test-topic not found");

        // When
        ResponseEntity<ApiResponse> response = exceptionHandler.handleUnknownTopicOrPartitionException(ex);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Topic not found"));
    }

    @Test
    void testHandleClusterAuthorizationException() {
        // Given
        ClusterAuthorizationException ex = new ClusterAuthorizationException("Not authorized");

        // When
        ResponseEntity<ApiResponse> response = exceptionHandler.handleClusterAuthorizationException(ex);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Authorization denied"));
    }

    @Test
    void testHandleSecurityDisabledException() {
        // Given
        SecurityDisabledException ex = new SecurityDisabledException("Security not enabled");

        // When
        ResponseEntity<ApiResponse> response = exceptionHandler.handleSecurityDisabledException(ex);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Security is not enabled"));
    }

    @Test
    void testHandleIllegalArgumentException() {
        // Given
        IllegalArgumentException ex = new IllegalArgumentException("Invalid username");

        // When
        ResponseEntity<ApiResponse> response = exceptionHandler.handleIllegalArgumentException(ex);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Invalid argument"));
    }

    @Test
    void testHandleGroupNotEmptyException() {
        // Given
        GroupNotEmptyException ex = new GroupNotEmptyException("Group test-group is not empty");

        // When
        ResponseEntity<ApiResponse> response = exceptionHandler.handleGroupNotEmptyException(ex);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Cannot reset offsets"));
        assertTrue(response.getBody().getMessage().contains("active consumers"));
    }

    @Test
    void testHandleValidationException() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "fieldName", "Field is required");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        // When
        ResponseEntity<ApiResponse> response = exceptionHandler.handleValidationException(ex);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("fieldName"));
    }

    @Test
    void testHandleValidationExceptionWithMultipleErrors() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError error1 = new FieldError("objectName", "field1", "Error 1");
        FieldError error2 = new FieldError("objectName", "field2", "Error 2");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(error1, error2));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        // When
        ResponseEntity<ApiResponse> response = exceptionHandler.handleValidationException(ex);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("field1"));
    }

    @Test
    void testHandleExecutionExceptionWithCause() {
        // Given
        RuntimeException cause = new RuntimeException("Connection refused");
        ExecutionException ex = new ExecutionException("Operation failed", cause);

        // When
        ResponseEntity<ApiResponse> response = exceptionHandler.handleExecutionException(ex);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Operation failed"));
        assertTrue(response.getBody().getMessage().contains("Connection refused"));
    }

    @Test
    void testHandleExecutionExceptionWithoutCause() {
        // Given
        ExecutionException ex = new ExecutionException("Operation failed", null);

        // When
        ResponseEntity<ApiResponse> response = exceptionHandler.handleExecutionException(ex);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Operation failed"));
    }

    @Test
    void testHandleGenericExceptionWithCause() {
        // Given
        RuntimeException cause = new RuntimeException("Root cause");
        Exception ex = new Exception("Unexpected error", cause);

        // When
        ResponseEntity<ApiResponse> response = exceptionHandler.handleGenericException(ex);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Unexpected error"));
        assertTrue(response.getBody().getMessage().contains("Root cause"));
    }

    @Test
    void testHandleGenericExceptionWithoutCause() {
        // Given
        Exception ex = new Exception("Something went wrong");

        // When
        ResponseEntity<ApiResponse> response = exceptionHandler.handleGenericException(ex);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Unexpected error"));
    }
}
