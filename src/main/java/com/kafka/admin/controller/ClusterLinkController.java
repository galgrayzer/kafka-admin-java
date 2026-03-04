package com.kafka.admin.controller;

import com.kafka.admin.model.request.*;
import com.kafka.admin.model.response.ApiResponse;
import com.kafka.admin.model.response.ClusterLinkResponse;
import com.kafka.admin.service.ClusterLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cluster-links")
@Tag(name = "Cluster Links", description = "Kafka cluster linking operations")
public class ClusterLinkController {

    private final ClusterLinkService clusterLinkService;
    private final RequestContextExtractor contextExtractor;

    public ClusterLinkController(ClusterLinkService clusterLinkService, RequestContextExtractor contextExtractor) {
        this.clusterLinkService = clusterLinkService;
        this.contextExtractor = contextExtractor;
    }

    @GetMapping
    @Operation(summary = "List all cluster links", description = "Get a list of all cluster links")
    public List<ClusterLinkResponse> listClusterLinks(
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "broker1:9092,broker2:9092")
            @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {
        
        var ctx = contextExtractor.extract(request);
        return clusterLinkService.listClusterLinks(ctx.bootstrapServers(), ctx.securityProtocol(), 
                ctx.username(), ctx.password(), ctx.saslMechanism());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a cluster link", description = "Create a new cluster link")
    public ApiResponse createClusterLink(
            @Valid @RequestBody CreateClusterLinkRequest createRequest,
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "broker1:9092,broker2:9092")
            @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {
        
        var ctx = contextExtractor.extract(request);
        clusterLinkService.createClusterLink(createRequest, ctx.bootstrapServers(), ctx.securityProtocol(), 
                ctx.username(), ctx.password(), ctx.saslMechanism());
        return ApiResponse.success("Cluster link created successfully", createRequest.getLinkName());
    }

    @DeleteMapping("/{linkName}")
    @Operation(summary = "Delete a cluster link", description = "Delete an existing cluster link")
    public ApiResponse deleteClusterLink(
            @Parameter(description = "Link name") @PathVariable String linkName,
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "broker1:9092,broker2:9092")
            @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {
        
        var ctx = contextExtractor.extract(request);
        clusterLinkService.deleteClusterLink(linkName, ctx.bootstrapServers(), ctx.securityProtocol(), 
                ctx.username(), ctx.password(), ctx.saslMechanism());
        return ApiResponse.success("Cluster link deleted successfully", linkName);
    }

    @PostMapping("/{linkName}/mirror-topics")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create mirror topics", description = "Create mirror topics on the cluster link")
    public ApiResponse createMirrorTopics(
            @Parameter(description = "Link name") @PathVariable String linkName,
            @Valid @RequestBody CreateMirrorTopicsRequest request,
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "broker1:9092,broker2:9092")
            @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest httpRequest) throws Exception {
        
        var ctx = contextExtractor.extract(httpRequest);
        clusterLinkService.createMirrorTopics(linkName, request, ctx.bootstrapServers(), ctx.securityProtocol(), 
                ctx.username(), ctx.password(), ctx.saslMechanism());
        return ApiResponse.success("Mirror topics created successfully", request.getTopics());
    }

    @PostMapping("/{linkName}/topics/{topicName}/reverse-and-start")
    @Operation(summary = "Reverse and start", description = "Reverse replication direction and start for a specific topic")
    public ApiResponse reverseAndStart(
            @Parameter(description = "Link name") @PathVariable String linkName,
            @Parameter(description = "Topic name") @PathVariable String topicName,
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "broker1:9092,broker2:9092")
            @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {
        
        var ctx = contextExtractor.extract(request);
        clusterLinkService.reverseAndStart(linkName, topicName, ctx.bootstrapServers(), ctx.securityProtocol(), 
                ctx.username(), ctx.password(), ctx.saslMechanism());
        return ApiResponse.success("Reverse and start completed", topicName);
    }

    @PostMapping("/{linkName}/topics/{topicName}/truncate-and-restore")
    @Operation(summary = "Truncate and restore", description = "Truncate and restore a specific topic")
    public ApiResponse truncateAndRestore(
            @Parameter(description = "Link name") @PathVariable String linkName,
            @Parameter(description = "Topic name") @PathVariable String topicName,
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "broker1:9092,broker2:9092")
            @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {
        
        var ctx = contextExtractor.extract(request);
        clusterLinkService.truncateAndRestore(linkName, topicName, ctx.bootstrapServers(), ctx.securityProtocol(), 
                ctx.username(), ctx.password(), ctx.saslMechanism());
        return ApiResponse.success("Truncate and restore completed", topicName);
    }

    @PostMapping("/{linkName}/topics/{topicName}/failover")
    @Operation(summary = "Failover", description = "Failover to mirror cluster for a specific topic")
    public ApiResponse failover(
            @Parameter(description = "Link name") @PathVariable String linkName,
            @Parameter(description = "Topic name") @PathVariable String topicName,
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "broker1:9092,broker2:9092")
            @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest requestHttp) throws Exception {
        
        var ctx = contextExtractor.extract(requestHttp);
        clusterLinkService.failover(linkName, topicName, ctx.bootstrapServers(), ctx.securityProtocol(), 
                ctx.username(), ctx.password(), ctx.saslMechanism());
        return ApiResponse.success("Failover completed", topicName);
    }

    @PostMapping("/{linkName}/topics/{topicName}/promote")
    @Operation(summary = "Promote", description = "Promote mirror topic to primary")
    public ApiResponse promote(
            @Parameter(description = "Link name") @PathVariable String linkName,
            @Parameter(description = "Topic name") @PathVariable String topicName,
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "broker1:9092,broker2:9092")
            @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {
        
        var ctx = contextExtractor.extract(request);
        clusterLinkService.promote(linkName, topicName, ctx.bootstrapServers(), ctx.securityProtocol(), 
                ctx.username(), ctx.password(), ctx.saslMechanism());
        return ApiResponse.success("Promote completed", topicName);
    }
}
