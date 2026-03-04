package com.kafka.admin.service;

import com.kafka.admin.client.KafkaAdminClientFactory;
import com.kafka.admin.model.request.*;
import com.kafka.admin.model.response.ClusterLinkResponse;
import jakarta.annotation.Nullable;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.config.SaslConfigs;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {
        try (ConfluentAdmin admin = (ConfluentAdmin) adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, username, password, saslMechanism, true)) {
            
            Collection<ClusterLinkDescription> descriptions = admin.describeClusterLinks(new DescribeClusterLinksOptions()).result().get();
            
            List<ConfigResource> resources = descriptions.stream()
                    .map(d -> new ConfigResource(ConfigResource.Type.CLUSTER_LINK, d.linkName()))
                    .collect(Collectors.toList());
            
            Map<ConfigResource, Config> configs = resources.isEmpty() ? Collections.emptyMap() : admin.describeConfigs(resources).all().get();
            
            return descriptions.stream()
                    .map(d -> {
                        ClusterLinkResponse response = new ClusterLinkResponse();
                        response.setLinkName(d.linkName());
                        response.setSourceClusterId(d.remoteClusterId());
                        response.setState(d.linkState().name());
                        
                        Config config = configs.get(new ConfigResource(ConfigResource.Type.CLUSTER_LINK, d.linkName()));
                        if (config != null) {
                            Map<String, String> configMap = new HashMap<>();
                            config.entries().forEach(entry -> configMap.put(entry.name(), entry.value()));
                            response.setConfigs(configMap);
                        }
                        return response;
                    })
                    .collect(Collectors.toList());
        }
    }

    public void createClusterLink(
            CreateClusterLinkRequest request,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {
        try (ConfluentAdmin admin = (ConfluentAdmin) adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, username, password, saslMechanism, true)) {
            
            Map<String, String> configs = new HashMap<>(Optional.ofNullable(request.getConfigs()).orElse(new HashMap<>()));
            configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, request.getSourceBootstrapServers());
            
            if (request.getSourceSecurityProtocol() != null) {
                configs.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, request.getSourceSecurityProtocol());
            }
            
            if (request.getSourceUsername() != null && request.getSourcePassword() != null) {
                String jaasConfig = String.format("org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";",
                        request.getSourceUsername(), request.getSourcePassword());
                configs.put(SaslConfigs.SASL_JAAS_CONFIG, jaasConfig);
                configs.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-256");
            }
            
            NewClusterLink newLink = new NewClusterLink(request.getLinkName(), null, configs);
            admin.createClusterLinks(Collections.singletonList(newLink), new CreateClusterLinksOptions()).all().get();
        }
    }

    public void deleteClusterLink(
            String linkName,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {
        try (ConfluentAdmin admin = (ConfluentAdmin) adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, username, password, saslMechanism, true)) {
            admin.deleteClusterLinks(Collections.singletonList(linkName), new DeleteClusterLinksOptions()).all().get();
        }
    }

    public void createMirrorTopics(
            String linkName,
            CreateMirrorTopicsRequest request,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {
        try (ConfluentAdmin admin = (ConfluentAdmin) adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, username, password, saslMechanism, true)) {
            
            List<NewTopic> topics = request.getTopics().stream()
                    .map(topic -> {
                        NewMirrorTopic mirror = new NewMirrorTopic(linkName, topic);
                        NewTopic newTopic = new NewTopic(topic, Optional.empty(), Optional.empty());
                        newTopic.mirror(Optional.of(mirror));
                        if (request.getConfigs() != null) {
                            newTopic.configs(request.getConfigs());
                        }
                        return newTopic;
                    })
                    .collect(Collectors.toList());
            
            admin.createTopics(topics).all().get();
        }
    }

    public void reverseAndStart(
            String linkName,
            String topicName,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {
        executeMirrorOp(linkName, topicName, AlterMirrorOp.REVERSE_AND_START_REMOTE_MIRROR, bootstrapServers, securityProtocol, username, password, saslMechanism);
    }

    public void truncateAndRestore(
            String linkName,
            String topicName,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {
        executeMirrorOp(linkName, topicName, AlterMirrorOp.TRUNCATE_AND_RESTORE, bootstrapServers, securityProtocol, username, password, saslMechanism);
    }

    public void failover(
            String linkName,
            String topicName,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {
        executeMirrorOp(linkName, topicName, AlterMirrorOp.FAILOVER, bootstrapServers, securityProtocol, username, password, saslMechanism);
    }

    public void promote(
            String linkName,
            String topicName,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {
        executeMirrorOp(linkName, topicName, AlterMirrorOp.PROMOTE, bootstrapServers, securityProtocol, username, password, saslMechanism);
    }

    private void executeMirrorOp(
            String linkName,
            String topicName,
            AlterMirrorOp op,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {
        try (ConfluentAdmin admin = (ConfluentAdmin) adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, username, password, saslMechanism, true)) {
            
            Map<String, AlterMirrorOp> ops = new HashMap<>();
            ops.put(topicName, op);
            
            admin.alterMirrors(ops, new AlterMirrorsOptions()).all().get();
        }
    }
}
