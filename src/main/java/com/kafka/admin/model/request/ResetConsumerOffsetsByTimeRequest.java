package com.kafka.admin.model.request;

import jakarta.validation.constraints.NotBlank;

public class ResetConsumerOffsetsByTimeRequest {

    @NotBlank(message = "Topic is required")
    private String topic;

    private Integer partition;

    @NotBlank(message = "Timestamp is required")
    private Long timestamp;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Integer getPartition() {
        return partition;
    }

    public void setPartition(Integer partition) {
        this.partition = partition;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
