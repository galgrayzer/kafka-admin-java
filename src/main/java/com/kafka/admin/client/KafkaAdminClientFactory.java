package com.kafka.admin.client;

import com.kafka.admin.constants.AdminConstants;
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

@Component
public class KafkaAdminClientFactory {

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

        String finalBootstrapServers = Optional.ofNullable(bootstrapServers)
                .orElse(config.getDefaultBootstrapServers());

        String finalSecurityProtocol = Optional.ofNullable(securityProtocol)
                .orElse(config.getDefaultSecurityProtocol());

        Map<String, Object> props = new HashMap<>();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, finalBootstrapServers);

        if (finalSecurityProtocol.equals(SecurityProtocol.SASL_PLAINTEXT.name()) ||
                finalSecurityProtocol.equals(SecurityProtocol.SASL_SSL.name())) {
            props.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, finalSecurityProtocol);

            String finalUsername = Optional.ofNullable(username)
                    .orElse(config.getDefaultUsername());
            String finalPassword = Optional.ofNullable(password)
                    .orElse(config.getDefaultPassword());
            String finalSaslMechanism = Optional.ofNullable(saslMechanism)
                    .orElse(config.getDefaultSaslMechanism());

            if (finalUsername != null && finalPassword != null) {
                props.put(SaslConfigs.SASL_MECHANISM, finalSaslMechanism);
                props.put(SaslConfigs.SASL_JAAS_CONFIG,
                        "org.apache.kafka.common.security.scram.ScramLoginModule required " +
                        "username=\"" + finalUsername + "\" " +
                        "password=\"" + finalPassword + "\";");
            }
        } else if (finalSecurityProtocol.equals(SecurityProtocol.SSL.name()) ||
                   finalSecurityProtocol.equals(SecurityProtocol.PLAINTEXT.name())) {
            props.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, finalSecurityProtocol);
        }

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

        String finalBootstrapServers = Optional.ofNullable(bootstrapServers)
                .orElse(config.getDefaultBootstrapServers());

        String finalSecurityProtocol = Optional.ofNullable(securityProtocol)
                .orElse(config.getDefaultSecurityProtocol());

        Map<String, Object> props = new HashMap<>();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, finalBootstrapServers);

        if (finalSecurityProtocol.equals(SecurityProtocol.SASL_PLAINTEXT.name()) ||
                finalSecurityProtocol.equals(SecurityProtocol.SASL_SSL.name())) {
            props.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, finalSecurityProtocol);
            
            String finalUsername = Optional.ofNullable(username)
                    .orElse(config.getDefaultUsername());
            String finalPassword = Optional.ofNullable(password)
                    .orElse(config.getDefaultPassword());
            String finalSaslMechanism = Optional.ofNullable(saslMechanism)
                    .orElse(config.getDefaultSaslMechanism());

            if (finalUsername != null && finalPassword != null) {
                props.put(SaslConfigs.SASL_MECHANISM, finalSaslMechanism);
                props.put(SaslConfigs.SASL_JAAS_CONFIG, 
                        "org.apache.kafka.common.security.scram.ScramLoginModule required " +
                        "username=\"" + finalUsername + "\" " +
                        "password=\"" + finalPassword + "\";");
            }
        } else if (finalSecurityProtocol.equals(SecurityProtocol.SSL.name()) ||
                   finalSecurityProtocol.equals(SecurityProtocol.PLAINTEXT.name())) {
            props.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, finalSecurityProtocol);
        }

        return props;
    }
}
