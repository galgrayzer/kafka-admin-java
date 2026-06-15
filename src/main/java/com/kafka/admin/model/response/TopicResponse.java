package com.kafka.admin.model.response;

import java.util.List;
import java.util.Map;

public class TopicResponse {

    private String name;
    private int partitions;
    private short replicationFactor;
    private Map<String, String> configs;
    private List<PartitionReplica> partitionsReplicas;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPartitions() {
        return partitions;
    }

    public void setPartitions(int partitions) {
        this.partitions = partitions;
    }

    public short getReplicationFactor() {
        return replicationFactor;
    }

    public void setReplicationFactor(short replicationFactor) {
        this.replicationFactor = replicationFactor;
    }

    public Map<String, String> getConfigs() {
        return configs;
    }

    public void setConfigs(Map<String, String> configs) {
        this.configs = configs;
    }

    public List<PartitionReplica> getPartitionsReplicas() {
        return partitionsReplicas;
    }

    public void setPartitionsReplicas(List<PartitionReplica> partitionsReplicas) {
        this.partitionsReplicas = partitionsReplicas;
    }
}
