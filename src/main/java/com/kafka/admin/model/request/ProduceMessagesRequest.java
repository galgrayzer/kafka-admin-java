package com.kafka.admin.model.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

public class ProduceMessagesRequest {

    @NotBlank(message = "Topic is required")
    private String topic;

    private Integer partition;

    private List<ProducerRecord> records;

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

    public List<ProducerRecord> getRecords() {
        return records;
    }

    public void setRecords(List<ProducerRecord> records) {
        this.records = records;
    }

    public static class ProducerRecord {
        private String key;
        private String value;
        private Long timestamp;
        private Map<String, String> headers;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }
    }
}
