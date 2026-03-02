package com.kafka.admin.service;

import com.kafka.admin.client.KafkaAdminClientFactory;
import com.kafka.admin.model.request.*;
import com.kafka.admin.model.response.AclResponse;
import jakarta.annotation.Nullable;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.acl.*;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourceType;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class AclService {

    private final KafkaAdminClientFactory adminClientFactory;

    public AclService(KafkaAdminClientFactory adminClientFactory) {
        this.adminClientFactory = adminClientFactory;
    }

    public List<AclResponse> listAcls(
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {

        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, username, password, saslMechanism)) {

            DescribeAclsResult result = admin.describeAcls(AclBindingFilter.ANY);
            Collection<AclBinding> aclBindings = result.values().get();

            return aclBindings.stream()
                    .map(this::mapToAclResponse)
                    .collect(Collectors.toList());
        }
    }

    public void createAcl(
            CreateAclRequest request,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {

        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, username, password, saslMechanism)) {

            ResourceType resourceType = ResourceType.valueOf(request.getResourceType().toUpperCase());
            ResourcePattern resourcePattern = new ResourcePattern(
                    resourceType,
                    request.getResourceName(),
                    PatternType.LITERAL);

            AclOperation operation = AclOperation.valueOf(request.getOperation().toUpperCase());
            AclPermissionType permission = AclPermissionType.valueOf(request.getPermission().toUpperCase());
            String host = request.getHost() != null ? request.getHost() : "*";

            AccessControlEntry entry = new AccessControlEntry(
                    request.getPrincipal(),
                    host,
                    operation,
                    permission);

            AclBinding aclBinding = new AclBinding(resourcePattern, entry);

            admin.createAcls(Collections.singletonList(aclBinding)).all().get();
        }
    }

    public void deleteAcl(
            String resourceType,
            String resourceName,
            String principal,
            String host,
            String operation,
            String permission,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {

        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, username, password, saslMechanism)) {

            ResourceType resType = ResourceType.valueOf(resourceType.toUpperCase());
            ResourcePattern resourcePattern = new ResourcePattern(resType, resourceName, PatternType.LITERAL);

            AclOperation op = AclOperation.valueOf(operation.toUpperCase());
            AclPermissionType perm = AclPermissionType.valueOf(permission.toUpperCase());

            AccessControlEntryFilter entryFilter = new AccessControlEntryFilter(principal, host, op, perm);

            AclBindingFilter filter = new AclBindingFilter(
                    resourcePattern.toFilter(),
                    entryFilter);

            admin.deleteAcls(Collections.singletonList(filter)).all().get();
        }
    }

    public void grantConsumerAcl(
            String username,
            GrantConsumerAclRequest request,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String adminUsername,
            @Nullable String adminPassword,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {

        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, adminUsername, adminPassword, saslMechanism)) {

            String principal = "User:" + username;
            List<AclBinding> aclBindings = new ArrayList<>();

            ResourcePattern topicPattern = new ResourcePattern(
                    ResourceType.TOPIC,
                    request.getTopic(),
                    PatternType.LITERAL);

            aclBindings.add(new AclBinding(
                    topicPattern,
                    new AccessControlEntry(principal, "*", AclOperation.DESCRIBE, AclPermissionType.ALLOW)));

            aclBindings.add(new AclBinding(
                    topicPattern,
                    new AccessControlEntry(principal, "*", AclOperation.READ, AclPermissionType.ALLOW)));

            if (request.getGroup() != null && !request.getGroup().isEmpty()) {
                ResourcePattern groupPattern = new ResourcePattern(
                        ResourceType.GROUP,
                        request.getGroup(),
                        PatternType.LITERAL);

                aclBindings.add(new AclBinding(
                        groupPattern,
                        new AccessControlEntry(principal, "*", AclOperation.DESCRIBE, AclPermissionType.ALLOW)));

                aclBindings.add(new AclBinding(
                        groupPattern,
                        new AccessControlEntry(principal, "*", AclOperation.READ, AclPermissionType.ALLOW)));
            }

            admin.createAcls(aclBindings).all().get();
        }
    }

    public void grantProducerAcl(
            String username,
            GrantProducerAclRequest request,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String adminUsername,
            @Nullable String adminPassword,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {

        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, adminUsername, adminPassword, saslMechanism)) {

            String principal = "User:" + username;
            List<AclBinding> aclBindings = new ArrayList<>();

            ResourcePattern topicPattern = new ResourcePattern(
                    ResourceType.TOPIC,
                    request.getTopic(),
                    PatternType.LITERAL);

            aclBindings.add(new AclBinding(
                    topicPattern,
                    new AccessControlEntry(principal, "*", AclOperation.DESCRIBE, AclPermissionType.ALLOW)));

            aclBindings.add(new AclBinding(
                    topicPattern,
                    new AccessControlEntry(principal, "*", AclOperation.WRITE, AclPermissionType.ALLOW)));

            if (request.getTransactionId() != null && !request.getTransactionId().isEmpty()) {
                ResourcePattern txPattern = new ResourcePattern(
                        ResourceType.TRANSACTIONAL_ID,
                        request.getTransactionId(),
                        PatternType.LITERAL);

                aclBindings.add(new AclBinding(
                        txPattern,
                        new AccessControlEntry(principal, "*", AclOperation.WRITE, AclPermissionType.ALLOW)));
            }

            aclBindings.add(new AclBinding(
                    topicPattern,
                    new AccessControlEntry(principal, "*", AclOperation.CREATE, AclPermissionType.ALLOW)));

            admin.createAcls(aclBindings).all().get();
        }
    }

    public boolean checkConsumerAcl(
            String username,
            String topic,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String adminUsername,
            @Nullable String adminPassword,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {

        return checkAcl(username, topic, 
                AclOperation.READ, bootstrapServers, securityProtocol, 
                adminUsername, adminPassword, saslMechanism);
    }

    public boolean checkProducerAcl(
            String username,
            String topic,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String adminUsername,
            @Nullable String adminPassword,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {

        return checkAcl(username, topic, 
                AclOperation.WRITE, bootstrapServers, securityProtocol, 
                adminUsername, adminPassword, saslMechanism);
    }

    public void revokeConsumerAcl(
            String username,
            GrantConsumerAclRequest request,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String adminUsername,
            @Nullable String adminPassword,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {

        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, adminUsername, adminPassword, saslMechanism)) {

            String principal = "User:" + username;
            List<AclBindingFilter> filters = new ArrayList<>();

            ResourcePattern topicPattern = new ResourcePattern(
                    ResourceType.TOPIC,
                    request.getTopic(),
                    PatternType.LITERAL);

            filters.add(new AclBindingFilter(
                    topicPattern.toFilter(),
                    new AccessControlEntryFilter(principal, "*", AclOperation.DESCRIBE, AclPermissionType.ALLOW)));

            filters.add(new AclBindingFilter(
                    topicPattern.toFilter(),
                    new AccessControlEntryFilter(principal, "*", AclOperation.READ, AclPermissionType.ALLOW)));

            if (request.getGroup() != null && !request.getGroup().isEmpty()) {
                ResourcePattern groupPattern = new ResourcePattern(
                        ResourceType.GROUP,
                        request.getGroup(),
                        PatternType.LITERAL);

                filters.add(new AclBindingFilter(
                        groupPattern.toFilter(),
                        new AccessControlEntryFilter(principal, "*", AclOperation.DESCRIBE, AclPermissionType.ALLOW)));

                filters.add(new AclBindingFilter(
                        groupPattern.toFilter(),
                        new AccessControlEntryFilter(principal, "*", AclOperation.READ, AclPermissionType.ALLOW)));
            }

            admin.deleteAcls(filters).all().get();
        }
    }

    public void revokeProducerAcl(
            String username,
            GrantProducerAclRequest request,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String adminUsername,
            @Nullable String adminPassword,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {

        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, adminUsername, adminPassword, saslMechanism)) {

            String principal = "User:" + username;
            List<AclBindingFilter> filters = new ArrayList<>();

            ResourcePattern topicPattern = new ResourcePattern(
                    ResourceType.TOPIC,
                    request.getTopic(),
                    PatternType.LITERAL);

            filters.add(new AclBindingFilter(
                    topicPattern.toFilter(),
                    new AccessControlEntryFilter(principal, "*", AclOperation.DESCRIBE, AclPermissionType.ALLOW)));

            filters.add(new AclBindingFilter(
                    topicPattern.toFilter(),
                    new AccessControlEntryFilter(principal, "*", AclOperation.WRITE, AclPermissionType.ALLOW)));

            filters.add(new AclBindingFilter(
                    topicPattern.toFilter(),
                    new AccessControlEntryFilter(principal, "*", AclOperation.CREATE, AclPermissionType.ALLOW)));

            if (request.getTransactionId() != null && !request.getTransactionId().isEmpty()) {
                ResourcePattern txPattern = new ResourcePattern(
                        ResourceType.TRANSACTIONAL_ID,
                        request.getTransactionId(),
                        PatternType.LITERAL);

                filters.add(new AclBindingFilter(
                        txPattern.toFilter(),
                        new AccessControlEntryFilter(principal, "*", AclOperation.WRITE, AclPermissionType.ALLOW)));
            }

            admin.deleteAcls(filters).all().get();
        }
    }

    private boolean checkAcl(
            String username,
            String topic,
            AclOperation operation,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String adminUsername,
            @Nullable String adminPassword,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {

        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, adminUsername, adminPassword, saslMechanism)) {

            String principal = "User:" + username;
            
            AccessControlEntryFilter entryFilter = new AccessControlEntryFilter(principal, "*", operation, AclPermissionType.ALLOW);

            AclBindingFilter topicFilter = new AclBindingFilter(
                    new ResourcePattern(ResourceType.TOPIC, topic, PatternType.LITERAL).toFilter(),
                    entryFilter);

            DescribeAclsResult result = admin.describeAcls(topicFilter);
            Collection<AclBinding> acls = result.values().get();
            
            return !acls.isEmpty();
        }
    }

    private AclResponse mapToAclResponse(AclBinding aclBinding) {
        AclResponse response = new AclResponse();
        response.setResourceType(aclBinding.pattern().resourceType().name());
        response.setResourceName(aclBinding.pattern().name());
        response.setPrincipal(aclBinding.entry().principal());
        response.setHost(aclBinding.entry().host());
        response.setOperation(aclBinding.entry().operation().name());
        response.setPermission(aclBinding.entry().permissionType().name());
        return response;
    }
}
