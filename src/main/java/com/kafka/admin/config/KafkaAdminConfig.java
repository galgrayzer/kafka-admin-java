package com.kafka.admin.config;

import com.kafka.admin.constants.AdminConstants;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "kafka-admin")
public class KafkaAdminConfig {

    private String defaultBootstrapServers = AdminConstants.DEFAULT_BOOTSTRAP_SERVERS;
    private String defaultSecurityProtocol = AdminConstants.DEFAULT_SECURITY_PROTOCOL;
    private String defaultUsername;
    private String defaultPassword;
    private String defaultSaslMechanism = AdminConstants.DEFAULT_SASL_MECHANISM;

    public String getDefaultBootstrapServers() {
        return defaultBootstrapServers;
    }

    public void setDefaultBootstrapServers(String defaultBootstrapServers) {
        this.defaultBootstrapServers = defaultBootstrapServers;
    }

    public String getDefaultSecurityProtocol() {
        return defaultSecurityProtocol;
    }

    public void setDefaultSecurityProtocol(String defaultSecurityProtocol) {
        this.defaultSecurityProtocol = defaultSecurityProtocol;
    }

    public String getDefaultUsername() {
        return defaultUsername;
    }

    public void setDefaultUsername(String defaultUsername) {
        this.defaultUsername = defaultUsername;
    }

    public String getDefaultPassword() {
        return defaultPassword;
    }

    public void setDefaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword;
    }

    public String getDefaultSaslMechanism() {
        return defaultSaslMechanism;
    }

    public void setDefaultSaslMechanism(String defaultSaslMechanism) {
        this.defaultSaslMechanism = defaultSaslMechanism;
    }
}
