package com.kafka.admin.exception;

import com.kafka.admin.model.response.ApiResponse;
import org.apache.kafka.common.errors.*;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class KafkaAdminExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(KafkaAdminExceptionHandler.class);

    @ExceptionHandler(TopicExistsException.class)
    public ResponseEntity<ApiResponse> handleTopicExistsException(TopicExistsException ex) {
        log.warn("Topic already exists: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Topic already exists: " + ex.getMessage()));
    }

    @ExceptionHandler(UnknownTopicOrPartitionException.class)
    public ResponseEntity<ApiResponse> handleUnknownTopicOrPartitionException(UnknownTopicOrPartitionException ex) {
        log.warn("Topic not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Topic not found: " + ex.getMessage()));
    }

    @ExceptionHandler(ClusterAuthorizationException.class)
    public ResponseEntity<ApiResponse> handleClusterAuthorizationException(ClusterAuthorizationException ex) {
        log.warn("Authorization denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Authorization denied: " + ex.getMessage()));
    }

    @ExceptionHandler(SecurityDisabledException.class)
    public ResponseEntity<ApiResponse> handleSecurityDisabledException(SecurityDisabledException ex) {
        log.warn("Security disabled: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Security is not enabled: " + ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Invalid argument: " + ex.getMessage()));
    }

    @ExceptionHandler(GroupNotEmptyException.class)
    public ResponseEntity<ApiResponse> handleGroupNotEmptyException(GroupNotEmptyException ex) {
        log.warn("Group not empty: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Cannot reset offsets: Consumer group has active consumers. " +
                        "Please stop all consumers in the group before resetting offsets. Details: " + ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        log.warn("Validation failed: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
    }

    @ExceptionHandler(ExecutionException.class)
    public ResponseEntity<ApiResponse> handleExecutionException(ExecutionException ex) {
        log.error("Kafka operation failed", ex);
        String message = "Operation failed: " + ex.getMessage();
        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            message += " - " + ex.getCause().getMessage();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        String message = "Unexpected error: " + ex.getMessage();
        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            message += " - " + ex.getCause().getMessage();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
    }
}
