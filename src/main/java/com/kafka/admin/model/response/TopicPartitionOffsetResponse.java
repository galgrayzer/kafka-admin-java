package com.kafka.admin.model.response;

public class TopicPartitionOffsetResponse {

    private String topic;
    private int partitionId;
    private long beginningOffset;
    private long endOffset;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(int partitionId) {
        this.partitionId = partitionId;
    }

    public long getBeginningOffset() {
        return beginningOffset;
    }

    public void setBeginningOffset(long beginningOffset) {
        this.beginningOffset = beginningOffset;
    }

    public long getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(long endOffset) {
        this.endOffset = endOffset;
    }
}
