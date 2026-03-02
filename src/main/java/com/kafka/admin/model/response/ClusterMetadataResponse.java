package com.kafka.admin.model.response;

import java.util.List;

public class ClusterMetadataResponse {

    private String clusterId;
    private List<Broker> brokers;
    private List<TopicMetadata> topics;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public List<Broker> getBrokers() {
        return brokers;
    }

    public void setBrokers(List<Broker> brokers) {
        this.brokers = brokers;
    }

    public List<TopicMetadata> getTopics() {
        return topics;
    }

    public void setTopics(List<TopicMetadata> topics) {
        this.topics = topics;
    }

    public static class Broker {
        private Integer id;
        private String host;
        private Integer port;
        private String rack;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getRack() {
            return rack;
        }

        public void setRack(String rack) {
            this.rack = rack;
        }
    }

    public static class TopicMetadata {
        private String name;
        private Integer partitionCount;
        private Integer replicationFactor;
        private Boolean isInternal;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getPartitionCount() {
            return partitionCount;
        }

        public void setPartitionCount(Integer partitionCount) {
            this.partitionCount = partitionCount;
        }

        public Integer getReplicationFactor() {
            return replicationFactor;
        }

        public void setReplicationFactor(Integer replicationFactor) {
            this.replicationFactor = replicationFactor;
        }

        public Boolean getIsInternal() {
            return isInternal;
        }

        public void setIsInternal(Boolean isInternal) {
            this.isInternal = isInternal;
        }
    }
}
