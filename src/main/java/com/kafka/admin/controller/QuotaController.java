package com.kafka.admin.controller;

import com.kafka.admin.model.request.CreateQuotaRequest;
import com.kafka.admin.model.response.ApiResponse;
import com.kafka.admin.model.response.QuotaResponse;
import com.kafka.admin.service.QuotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quotas")
@Tag(name = "Quotas", description = "Kafka quota management operations")
public class QuotaController {

    private final QuotaService quotaService;
    private final RequestContextExtractor contextExtractor;

    public QuotaController(QuotaService quotaService, RequestContextExtractor contextExtractor) {
        this.quotaService = quotaService;
        this.contextExtractor = contextExtractor;
    }

    @GetMapping
    @Operation(summary = "List all quotas", description = "Get a list of all quotas in the Kafka cluster")
    public List<QuotaResponse> listQuotas(
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "broker1:9092,broker2:9092")
            @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {
        
        var ctx = contextExtractor.extract(request);
        return quotaService.listQuotas(ctx.bootstrapServers(), ctx.securityProtocol(), 
                ctx.username(), ctx.password(), ctx.saslMechanism());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create or alter a quota", description = "Create or alter a quota for user/client")
    public ApiResponse createQuota(
            @Valid @RequestBody CreateQuotaRequest createRequest,
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "broker1:9092,broker2:9092")
            @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {
        
        var ctx = contextExtractor.extract(request);
        quotaService.createOrAlterQuota(createRequest, ctx.bootstrapServers(), ctx.securityProtocol(), 
                ctx.username(), ctx.password(), ctx.saslMechanism());
        return ApiResponse.success("Quota created/updated successfully");
    }

    @DeleteMapping
    @Operation(summary = "Delete a quota", description = "Delete a quota for user")
    public ApiResponse deleteQuota(
            @Parameter(description = "Username") @RequestParam String username,
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "broker1:9092,broker2:9092")
            @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {
        
        var ctx = contextExtractor.extract(request);
        quotaService.deleteQuota(username, ctx.bootstrapServers(), ctx.securityProtocol(), 
                ctx.username(), ctx.password(), ctx.saslMechanism());
        return ApiResponse.success("Quota deleted successfully");
    }

    @GetMapping("/user/{username}")
    @Operation(summary = "Get user quota", description = "Get quota for a specific user")
    public QuotaResponse getUserQuota(
            @Parameter(description = "Username") @PathVariable String username,
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "broker1:9092,broker2:9092")
            @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {
        
        var ctx = contextExtractor.extract(request);
        return quotaService.getUserQuota(username, ctx.bootstrapServers(), ctx.securityProtocol(), 
                ctx.username(), ctx.password(), ctx.saslMechanism());
    }
}
