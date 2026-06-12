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
}
