package com.kafka.admin.model.request;

import jakarta.validation.constraints.NotNull;

public class PartitionOffsetRequest {

    @NotNull(message = "Partition ID is required")
    private Integer partitionId;

    @NotNull(message = "Offset is required")
    private Long offset;

    public Integer getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(Integer partitionId) {
        this.partitionId = partitionId;
    }

    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }
}
