package com.kafka.admin.controller;

import com.kafka.admin.constants.AdminConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RequestContextExtractor {

    @Value("${kafka-admin.default-bootstrap-servers:" + AdminConstants.DEFAULT_BOOTSTRAP_SERVERS + "}")
    private String defaultBootstrapServers;

    @Value("${kafka-admin.default-security-protocol:" + AdminConstants.DEFAULT_SECURITY_PROTOCOL + "}")
    private String defaultSecurityProtocol;

    @Value("${kafka-admin.default-sasl-mechanism:" + AdminConstants.DEFAULT_SASL_MECHANISM + "}")
    private String defaultSaslMechanism;

    public record KafkaSecurityContext(
            String bootstrapServers,
            String securityProtocol,
            String username,
            String password,
            String saslMechanism) {}

    public KafkaSecurityContext extract(HttpServletRequest request) {
        String bootstrapServers = getParamOrHeader(request, AdminConstants.PARAM_BOOTSTRAP_SERVERS, 
                AdminConstants.HEADER_SECURITY_PROTOCOL, defaultBootstrapServers);
        
        String securityProtocol = getHeaderOrDefault(request, AdminConstants.HEADER_SECURITY_PROTOCOL, 
                defaultSecurityProtocol);
        
        String username = request.getHeader(AdminConstants.HEADER_USERNAME);
        String password = request.getHeader(AdminConstants.HEADER_PASSWORD);
        String saslMechanism = getHeaderOrDefault(request, AdminConstants.HEADER_SASL_MECHANISM, 
                defaultSaslMechanism);

        return new KafkaSecurityContext(
                bootstrapServers,
                securityProtocol,
                username,
                password,
                saslMechanism
        );
    }

    private String getParamOrHeader(HttpServletRequest request, String paramName, String headerName, String defaultValue) {
        String paramValue = request.getParameter(paramName);
        if (paramValue != null && !paramValue.isBlank()) {
            return paramValue;
        }
        
        String headerValue = request.getHeader(headerName);
        if (headerValue != null && !headerValue.isBlank()) {
            return headerValue;
        }
        
        return defaultValue;
    }

    private String getHeaderOrDefault(HttpServletRequest request, String headerName, String defaultValue) {
        String headerValue = request.getHeader(headerName);
        return (headerValue != null && !headerValue.isBlank()) ? headerValue : defaultValue;
    }
}
