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

    public static class PartitionReplica {
        private int partitionId;
        private List<Integer> replicas;
        private List<Integer> isr;

        public int getPartitionId() {
            return partitionId;
        }

        public void setPartitionId(int partitionId) {
            this.partitionId = partitionId;
        }

        public List<Integer> getReplicas() {
            return replicas;
        }

        public void setReplicas(List<Integer> replicas) {
            this.replicas = replicas;
        }

        public List<Integer> getIsr() {
            return isr;
        }

        public void setIsr(List<Integer> isr) {
            this.isr = isr;
        }
    }
}
