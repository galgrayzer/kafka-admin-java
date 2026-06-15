package com.kafka.admin.model.response;

public class MirrorTopicResponse {

    private String topicName;
    private String linkName;
    private String mirrorState;
    private String sourceTopicId;
    private int numPartitions;

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getLinkName() {
        return linkName;
    }

    public void setLinkName(String linkName) {
        this.linkName = linkName;
    }

    public String getMirrorState() {
        return mirrorState;
    }

    public void setMirrorState(String mirrorState) {
        this.mirrorState = mirrorState;
    }

    public String getSourceTopicId() {
        return sourceTopicId;
    }

    public void setSourceTopicId(String sourceTopicId) {
        this.sourceTopicId = sourceTopicId;
    }

    public int getNumPartitions() {
        return numPartitions;
    }

    public void setNumPartitions(int numPartitions) {
        this.numPartitions = numPartitions;
    }
}
