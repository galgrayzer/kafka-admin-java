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
import java.util.Map;

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
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "localhost:9092", required = true)
            @RequestParam(required = true) String bootstrapServers,
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
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "localhost:9092", required = true)
            @RequestParam(required = true) String bootstrapServers,
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
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "localhost:9092", required = true)
            @RequestParam(required = true) String bootstrapServers,
            HttpServletRequest request) throws Exception {
        
        var ctx = contextExtractor.extract(request);
        userService.deleteUser(username, ctx.bootstrapServers(), ctx.securityProtocol(), 
                ctx.username(), ctx.password(), ctx.saslMechanism());
        return ApiResponse.success("User deleted successfully", username);
    }

    @GetMapping("/{username}/validate")
    @Operation(summary = "Validate user exists", description = "Check if a SCRAM user exists")
    public ApiResponse validateUser(
            @Parameter(description = "Username") @PathVariable String username,
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "localhost:9092", required = true)
            @RequestParam(required = true) String bootstrapServers,
            HttpServletRequest request) throws Exception {
        
        var ctx = contextExtractor.extract(request);
        boolean exists = userService.userExists(username, ctx.bootstrapServers(), ctx.securityProtocol(), 
                ctx.username(), ctx.password(), ctx.saslMechanism());
        if (exists) {
            return ApiResponse.success("User exists", true);
        } else {
            return ApiResponse.success("User does not exist", false);
        }
    }

    @PostMapping("/authenticate")
    @Operation(summary = "Check authentication", description = "Verify if a user can authenticate and determine their role (consumer/producer/both)")
    public ApiResponse checkAuthentication(
            @Parameter(description = "Username to check") @RequestParam String username,
            @Parameter(description = "Password to verify") @RequestParam String password,
            @Parameter(description = "Topic name to check permissions for") @RequestParam String topic,
            @Parameter(description = "Bootstrap servers (comma-separated)", example = "localhost:9092", required = true)
            @RequestParam(required = true) String bootstrapServers,
            HttpServletRequest request) throws Exception {

        var ctx = contextExtractor.extract(request);
        var result = userService.checkAuthentication(username, password, topic,
                ctx.bootstrapServers(), ctx.securityProtocol(),
                ctx.username(), ctx.password(), ctx.saslMechanism());
        return ApiResponse.success("Authentication check completed", result);
    }
}
