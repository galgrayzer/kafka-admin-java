package com.kafka.admin.service;

import com.kafka.admin.client.KafkaAdminClientFactory;
import com.kafka.admin.model.request.CreateQuotaRequest;
import com.kafka.admin.model.response.QuotaResponse;
import jakarta.annotation.Nullable;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.quota.ClientQuotaAlteration;
import org.apache.kafka.common.quota.ClientQuotaEntity;
import org.apache.kafka.common.quota.ClientQuotaFilter;
import org.apache.kafka.common.quota.ClientQuotaFilterComponent;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class QuotaService {

    private final KafkaAdminClientFactory adminClientFactory;

    public QuotaService(KafkaAdminClientFactory adminClientFactory) {
        this.adminClientFactory = adminClientFactory;
    }

    public List<QuotaResponse> listQuotas(
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {

        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, username, password, saslMechanism)) {

            DescribeClientQuotasResult result = admin.describeClientQuotas(ClientQuotaFilter.all());
            Map<ClientQuotaEntity, Map<String, Double>> quotas = result.entities().get();

            return quotas.entrySet().stream()
                    .map(entry -> {
                        QuotaResponse response = new QuotaResponse();
                        Map<String, String> entityMap = entry.getKey().entries();
                        
                        if (entityMap.containsKey("user")) {
                            response.setEntityType("user");
                            response.setEntityName(entityMap.get("user"));
                        } else if (entityMap.containsKey("client-id")) {
                            response.setEntityType("client-id");
                            response.setEntityName(entityMap.get("client-id"));
                        } else {
                            response.setEntityType("default");
                            response.setEntityName("");
                        }

                        Map<String, String> configs = new HashMap<>();
                        entry.getValue().forEach((key, value) -> configs.put(key, String.valueOf(value)));
                        response.setConfigs(configs);
                        
                        return response;
                    })
                    .collect(Collectors.toList());
        }
    }

    public void createOrAlterQuota(
            CreateQuotaRequest request,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {

        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, username, password, saslMechanism)) {

            Map<String, String> entityMap = new HashMap<>();
            if (request.getUsername() != null && !request.getUsername().isEmpty()) {
                entityMap.put("user", request.getUsername());
            }

            ClientQuotaEntity entity = new ClientQuotaEntity(entityMap);
            List<ClientQuotaAlteration.Op> ops = new ArrayList<>();
            
            if (request.getBytesInQuota() != null) {
                ops.add(new ClientQuotaAlteration.Op("producer_byte_rate", request.getBytesInQuota().doubleValue()));
            }
            
            if (request.getBytesOutQuota() != null) {
                ops.add(new ClientQuotaAlteration.Op("consumer_byte_rate", request.getBytesOutQuota().doubleValue()));
            }

            AlterClientQuotasResult result = admin.alterClientQuotas(
                    Collections.singletonList(new ClientQuotaAlteration(entity, ops)));
            result.all().get();
        }
    }

    public void deleteQuota(
            String username,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String adminUsername,
            @Nullable String adminPassword,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {

        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username is required to delete a quota");
        }

        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, adminUsername, adminPassword, saslMechanism)) {

            Map<String, String> entityMap = new HashMap<>();
            entityMap.put(ClientQuotaEntity.USER, username);

            ClientQuotaEntity entity = new ClientQuotaEntity(entityMap);
            
            List<ClientQuotaAlteration.Op> ops = new ArrayList<>();
            ops.add(new ClientQuotaAlteration.Op("producer_byte_rate", null));
            ops.add(new ClientQuotaAlteration.Op("consumer_byte_rate", null));
            
            AlterClientQuotasResult result = admin.alterClientQuotas(
                    Collections.singletonList(new ClientQuotaAlteration(entity, ops)));
            result.all().get();
        }
    }

    public QuotaResponse getUserQuota(
            String username,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String adminUsername,
            @Nullable String adminPassword,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {

        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, adminUsername, adminPassword, saslMechanism)) {

            List<ClientQuotaFilterComponent> components = new ArrayList<>();
            components.add(ClientQuotaFilterComponent.ofEntity("user", username));
            ClientQuotaFilter filter = ClientQuotaFilter.contains(components);

            DescribeClientQuotasResult result = admin.describeClientQuotas(filter);
            Map<ClientQuotaEntity, Map<String, Double>> quotas = result.entities().get();

            if (quotas.isEmpty()) {
                return null;
            }

            Map.Entry<ClientQuotaEntity, Map<String, Double>> entry = quotas.entrySet().iterator().next();
            QuotaResponse response = new QuotaResponse();
            response.setEntityType("user");
            response.setEntityName(username);

            Map<String, String> configs = new HashMap<>();
            entry.getValue().forEach((key, value) -> configs.put(key, String.valueOf(value)));
            response.setConfigs(configs);

            return response;
        }
    }
}
