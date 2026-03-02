package com.kafka.admin.service;

import com.kafka.admin.client.KafkaAdminClientFactory;
import com.kafka.admin.model.request.CreateTopicRequest;
import com.kafka.admin.model.request.UpdateTopicConfigRequest;
import com.kafka.admin.model.response.TopicResponse;
import jakarta.annotation.Nullable;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.Node;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class TopicService {

    private final KafkaAdminClientFactory adminClientFactory;

    public TopicService(KafkaAdminClientFactory adminClientFactory) {
        this.adminClientFactory = adminClientFactory;
    }

    public List<TopicResponse> listTopics(
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {

        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, username, password, saslMechanism)) {

            ListTopicsResult result = admin.listTopics();
            Set<String> topicNames = result.names().get();
            
            if (topicNames.isEmpty()) {
                return Collections.emptyList();
            }

            DescribeTopicsResult describeResult = admin.describeTopics(topicNames);
            Map<String, TopicDescription> topicDescriptions = describeResult.allTopicNames().get();

            return topicDescriptions.values().stream()
                    .map(topicDesc -> mapToTopicResponse(topicDesc))
                    .collect(Collectors.toList());
        }
    }

    public TopicResponse getTopic(
            String topicName,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {

        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, username, password, saslMechanism)) {

            DescribeTopicsResult result = admin.describeTopics(Collections.singletonList(topicName));
            TopicDescription topicDescription = result.allTopicNames().get().get(topicName);

            Map<String, String> configs = getTopicConfigs(admin, topicName);

            TopicResponse response = mapToTopicResponse(topicDescription);
            response.setConfigs(configs);
            return response;
        }
    }

    public void createTopic(
            CreateTopicRequest request,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {

        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, username, password, saslMechanism)) {

            NewTopic newTopic = new NewTopic(
                    request.getName(),
                    request.getPartitions(),
                    request.getReplicationFactor());

            if (request.getConfigs() != null && !request.getConfigs().isEmpty()) {
                newTopic.configs(request.getConfigs());
            }

            admin.createTopics(Collections.singletonList(newTopic)).all().get();
        }
    }

    public void deleteTopic(
            String topicName,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {

        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, username, password, saslMechanism)) {

            admin.deleteTopics(Collections.singletonList(topicName)).all().get();
        }
    }

    public void updateTopicConfig(
            String topicName,
            UpdateTopicConfigRequest request,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {

        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, username, password, saslMechanism)) {

            ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);

            List<AlterConfigOp> ops = request.getConfigs().entrySet().stream()
                    .map(e -> new AlterConfigOp(new ConfigEntry(e.getKey(), e.getValue()), AlterConfigOp.OpType.SET))
                    .collect(Collectors.toList());

            admin.incrementalAlterConfigs(Map.of(resource, ops)).all().get();
        }
    }

    private Map<String, String> getTopicConfigs(Admin admin, String topicName) throws ExecutionException, InterruptedException {
        ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);
        DescribeConfigsResult result = admin.describeConfigs(Set.of(resource));
        
        Map<ConfigResource, Config> configMap = result.all().get();
        Config config = configMap.get(resource);

        if (config == null) {
            return Collections.emptyMap();
        }

        return config.entries().stream()
                .filter(entry -> entry.value() != null)
                .collect(Collectors.toMap(
                        ConfigEntry::name,
                        ConfigEntry::value
                ));
    }

    private TopicResponse mapToTopicResponse(TopicDescription topicDescription) {
        TopicResponse response = new TopicResponse();
        response.setName(topicDescription.name());
        response.setPartitions(topicDescription.partitions().size());
        
        if (!topicDescription.partitions().isEmpty()) {
            response.setReplicationFactor((short) topicDescription.partitions().get(0).replicas().size());
        }

        List<TopicResponse.PartitionReplica> partitionReplicas = topicDescription.partitions().stream()
                .map(p -> {
                    TopicResponse.PartitionReplica replica = new TopicResponse.PartitionReplica();
                    replica.setPartitionId(p.partition());
                    replica.setReplicas(p.replicas().stream().map(Node::id).collect(Collectors.toList()));
                    replica.setIsr(p.isr().stream().map(Node::id).collect(Collectors.toList()));
                    return replica;
                })
                .collect(Collectors.toList());
        
        response.setPartitionsReplicas(partitionReplicas);
        return response;
    }
}
