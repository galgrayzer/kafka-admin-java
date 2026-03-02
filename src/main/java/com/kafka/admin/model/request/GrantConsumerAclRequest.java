package com.kafka.admin.model.request;

import jakarta.validation.constraints.NotBlank;

public class GrantConsumerAclRequest {

    @NotBlank(message = "Topic is required")
    private String topic;

    private String group;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
