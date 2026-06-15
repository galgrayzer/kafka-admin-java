package com.kafka.admin.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ProduceMessagesRequest {

    @NotBlank(message = "Topic is required")
    private String topic;

    private Integer partition;

    @NotNull(message = "Records list is required")
    @NotEmpty(message = "Records list must not be empty")
    private List<MessageRecord> records;

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

    public List<MessageRecord> getRecords() {
        return records;
    }

    public void setRecords(List<MessageRecord> records) {
        this.records = records;
    }
}
