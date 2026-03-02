package com.kafka.admin.service;

import com.kafka.admin.client.KafkaAdminClientFactory;
import com.kafka.admin.model.request.CreateUserRequest;
import com.kafka.admin.model.response.UserResponse;
import jakarta.annotation.Nullable;
import org.apache.kafka.clients.admin.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class UserService {

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

        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, adminUsername, adminPassword, saslMechanism)) {

            UserScramCredentialDeletion deletion = new UserScramCredentialDeletion(username, null);
            admin.alterUserScramCredentials(List.of(deletion)).all().get();
        }
    }
}
