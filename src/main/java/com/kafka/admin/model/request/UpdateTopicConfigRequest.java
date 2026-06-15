package com.kafka.admin.model.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public class UpdateTopicConfigRequest {

    @NotNull(message = "Configs map is required")
    @NotEmpty(message = "Configs map must not be empty")
    private Map<String, String> configs;

    public Map<String, String> getConfigs() {
        return configs;
    }

    public void setConfigs(Map<String, String> configs) {
        this.configs = configs;
    }
}
