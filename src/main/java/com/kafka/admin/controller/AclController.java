package com.kafka.admin.controller;

import com.kafka.admin.model.request.*;
import com.kafka.admin.model.response.AclResponse;
import com.kafka.admin.model.response.ApiResponse;
import com.kafka.admin.service.AclService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/acls")
@Tag(name = "ACLs", description = "Kafka ACL management operations")
public class AclController {

    private final AclService aclService;
    private final RequestContextExtractor contextExtractor;

    public AclController(AclService aclService, RequestContextExtractor contextExtractor) {
        this.aclService = aclService;
        this.contextExtractor = contextExtractor;
    }

    @GetMapping
    @Operation(summary = "List all ACLs", description = "Get a list of all ACLs in the Kafka cluster")
    public List<AclResponse> listAcls(
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "broker1:9092,broker2:9092")
            @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {
        
        var ctx = contextExtractor.extract(request);
        return aclService.listAcls(ctx.bootstrapServers(), ctx.securityProtocol(), 
                ctx.username(), ctx.password(), ctx.saslMechanism());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an ACL", description = "Create a new ACL")
    public ApiResponse createAcl(
            @Valid @RequestBody CreateAclRequest createRequest,
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "broker1:9092,broker2:9092")
            @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {
        
        var ctx = contextExtractor.extract(request);
        aclService.createAcl(createRequest, ctx.bootstrapServers(), ctx.securityProtocol(), 
                ctx.username(), ctx.password(), ctx.saslMechanism());
        return ApiResponse.success("ACL created successfully");
    }

    @DeleteMapping
    @Operation(summary = "Delete an ACL", description = "Delete an ACL")
    public ApiResponse deleteAcl(
            @Parameter(description = "Resource type") @RequestParam String resourceType,
            @Parameter(description = "Resource name") @RequestParam String resourceName,
            @Parameter(description = "Principal") @RequestParam String principal,
            @Parameter(description = "Host") @RequestParam(required = false) String host,
            @Parameter(description = "Operation") @RequestParam String operation,
            @Parameter(description = "Permission") @RequestParam String permission,
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "broker1:9092,broker2:9092")
            @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {
        
        var ctx = contextExtractor.extract(request);
        aclService.deleteAcl(resourceType, resourceName, principal, host, operation, permission,
                ctx.bootstrapServers(), ctx.securityProtocol(), 
                ctx.username(), ctx.password(), ctx.saslMechanism());
        return ApiResponse.success("ACL deleted successfully");
    }

    @PostMapping("/user/{username}/consumer")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Grant consumer ACL", description = "Grant consumer privileges (READ, DESCRIBE on topic, READ on group)")
    public ApiResponse grantConsumerAcl(
            @Parameter(description = "Username") @PathVariable String username,
            @Valid @RequestBody GrantConsumerAclRequest aclRequest,
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "broker1:9092,broker2:9092")
            @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {
        
        var ctx = contextExtractor.extract(request);
        aclService.grantConsumerAcl(username, aclRequest, ctx.bootstrapServers(), ctx.securityProtocol(), 
                ctx.username(), ctx.password(), ctx.saslMechanism());
        return ApiResponse.success("Consumer ACL granted successfully", Map.of("username", username, "topic", aclRequest.getTopic()));
    }

    @PostMapping("/user/{username}/producer")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Grant producer ACL", description = "Grant producer privileges (WRITE, DESCRIBE on topic)")
    public ApiResponse grantProducerAcl(
            @Parameter(description = "Username") @PathVariable String username,
            @Valid @RequestBody GrantProducerAclRequest aclRequest,
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "broker1:9092,broker2:9092")
            @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {
        
        var ctx = contextExtractor.extract(request);
        aclService.grantProducerAcl(username, aclRequest, ctx.bootstrapServers(), ctx.securityProtocol(), 
                ctx.username(), ctx.password(), ctx.saslMechanism());
        return ApiResponse.success("Producer ACL granted successfully", Map.of("username", username, "topic", aclRequest.getTopic()));
    }

    @GetMapping("/user/{username}/consumer/check")
    @Operation(summary = "Check consumer ACL", description = "Check if user has consumer privileges on a topic")
    public ApiResponse checkConsumerAcl(
            @Parameter(description = "Username") @PathVariable String username,
            @Parameter(description = "Topic name") @RequestParam String topic,
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "broker1:9092,broker2:9092")
            @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {
        
        var ctx = contextExtractor.extract(request);
        boolean hasAcl = aclService.checkConsumerAcl(username, topic, ctx.bootstrapServers(), ctx.securityProtocol(), 
                ctx.username(), ctx.password(), ctx.saslMechanism());
        return ApiResponse.success(hasAcl ? "User has consumer ACL" : "User does not have consumer ACL", hasAcl);
    }

    @GetMapping("/user/{username}/producer/check")
    @Operation(summary = "Check producer ACL", description = "Check if user has producer privileges on a topic")
    public ApiResponse checkProducerAcl(
            @Parameter(description = "Username") @PathVariable String username,
            @Parameter(description = "Topic name") @RequestParam String topic,
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "broker1:9092,broker2:9092")
            @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {
        
        var ctx = contextExtractor.extract(request);
        boolean hasAcl = aclService.checkProducerAcl(username, topic, ctx.bootstrapServers(), ctx.securityProtocol(), 
                ctx.username(), ctx.password(), ctx.saslMechanism());
        return ApiResponse.success(hasAcl ? "User has producer ACL" : "User does not have producer ACL", hasAcl);
    }

    @DeleteMapping("/user/{username}/consumer")
    @Operation(summary = "Revoke consumer ACL", description = "Revoke consumer privileges (READ, DESCRIBE on topic, DESCRIBE, READ on group)")
    public ApiResponse revokeConsumerAcl(
            @Parameter(description = "Username") @PathVariable String username,
            @Valid @RequestBody GrantConsumerAclRequest aclRequest,
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "broker1:9092,broker2:9092")
            @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {
        
        var ctx = contextExtractor.extract(request);
        aclService.revokeConsumerAcl(username, aclRequest, ctx.bootstrapServers(), ctx.securityProtocol(), 
                ctx.username(), ctx.password(), ctx.saslMechanism());
        return ApiResponse.success("Consumer ACL revoked successfully", Map.of("username", username, "topic", aclRequest.getTopic()));
    }

    @DeleteMapping("/user/{username}/producer")
    @Operation(summary = "Revoke producer ACLRevoke producer privileges", description = " (WRITE, DESCRIBE on topic, WRITE on transaction ID)")
    public ApiResponse revokeProducerAcl(
            @Parameter(description = "Username") @PathVariable String username,
            @Valid @RequestBody GrantProducerAclRequest aclRequest,
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "broker1:9092,broker2:9092")
            @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {
        
        var ctx = contextExtractor.extract(request);
        aclService.revokeProducerAcl(username, aclRequest, ctx.bootstrapServers(), ctx.securityProtocol(), 
                ctx.username(), ctx.password(), ctx.saslMechanism());
        return ApiResponse.success("Producer ACL revoked successfully", Map.of("username", username, "topic", aclRequest.getTopic()));
    }
}
