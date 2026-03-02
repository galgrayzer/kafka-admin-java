package com.kafka.admin.controller;

import com.kafka.admin.model.request.FetchMessagesRequest;
import com.kafka.admin.model.request.ProduceMessagesRequest;
import com.kafka.admin.model.response.ApiResponse;
import com.kafka.admin.model.response.ConsumerOffsetResponse;
import com.kafka.admin.model.response.MessageResponse;
import com.kafka.admin.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
@Tag(name = "Messages", description = "Kafka message operations")
public class MessageController {

    private final MessageService messageService;
    private final RequestContextExtractor contextExtractor;

    public MessageController(MessageService messageService, RequestContextExtractor contextExtractor) {
        this.messageService = messageService;
        this.contextExtractor = contextExtractor;
    }

    @GetMapping("/topic/{topicName}/offsets")
    @Operation(summary = "Get topic offsets", description = "Get current offsets for a topic")
    public List<ConsumerOffsetResponse> getTopicOffsets(
            @Parameter(description = "Topic name") @PathVariable String topicName,
            @Parameter(description = "Bootstrap servers") @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {

        var ctx = contextExtractor.extract(request);
        return messageService.getTopicOffsets(topicName, ctx.bootstrapServers(), ctx.securityProtocol(),
                ctx.username(), ctx.password(), ctx.saslMechanism());
    }

    @PostMapping("/fetch")
    @Operation(summary = "Fetch messages", description = "Fetch messages from a topic")
    public List<MessageResponse> fetchMessages(
            @Valid @RequestBody FetchMessagesRequest fetchRequest,
            @Parameter(description = "Bootstrap servers") @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {

        var ctx = contextExtractor.extract(request);
        return messageService.fetchMessages(fetchRequest, ctx.bootstrapServers(), ctx.securityProtocol(),
                ctx.username(), ctx.password(), ctx.saslMechanism());
    }

    @PostMapping("/produce")
    @Operation(summary = "Produce messages", description = "Produce messages to a topic")
    public ApiResponse produceMessages(
            @Valid @RequestBody ProduceMessagesRequest produceRequest,
            @Parameter(description = "Bootstrap servers") @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {

        var ctx = contextExtractor.extract(request);
        int count = messageService.produceMessages(produceRequest, ctx.bootstrapServers(), ctx.securityProtocol(),
                ctx.username(), ctx.password(), ctx.saslMechanism());
        return ApiResponse.success("Produced " + count + " messages successfully");
    }
}
