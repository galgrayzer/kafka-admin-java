package com.kafka.admin.model.request;

import jakarta.validation.constraints.NotBlank;

public class CopyConsumerOffsetsRequest {

    @NotBlank(message = "Source group is required")
    private String sourceGroup;

    @NotBlank(message = "Topic is required")
    private String topic;

    public String getSourceGroup() {
        return sourceGroup;
    }

    public void setSourceGroup(String sourceGroup) {
        this.sourceGroup = sourceGroup;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
