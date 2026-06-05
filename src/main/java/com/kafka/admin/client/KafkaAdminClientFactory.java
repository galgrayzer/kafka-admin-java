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

        Map<String, Object> props = createProperties(
                bootstrapServers, securityProtocol, username, password, saslMechanism
        );

        return Boolean.TRUE.equals(confluentAdmin)
                ? ConfluentAdmin.create(props)
                : Admin.create(props);
    }

    public Admin createAdminClient(
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) {

        return createAdminClient(bootstrapServers, securityProtocol, username, password, saslMechanism, false);
    }

    public Map<String, Object> createProperties(
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) {

        String finalBootstrapServers = defaultIfNull(bootstrapServers, config.getDefaultBootstrapServers());
        String finalSecurityProtocol = defaultIfNull(securityProtocol, config.getDefaultSecurityProtocol());

        SecurityProtocol protocol = SecurityProtocol.forName(finalSecurityProtocol);

        Map<String, Object> props = new HashMap<>();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, finalBootstrapServers);
        props.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, protocol.name());

        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, config.getDefaultRequestTimeoutMs());
        props.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, config.getDefaultAdminClientTimeoutMs());

        if (isSasl(protocol)) {
            String user = defaultIfNull(username, config.getDefaultUsername());
            String pass = defaultIfNull(password, config.getDefaultPassword());
            String mechanism = defaultIfNull(saslMechanism, config.getDefaultSaslMechanism());

            if (user == null || pass == null) {
                throw new IllegalArgumentException("SASL authentication requires both username and password.");
            }
            props.put(SaslConfigs.SASL_MECHANISM, mechanism);
            props.put(SaslConfigs.SASL_JAAS_CONFIG, buildJaasConfig(user, pass));
        }

        return props;
    }

    private boolean isSasl(SecurityProtocol protocol) {
        return protocol == SecurityProtocol.SASL_SSL ||
               protocol == SecurityProtocol.SASL_PLAINTEXT;
    }

    private String defaultIfNull(String value, String defaultValue) {
        return value != null ? value : defaultValue;
    }

    private String buildJaasConfig(String username, String password) {
        return String.format(
                "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";",
                username, password
        );
    }
}
