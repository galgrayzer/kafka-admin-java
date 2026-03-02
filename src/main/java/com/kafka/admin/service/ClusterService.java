package com.kafka.admin.service;

import com.kafka.admin.client.KafkaAdminClientFactory;
import com.kafka.admin.model.response.ClusterMetadataResponse;
import jakarta.annotation.Nullable;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.Node;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ClusterService {

    private final KafkaAdminClientFactory adminClientFactory;

    public ClusterService(KafkaAdminClientFactory adminClientFactory) {
        this.adminClientFactory = adminClientFactory;
    }

    public ClusterMetadataResponse getClusterMetadata(
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws Exception {

        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, username, password, saslMechanism)) {

            ClusterMetadataResponse response = new ClusterMetadataResponse();

            DescribeClusterResult clusterResult = admin.describeCluster();
            response.setClusterId(clusterResult.clusterId().get());

            var nodes = clusterResult.nodes().get();
            List<ClusterMetadataResponse.Broker> brokers = new ArrayList<>();
            for (Node node : nodes) {
                ClusterMetadataResponse.Broker broker = new ClusterMetadataResponse.Broker();
                broker.setId(node.id());
                broker.setHost(node.host());
                broker.setPort(node.port());
                broker.setRack(node.rack());
                brokers.add(broker);
            }
            response.setBrokers(brokers);

            ListTopicsResult topicsResult = admin.listTopics();
            Set<String> topicNames = topicsResult.names().get();
            List<ClusterMetadataResponse.TopicMetadata> topics = new ArrayList<>();
            if (!topicNames.isEmpty()) {
                DescribeTopicsResult describeResult = admin.describeTopics(topicNames);
                Map<String, TopicDescription> topicDescriptions = describeResult.allTopicNames().get();
                for (TopicDescription topicDesc : topicDescriptions.values()) {
                    ClusterMetadataResponse.TopicMetadata topicMetadata = new ClusterMetadataResponse.TopicMetadata();
                    topicMetadata.setName(topicDesc.name());
                    topicMetadata.setPartitionCount(topicDesc.partitions().size());
                    if (!topicDesc.partitions().isEmpty()) {
                        topicMetadata.setReplicationFactor(Integer.valueOf(topicDesc.partitions().get(0).replicas().size()));
                    }
                    topicMetadata.setIsInternal(topicDesc.isInternal());
                    topics.add(topicMetadata);
                }
            }
            response.setTopics(topics);

            return response;
        }
    }

    public List<String> listTopics(
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws Exception {

        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, username, password, saslMechanism)) {

            ListTopicsResult result = admin.listTopics();
            Set<String> topicNames = result.names().get();
            return new ArrayList<>(topicNames);
        }
    }
}
