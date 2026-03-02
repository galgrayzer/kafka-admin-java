package com.kafka.admin.model.request;

import jakarta.validation.constraints.NotBlank;

public class FetchMessagesRequest {

    @NotBlank(message = "Topic is required")
    private String topic;

    private Integer partition;

    private Long offset;

    private Long timestamp;

    private Integer maxMessages;

    private String startingPosition;

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

    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getMaxMessages() {
        return maxMessages;
    }

    public void setMaxMessages(Integer maxMessages) {
        this.maxMessages = maxMessages;
    }

    public String getStartingPosition() {
        return startingPosition;
    }

    public void setStartingPosition(String startingPosition) {
        this.startingPosition = startingPosition;
    }
}
