package com.kafka.admin.client;

import com.kafka.admin.config.KafkaAdminConfig;
import jakarta.annotation.Nullable;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.ConfluentAdmin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.config.SaslConfigs;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class KafkaAdminClientFactory {

    private static final Set<String> SUPPORTED_PROTOCOLS = Set.of(
            SecurityProtocol.PLAINTEXT.name(),
            SecurityProtocol.SSL.name(),
            SecurityProtocol.SASL_PLAINTEXT.name(),
            SecurityProtocol.SASL_SSL.name());

    private static final Set<String> SASL_PROTOCOLS = Set.of(
            SecurityProtocol.SASL_PLAINTEXT.name(),
            SecurityProtocol.SASL_SSL.name());

    private final KafkaAdminConfig config;

    public KafkaAdminClientFactory(KafkaAdminConfig config) {
        this.config = config;
    }

    public Admin createAdminClient(
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism,
            @Nullable Boolean confluentAdmin) {

        Map<String, Object> props = buildProperties(bootstrapServers, securityProtocol, username, password, saslMechanism);

        if (Boolean.TRUE.equals(confluentAdmin)) {
            return ConfluentAdmin.create(props);
        }

        return Admin.create(props);
    }

    public Admin createAdminClient(
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) {
        return this.createAdminClient(bootstrapServers, securityProtocol, username, password, saslMechanism, false);
    }

    public Map<String, Object> createProperties(
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) {
        return buildProperties(bootstrapServers, securityProtocol, username, password, saslMechanism);
    }

    private Map<String, Object> buildProperties(
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) {

        String finalBootstrapServers = Optional.ofNullable(bootstrapServers)
                .orElse(config.getDefaultBootstrapServers());

        String finalSecurityProtocol = Optional.ofNullable(securityProtocol)
                .orElse(config.getDefaultSecurityProtocol());

        validateSecurityProtocol(finalSecurityProtocol);

        Map<String, Object> props = new HashMap<>();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, finalBootstrapServers);
        props.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, finalSecurityProtocol);

        if (SASL_PROTOCOLS.contains(finalSecurityProtocol)) {
            applySaslProperties(props, username, password, saslMechanism);
        }

        return props;
    }

    private void validateSecurityProtocol(String securityProtocol) {
        if (!SUPPORTED_PROTOCOLS.contains(securityProtocol)) {
            throw new IllegalArgumentException("Unsupported security protocol: " + securityProtocol +
                    ". Supported values: " + String.join(", ", SUPPORTED_PROTOCOLS));
        }
    }

    private void applySaslProperties(Map<String, Object> props,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) {

        String finalUsername = Optional.ofNullable(username)
                .orElse(config.getDefaultUsername());
        String finalPassword = Optional.ofNullable(password)
                .orElse(config.getDefaultPassword());
        String finalSaslMechanism = Optional.ofNullable(saslMechanism)
                .orElse(config.getDefaultSaslMechanism());

        if (finalUsername != null && finalPassword != null) {
            props.put(SaslConfigs.SASL_MECHANISM, finalSaslMechanism);
            props.put(SaslConfigs.SASL_JAAS_CONFIG, buildJaasConfig(finalUsername, finalPassword, finalSaslMechanism));
        }
    }

    private String buildJaasConfig(String username, String password, String saslMechanism) {
        String loginModule = "PLAIN".equalsIgnoreCase(saslMechanism)
                ? "org.apache.kafka.common.security.plain.PlainLoginModule"
                : "org.apache.kafka.common.security.scram.ScramLoginModule";

        return loginModule + " required " +
                "username=\"" + username + "\" " +
                "password=\"" + password + "\";";
    }
}
