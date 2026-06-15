package com.kafka.admin.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class UpdateTopicPartitionOffsetsRequest {

    @NotBlank(message = "Consumer group ID is required")
    private String groupId;

    @NotEmpty(message = "Partition offsets list cannot be empty")
    @Valid
    private List<PartitionOffsetRequest> partitionOffsets;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public List<PartitionOffsetRequest> getPartitionOffsets() {
        return partitionOffsets;
    }

    public void setPartitionOffsets(List<PartitionOffsetRequest> partitionOffsets) {
        this.partitionOffsets = partitionOffsets;
    }
}
