package com.kafka.admin.controller;

import com.kafka.admin.config.KafkaAdminConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestContextExtractorTest {

    private KafkaAdminConfig config;
    private RequestContextExtractor extractor;

    @BeforeEach
    void setUp() {
        config = new KafkaAdminConfig();
        config.setDefaultBootstrapServers("localhost:9092");
        config.setDefaultSecurityProtocol("PLAINTEXT");
        config.setDefaultSaslMechanism("PLAIN");
        extractor = new RequestContextExtractor(config);
    }

    @Test
    void testExtractWithRequestParameter() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("bootstrapServers")).thenReturn("remote:9093");
        when(request.getHeader(anyString())).thenReturn(null);

        // When
        var context = extractor.extract(request);

        // Then
        assertEquals("remote:9093", context.bootstrapServers());
        assertEquals("PLAINTEXT", context.securityProtocol());
    }

    @Test
    void testExtractWithHeaders() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter(anyString())).thenReturn(null);
        when(request.getHeader("X-Kafka-Bootstrap-Servers")).thenReturn("header-host:9092");
        when(request.getHeader("X-Kafka-Security-Protocol")).thenReturn("SASL_SSL");
        when(request.getHeader("X-Kafka-Username")).thenReturn("test-user");
        when(request.getHeader("X-Kafka-Password")).thenReturn("test-pass");
        when(request.getHeader("X-Kafka-Sasl-Mechanism")).thenReturn("SCRAM-SHA-512");

        // When
        var context = extractor.extract(request);

        // Then
        assertEquals("header-host:9092", context.bootstrapServers());
        assertEquals("SASL_SSL", context.securityProtocol());
        assertEquals("test-user", context.username());
        assertEquals("test-pass", context.password());
        assertEquals("SCRAM-SHA-512", context.saslMechanism());
    }

    @Test
    void testExtractWithDefaults() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter(anyString())).thenReturn(null);
        when(request.getHeader(anyString())).thenReturn(null);

        // Then — bootstrapServers is required; no param/header/env means fail-fast
        assertThrows(IllegalArgumentException.class, () -> extractor.extract(request));
    }

    @Test
    void testExtractThrowsWhenBootstrapServersMissing() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter(anyString())).thenReturn(null);
        when(request.getHeader(anyString())).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> extractor.extract(request));
    }

    @Test
    void testExtractParameterPriorityOverHeaders() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("bootstrapServers")).thenReturn("param-servers:9092");
        when(request.getHeader("bootstrapServers")).thenReturn("header-servers:9093");

        // When
        var context = extractor.extract(request);

        // Then
        assertEquals("param-servers:9092", context.bootstrapServers());
    }

    @Test
    void testExtractHeaderPriorityOverDefaults() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter(anyString())).thenReturn(null);
        when(request.getHeader("X-Kafka-Bootstrap-Servers")).thenReturn("localhost:9092");
        when(request.getHeader("X-Kafka-Security-Protocol")).thenReturn("SSL");

        // When
        var context = extractor.extract(request);

        // Then
        assertEquals("SSL", context.securityProtocol());
    }

    @Test
    void testExtractBlankParameterFallsBackToHeader() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("bootstrapServers")).thenReturn("   ");
        when(request.getHeader("X-Kafka-Bootstrap-Servers")).thenReturn("header-servers:9093");

        // When
        var context = extractor.extract(request);

        // Then
        assertEquals("header-servers:9093", context.bootstrapServers());
    }

    @Test
    void testExtractBlankHeaderFallsBackToDefault() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter(anyString())).thenReturn(null);
        when(request.getHeader("X-Kafka-Bootstrap-Servers")).thenReturn("localhost:9092");
        when(request.getHeader("X-Kafka-Security-Protocol")).thenReturn("   ");

        // When
        var context = extractor.extract(request);

        // Then
        assertEquals("PLAINTEXT", context.securityProtocol());
    }

    @Test
    void testKafkaSecurityContextRecord() {
        // When
        var context = new RequestContextExtractor.KafkaSecurityContext(
                "localhost:9092", "SASL_SSL", "user", "pass", "SCRAM-SHA-512");

        // Then
        assertEquals("localhost:9092", context.bootstrapServers());
        assertEquals("SASL_SSL", context.securityProtocol());
        assertEquals("user", context.username());
        assertEquals("pass", context.password());
        assertEquals("SCRAM-SHA-512", context.saslMechanism());
    }
}
