package com.kafka.admin.service;

import com.kafka.admin.client.KafkaAdminClientFactory;
import com.kafka.admin.model.request.*;
import com.kafka.admin.model.response.ClusterLinkResponse;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ClusterLinkService {

    private final KafkaAdminClientFactory adminClientFactory;

    public ClusterLinkService(KafkaAdminClientFactory adminClientFactory) {
        this.adminClientFactory = adminClientFactory;
    }

    public List<ClusterLinkResponse> listClusterLinks(
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cluster Links require Confluent Platform. Add the Confluent kafka-clients dependency to enable this feature.");
    }

    public void createClusterLink(
            CreateClusterLinkRequest request,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cluster Links require Confluent Platform. Add the Confluent kafka-clients dependency to enable this feature.");
    }

    public void deleteClusterLink(
            String linkName,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cluster Links require Confluent Platform. Add the Confluent kafka-clients dependency to enable this feature.");
    }

    public void createMirrorTopics(
            String linkName,
            CreateMirrorTopicsRequest request,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Mirror Topics require Confluent Platform. Add the Confluent kafka-clients dependency to enable this feature.");
    }

    public void reverseAndStart(
            String linkName,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cluster Links require Confluent Platform. Add the Confluent kafka-clients dependency to enable this feature.");
    }

    public void truncateAndRestore(
            String linkName,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cluster Links require Confluent Platform. Add the Confluent kafka-clients dependency to enable this feature.");
    }

    public void failover(
            String linkName,
            FailoverRequest request,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cluster Links require Confluent Platform. Add the Confluent kafka-clients dependency to enable this feature.");
    }

    public void promote(
            String linkName,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Mirror Topics require Confluent Platform. Add the Confluent kafka-clients dependency to enable this feature.");
    }
}
