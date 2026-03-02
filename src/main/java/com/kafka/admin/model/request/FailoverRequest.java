package com.kafka.admin.model.request;

import jakarta.validation.constraints.NotBlank;

public class FailoverRequest {

    @NotBlank(message = "Primary cluster ID is required")
    private String primaryClusterId;

    private String failoverMode;

    public String getPrimaryClusterId() {
        return primaryClusterId;
    }

    public void setPrimaryClusterId(String primaryClusterId) {
        this.primaryClusterId = primaryClusterId;
    }

    public String getFailoverMode() {
        return failoverMode;
    }

    public void setFailoverMode(String failoverMode) {
        this.failoverMode = failoverMode;
    }
}
