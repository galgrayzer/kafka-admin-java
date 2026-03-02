package com.kafka.admin.controller;

import com.kafka.admin.model.response.ApiResponse;
import com.kafka.admin.model.response.ClusterMetadataResponse;
import com.kafka.admin.service.ClusterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cluster")
@Tag(name = "Cluster", description = "Kafka cluster operations")
public class ClusterController {

    private final ClusterService clusterService;
    private final RequestContextExtractor contextExtractor;

    public ClusterController(ClusterService clusterService, RequestContextExtractor contextExtractor) {
        this.clusterService = clusterService;
        this.contextExtractor = contextExtractor;
    }

    @GetMapping("/metadata")
    @Operation(summary = "Get cluster metadata", description = "Get cluster information including brokers and topics")
    public ClusterMetadataResponse getClusterMetadata(
            @Parameter(description = "Bootstrap servers") @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {

        var ctx = contextExtractor.extract(request);
        return clusterService.getClusterMetadata(ctx.bootstrapServers(), ctx.securityProtocol(),
                ctx.username(), ctx.password(), ctx.saslMechanism());
    }

    @GetMapping("/topics")
    @Operation(summary = "List topics", description = "Get a list of all topic names in the cluster")
    public List<String> listTopics(
            @Parameter(description = "Bootstrap servers") @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {

        var ctx = contextExtractor.extract(request);
        return clusterService.listTopics(ctx.bootstrapServers(), ctx.securityProtocol(),
                ctx.username(), ctx.password(), ctx.saslMechanism());
    }
}
