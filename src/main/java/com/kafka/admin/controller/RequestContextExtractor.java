package com.kafka.admin.controller;

import com.kafka.admin.config.KafkaAdminConfig;
import com.kafka.admin.constants.AdminConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class RequestContextExtractor {

    private final KafkaAdminConfig config;

    public RequestContextExtractor(KafkaAdminConfig config) {
        this.config = config;
    }

    public record KafkaSecurityContext(
            String bootstrapServers,
            String securityProtocol,
            String username,
            String password,
            String saslMechanism) {}

    public KafkaSecurityContext extract(HttpServletRequest request) {
        String bootstrapServers = resolveWithPriority(
                request,
                AdminConstants.PARAM_BOOTSTRAP_SERVERS,
                AdminConstants.HEADER_BOOTSTRAP_SERVERS,
                AdminConstants.ENV_BOOTSTRAP_SERVERS,
                null);

        if (bootstrapServers == null || bootstrapServers.isBlank()) {
            throw new IllegalArgumentException("bootstrapServers is required");
        }

        String securityProtocol = resolveWithPriority(
                request,
                null,
                AdminConstants.HEADER_SECURITY_PROTOCOL,
                AdminConstants.ENV_SECURITY_PROTOCOL,
                config.getDefaultSecurityProtocol());

        String username = resolveWithPriority(
                request,
                null,
                AdminConstants.HEADER_USERNAME,
                AdminConstants.ENV_USERNAME,
                config.getDefaultUsername());

        String password = resolveWithPriority(
                request,
                null,
                AdminConstants.HEADER_PASSWORD,
                AdminConstants.ENV_PASSWORD,
                config.getDefaultPassword());

        String saslMechanism = resolveWithPriority(
                request,
                null,
                AdminConstants.HEADER_SASL_MECHANISM,
                AdminConstants.ENV_SASL_MECHANISM,
                config.getDefaultSaslMechanism());

        return new KafkaSecurityContext(
                bootstrapServers,
                securityProtocol,
                username,
                password,
                saslMechanism);
    }

    private String resolveWithPriority(HttpServletRequest request, String paramName, String headerName, String envName, String defaultValue) {
        if (paramName != null) {
            String paramValue = request.getParameter(paramName);
            if (paramValue != null && !paramValue.isBlank()) {
                return paramValue;
            }
        }

        if (headerName != null) {
            String headerValue = request.getHeader(headerName);
            if (headerValue != null && !headerValue.isBlank()) {
                return headerValue;
            }
        }

        if (envName != null) {
            String envValue = System.getenv(envName);
            if (envValue != null && !envValue.isBlank()) {
                return envValue;
            }
        }

        return defaultValue;
    }
}
