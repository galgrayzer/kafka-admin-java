package com.kafka.admin.service;

import com.kafka.admin.client.KafkaAdminClientFactory;
import com.kafka.admin.model.request.CreateUserRequest;
import com.kafka.admin.model.response.UserResponse;
import jakarta.annotation.Nullable;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.acl.AccessControlEntryFilter;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final KafkaAdminClientFactory adminClientFactory;

    public UserService(KafkaAdminClientFactory adminClientFactory) {
        this.adminClientFactory = adminClientFactory;
    }

    public List<UserResponse> listUsers(
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {

        log.debug("Listing users");
        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, username, password, saslMechanism)) {

            DescribeUserScramCredentialsResult result = admin.describeUserScramCredentials();
            Map<String, UserScramCredentialsDescription> users = result.all().get();

            return users.entrySet().stream()
                    .map(entry -> {
                        UserResponse response = new UserResponse();
                        response.setUsername(entry.getKey());
                        response.setMechanisms(entry.getValue().credentialInfos().stream()
                                .map(info -> info.mechanism().name())
                                .collect(Collectors.toList()));
                        return response;
                    })
                    .collect(Collectors.toList());
        }
    }

    public void createUser(
            CreateUserRequest request,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {

        log.info("Creating user: username={}", request.getUsername());
        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, username, password, saslMechanism)) {

            String mechanism = request.getMechanism() != null ? request.getMechanism() : "SCRAM-SHA-512";
            
            ScramMechanism scramMechanism;
            switch (mechanism.toUpperCase()) {
                case "SCRAM-SHA-256":
                    scramMechanism = ScramMechanism.SCRAM_SHA_256;
                    break;
                case "SCRAM-SHA-512":
                default:
                    scramMechanism = ScramMechanism.SCRAM_SHA_512;
                    break;
            }

            ScramCredentialInfo credInfo = new ScramCredentialInfo(scramMechanism, 4096);

            UserScramCredentialUpsertion upsertion = new UserScramCredentialUpsertion(
                    request.getUsername(), credInfo, request.getPassword() != null ? request.getPassword().getBytes() : null);

            admin.alterUserScramCredentials(List.of(upsertion)).all().get();
        }
    }

    public void deleteUser(
            String username,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String adminUsername,
            @Nullable String adminPassword,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {

        log.info("Deleting user: username={}", username);
        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, adminUsername, adminPassword, saslMechanism)) {

            DescribeUserScramCredentialsResult credResult = admin.describeUserScramCredentials(List.of(username));
            Map<String, UserScramCredentialsDescription> creds = credResult.all().get();
            UserScramCredentialsDescription userCreds = creds.get(username);
            if (userCreds == null || userCreds.credentialInfos().isEmpty()) {
                throw new IllegalArgumentException("No SCRAM credentials found for user: " + username);
            }
            ScramMechanism mechanism = userCreds.credentialInfos().get(0).mechanism();

            UserScramCredentialDeletion deletion = new UserScramCredentialDeletion(username, mechanism);
            admin.alterUserScramCredentials(List.of(deletion)).all().get();
        }
    }

    public boolean userExists(
            String username,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String adminUsername,
            @Nullable String adminPassword,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {

        log.debug("Checking user existence: username={}", username);
        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, adminUsername, adminPassword, saslMechanism)) {

            DescribeUserScramCredentialsResult result = admin.describeUserScramCredentials();
            Map<String, UserScramCredentialsDescription> users = result.all().get();
            return users.containsKey(username);
        }
    }

    public Map<String, Object> checkAuthentication(
            String username,
            String password,
            String topic,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String adminUsername,
            @Nullable String adminPassword,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {

        log.info("Checking authentication: username={}, topic={}", username, topic);
        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, username, password, saslMechanism)) {

            admin.describeCluster().clusterId().get();

            boolean isConsumer = checkAcl(admin, username, topic, AclOperation.READ);
            boolean isProducer = checkAcl(admin, username, topic, AclOperation.WRITE);

            String role;
            if (isConsumer && isProducer) {
                role = "both";
            } else if (isConsumer) {
                role = "consumer";
            } else if (isProducer) {
                role = "producer";
            } else {
                role = "none";
            }

            return Map.of(
                    "authenticated", true,
                    "username", username,
                    "role", role,
                    "isConsumer", isConsumer,
                    "isProducer", isProducer
            );
        } catch (Exception e) {
            return Map.of(
                    "authenticated", false,
                    "username", username,
                    "role", "none",
                    "isConsumer", false,
                    "isProducer", false,
                    "error", e.getMessage()
            );
        }
    }

    private boolean checkAcl(Admin admin, String username, String topic, AclOperation operation)
            throws ExecutionException, InterruptedException {
        String principal = "User:" + username;
        AccessControlEntryFilter entryFilter = new AccessControlEntryFilter(
                principal, "*", operation, AclPermissionType.ALLOW);
        AclBindingFilter topicFilter = new AclBindingFilter(
                new ResourcePattern(ResourceType.TOPIC, topic, PatternType.LITERAL).toFilter(),
                entryFilter);
        return !admin.describeAcls(topicFilter).values().get().isEmpty();
    }
}
