package com.kafka.admin.service;

import com.kafka.admin.client.KafkaAdminClientFactory;
import com.kafka.admin.model.response.Broker;
import com.kafka.admin.model.response.ClusterMetadataResponse;
import com.kafka.admin.model.response.TopicMetadata;
import jakarta.annotation.Nullable;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ClusterService {

    private static final Logger log = LoggerFactory.getLogger(ClusterService.class);

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

        log.debug("Getting cluster metadata");
        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, username, password, saslMechanism)) {

            ClusterMetadataResponse response = new ClusterMetadataResponse();

            DescribeClusterResult clusterResult = admin.describeCluster();
            response.setClusterId(clusterResult.clusterId().get());

            var nodes = clusterResult.nodes().get();
            List<Broker> brokers = new ArrayList<>();
            for (Node node : nodes) {
                Broker broker = new Broker();
                broker.setId(node.id());
                broker.setHost(node.host());
                broker.setPort(node.port());
                broker.setRack(node.rack());
                brokers.add(broker);
            }
            response.setBrokers(brokers);

            ListTopicsResult topicsResult = admin.listTopics();
            Set<String> topicNames = topicsResult.names().get();
            List<TopicMetadata> topics = new ArrayList<>();
            if (!topicNames.isEmpty()) {
                DescribeTopicsResult describeResult = admin.describeTopics(topicNames);
                Map<String, TopicDescription> topicDescriptions = describeResult.allTopicNames().get();
                for (TopicDescription topicDesc : topicDescriptions.values()) {
                    TopicMetadata topicMetadata = new TopicMetadata();
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

        log.debug("Listing topics");
        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, username, password, saslMechanism)) {

            ListTopicsResult result = admin.listTopics();
            Set<String> topicNames = result.names().get();
            return new ArrayList<>(topicNames);
        }
    }
}
