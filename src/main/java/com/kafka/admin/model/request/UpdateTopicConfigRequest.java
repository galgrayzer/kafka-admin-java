package com.kafka.admin.model.request;

import java.util.Map;

public class UpdateTopicConfigRequest {

    private Map<String, String> configs;

    public Map<String, String> getConfigs() {
        return configs;
    }

    public void setConfigs(Map<String, String> configs) {
        this.configs = configs;
    }
}
