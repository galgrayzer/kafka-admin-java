package com.kafka.admin.controller;

import com.kafka.admin.model.request.CreateTopicRequest;
import com.kafka.admin.model.request.UpdateTopicConfigRequest;
import com.kafka.admin.model.response.ApiResponse;
import com.kafka.admin.model.response.TopicResponse;
import com.kafka.admin.service.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/topics")
@Tag(name = "Topics", description = "Kafka topic management operations")
public class TopicController {

    private final TopicService topicService;
    private final RequestContextExtractor contextExtractor;

    public TopicController(TopicService topicService, RequestContextExtractor contextExtractor) {
        this.topicService = topicService;
        this.contextExtractor = contextExtractor;
    }

    @GetMapping
    @Operation(summary = "List all topics", description = "Get a list of all topics in the Kafka cluster")
    public List<TopicResponse> listTopics(
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "broker1:9092,broker2:9092")
            @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {
        
        var ctx = contextExtractor.extract(request);
        return topicService.listTopics(ctx.bootstrapServers(), ctx.securityProtocol(), 
                ctx.username(), ctx.password(), ctx.saslMechanism());
    }

    @GetMapping("/{topicName}")
    @Operation(summary = "Get topic details", description = "Get detailed information about a specific topic")
    public TopicResponse getTopic(
            @Parameter(description = "Topic name") @PathVariable String topicName,
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "broker1:9092,broker2:9092")
            @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {
        
        var ctx = contextExtractor.extract(request);
        return topicService.getTopic(topicName, ctx.bootstrapServers(), ctx.securityProtocol(), 
                ctx.username(), ctx.password(), ctx.saslMechanism());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new topic", description = "Create a new Kafka topic")
    public ApiResponse createTopic(
            @Valid @RequestBody CreateTopicRequest createRequest,
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "broker1:9092,broker2:9092")
            @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {
        
        var ctx = contextExtractor.extract(request);
        topicService.createTopic(createRequest, ctx.bootstrapServers(), ctx.securityProtocol(), 
                ctx.username(), ctx.password(), ctx.saslMechanism());
        return ApiResponse.success("Topic created successfully", createRequest.getName());
    }

    @DeleteMapping("/{topicName}")
    @Operation(summary = "Delete a topic", description = "Delete an existing Kafka topic")
    public ApiResponse deleteTopic(
            @Parameter(description = "Topic name") @PathVariable String topicName,
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "broker1:9092,broker2:9092")
            @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {
        
        var ctx = contextExtractor.extract(request);
        topicService.deleteTopic(topicName, ctx.bootstrapServers(), ctx.securityProtocol(), 
                ctx.username(), ctx.password(), ctx.saslMechanism());
        return ApiResponse.success("Topic deleted successfully", topicName);
    }

    @PatchMapping("/{topicName}")
    @Operation(summary = "Update topic configuration", description = "Update configuration for an existing topic")
    public ApiResponse updateTopicConfig(
            @Parameter(description = "Topic name") @PathVariable String topicName,
            @Valid @RequestBody UpdateTopicConfigRequest updateRequest,
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "broker1:9092,broker2:9092")
            @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {
        
        var ctx = contextExtractor.extract(request);
        topicService.updateTopicConfig(topicName, updateRequest, ctx.bootstrapServers(), ctx.securityProtocol(), 
                ctx.username(), ctx.password(), ctx.saslMechanism());
        return ApiResponse.success("Topic configuration updated successfully", topicName);
    }
}
