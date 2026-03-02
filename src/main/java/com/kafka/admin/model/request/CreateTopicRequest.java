package com.kafka.admin.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.util.Map;

public class CreateTopicRequest {

    @NotBlank(message = "Topic name is required")
    private String name;

    @NotNull(message = "Partitions is required")
    @Min(value = 1, message = "At least 1 partition is required")
    private Integer partitions;

    @NotNull(message = "Replication factor is required")
    @Min(value = 1, message = "Replication factor must be at least 1")
    private Short replicationFactor;

    private Map<String, String> configs;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPartitions() {
        return partitions;
    }

    public void setPartitions(Integer partitions) {
        this.partitions = partitions;
    }

    public Short getReplicationFactor() {
        return replicationFactor;
    }

    public void setReplicationFactor(Short replicationFactor) {
        this.replicationFactor = replicationFactor;
    }

    public Map<String, String> getConfigs() {
        return configs;
    }

    public void setConfigs(Map<String, String> configs) {
        this.configs = configs;
    }
}
