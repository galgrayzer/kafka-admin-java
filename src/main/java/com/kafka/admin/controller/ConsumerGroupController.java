package com.kafka.admin.controller;

import com.kafka.admin.model.request.CopyConsumerOffsetsRequest;
import com.kafka.admin.model.request.ResetConsumerOffsetsByTimeRequest;
import com.kafka.admin.model.request.ResetConsumerOffsetsRequest;
import com.kafka.admin.model.response.ApiResponse;
import com.kafka.admin.model.response.ConsumerOffsetResponse;
import com.kafka.admin.service.ConsumerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/consumer-groups")
@Tag(name = "Consumer Groups", description = "Kafka consumer group operations")
public class ConsumerGroupController {

    private final ConsumerService consumerService;
    private final RequestContextExtractor contextExtractor;

    public ConsumerGroupController(ConsumerService consumerService, RequestContextExtractor contextExtractor) {
        this.consumerService = consumerService;
        this.contextExtractor = contextExtractor;
    }

    @GetMapping("/{groupId}/offsets")
    @Operation(summary = "Get consumer offsets", description = "Get current offsets for a consumer group")
    public List<ConsumerOffsetResponse> getConsumerOffsets(
            @Parameter(description = "Consumer group ID") @PathVariable String groupId,
            @Parameter(description = "Topic name (optional)") @RequestParam(required = false) String topic,
            @Parameter(description = "Bootstrap servers") @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {

        var ctx = contextExtractor.extract(request);
        return consumerService.getConsumerOffsets(groupId, topic, ctx.bootstrapServers(), ctx.securityProtocol(),
                ctx.username(), ctx.password(), ctx.saslMechanism());
    }

    @PostMapping("/{groupId}/offsets/reset")
    @Operation(summary = "Reset consumer offsets", description = "Reset offsets for a consumer group (earliest/latest/specific offset)")
    public ApiResponse resetConsumerOffsets(
            @Parameter(description = "Consumer group ID") @PathVariable String groupId,
            @Valid @RequestBody ResetConsumerOffsetsRequest resetRequest,
            @Parameter(description = "Bootstrap servers") @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {

        var ctx = contextExtractor.extract(request);
        consumerService.resetConsumerOffsets(groupId, resetRequest, ctx.bootstrapServers(), ctx.securityProtocol(),
                ctx.username(), ctx.password(), ctx.saslMechanism());
        return ApiResponse.success("Consumer offsets reset successfully");
    }

    @PostMapping("/{groupId}/offsets/reset-by-time")
    @Operation(summary = "Reset consumer offsets by timestamp", description = "Reset offsets to the offset of the first message at or after the given timestamp")
    public ApiResponse resetConsumerOffsetsByTimestamp(
            @Parameter(description = "Consumer group ID") @PathVariable String groupId,
            @Valid @RequestBody ResetConsumerOffsetsByTimeRequest resetRequest,
            @Parameter(description = "Bootstrap servers") @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {

        var ctx = contextExtractor.extract(request);
        consumerService.resetConsumerOffsetsByTimestamp(groupId, resetRequest, 
                ctx.bootstrapServers(), ctx.securityProtocol(),
                ctx.username(), ctx.password(), ctx.saslMechanism());
        return ApiResponse.success("Consumer offsets reset by timestamp successfully");
    }

    @PostMapping("/{groupId}/offsets/copy")
    @Operation(summary = "Copy consumer offsets", description = "Copy offsets from another group for a topic")
    public ApiResponse copyConsumerOffsets(
            @Parameter(description = "Consumer group ID") @PathVariable String groupId,
            @Valid @RequestBody CopyConsumerOffsetsRequest copyRequest,
            @Parameter(description = "Bootstrap servers") @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {

        var ctx = contextExtractor.extract(request);
        consumerService.copyConsumerOffsets(groupId, copyRequest, ctx.bootstrapServers(), ctx.securityProtocol(),
                ctx.username(), ctx.password(), ctx.saslMechanism());
        return ApiResponse.success("Consumer offsets copied successfully");
    }
}
