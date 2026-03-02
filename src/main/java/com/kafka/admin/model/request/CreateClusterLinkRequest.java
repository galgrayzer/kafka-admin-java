package com.kafka.admin.model.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public class CreateClusterLinkRequest {

    @NotBlank(message = "Link name is required")
    private String linkName;

    @NotBlank(message = "Source cluster bootstrap servers is required")
    private String sourceBootstrapServers;

    private String sourceSecurityProtocol;

    private String sourceUsername;

    private String sourcePassword;

    private Map<String, String> configs;

    public String getLinkName() {
        return linkName;
    }

    public void setLinkName(String linkName) {
        this.linkName = linkName;
    }

    public String getSourceBootstrapServers() {
        return sourceBootstrapServers;
    }

    public void setSourceBootstrapServers(String sourceBootstrapServers) {
        this.sourceBootstrapServers = sourceBootstrapServers;
    }

    public String getSourceSecurityProtocol() {
        return sourceSecurityProtocol;
    }

    public void setSourceSecurityProtocol(String sourceSecurityProtocol) {
        this.sourceSecurityProtocol = sourceSecurityProtocol;
    }

    public String getSourceUsername() {
        return sourceUsername;
    }

    public void setSourceUsername(String sourceUsername) {
        this.sourceUsername = sourceUsername;
    }

    public String getSourcePassword() {
        return sourcePassword;
    }

    public void setSourcePassword(String sourcePassword) {
        this.sourcePassword = sourcePassword;
    }

    public Map<String, String> getConfigs() {
        return configs;
    }

    public void setConfigs(Map<String, String> configs) {
        this.configs = configs;
    }
}
