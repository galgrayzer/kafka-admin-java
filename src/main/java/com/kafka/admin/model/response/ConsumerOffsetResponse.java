package com.kafka.admin.model.response;

public class ConsumerOffsetResponse {

    private String topic;
    private Integer partition;
    private Long currentOffset;
    private Long logEndOffset;
    private Long lag;

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

    public Long getCurrentOffset() {
        return currentOffset;
    }

    public void setCurrentOffset(Long currentOffset) {
        this.currentOffset = currentOffset;
    }

    public Long getLogEndOffset() {
        return logEndOffset;
    }

    public void setLogEndOffset(Long logEndOffset) {
        this.logEndOffset = logEndOffset;
    }

    public Long getLag() {
        return lag;
    }

    public void setLag(Long lag) {
        this.lag = lag;
    }
}
