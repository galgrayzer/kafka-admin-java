package com.kafka.admin.model.request;

import jakarta.validation.constraints.NotBlank;

public class ResetConsumerOffsetsRequest {

    @NotBlank(message = "Topic is required")
    private String topic;

    private Integer partition;

    private String offset;

    private String resetStrategy;

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

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    public String getResetStrategy() {
        return resetStrategy;
    }

    public void setResetStrategy(String resetStrategy) {
        this.resetStrategy = resetStrategy;
    }
}
