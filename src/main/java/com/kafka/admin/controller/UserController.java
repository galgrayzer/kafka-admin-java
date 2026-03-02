package com.kafka.admin.controller;

import com.kafka.admin.model.request.CreateUserRequest;
import com.kafka.admin.model.response.ApiResponse;
import com.kafka.admin.model.response.UserResponse;
import com.kafka.admin.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "SCRAM user management operations")
public class UserController {

    private final UserService userService;
    private final RequestContextExtractor contextExtractor;

    public UserController(UserService userService, RequestContextExtractor contextExtractor) {
        this.userService = userService;
        this.contextExtractor = contextExtractor;
    }

    @GetMapping
    @Operation(summary = "List all users", description = "Get a list of all SCRAM users in the Kafka cluster")
    public List<UserResponse> listUsers(
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "broker1:9092,broker2:9092")
            @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {
        
        var ctx = contextExtractor.extract(request);
        return userService.listUsers(ctx.bootstrapServers(), ctx.securityProtocol(), 
                ctx.username(), ctx.password(), ctx.saslMechanism());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new user", description = "Create a new SCRAM user")
    public ApiResponse createUser(
            @Valid @RequestBody CreateUserRequest createRequest,
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "broker1:9092,broker2:9092")
            @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {
        
        var ctx = contextExtractor.extract(request);
        userService.createUser(createRequest, ctx.bootstrapServers(), ctx.securityProtocol(), 
                ctx.username(), ctx.password(), ctx.saslMechanism());
        return ApiResponse.success("User created successfully", createRequest.getUsername());
    }

    @DeleteMapping("/{username}")
    @Operation(summary = "Delete a user", description = "Delete an existing SCRAM user")
    public ApiResponse deleteUser(
            @Parameter(description = "Username") @PathVariable String username,
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "broker1:9092,broker2:9092")
            @RequestParam(required = false) String bootstrapServers,
            HttpServletRequest request) throws Exception {
        
        var ctx = contextExtractor.extract(request);
        userService.deleteUser(username, ctx.bootstrapServers(), ctx.securityProtocol(), 
                ctx.username(), ctx.password(), ctx.saslMechanism());
        return ApiResponse.success("User deleted successfully", username);
    }
}
