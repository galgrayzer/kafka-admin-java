package com.kafka.admin.model.request;

import jakarta.validation.constraints.NotBlank;

public class GrantProducerAclRequest {

    @NotBlank(message = "Topic is required")
    private String topic;

    private String transactionId;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
