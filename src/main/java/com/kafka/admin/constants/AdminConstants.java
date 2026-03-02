package com.kafka.admin.constants;

public final class AdminConstants {

    private AdminConstants() {
    }

    public static final String DEFAULT_BOOTSTRAP_SERVERS = "localhost:9092";
    public static final String DEFAULT_SECURITY_PROTOCOL = "SASL_PLAINTEXT";
    public static final String DEFAULT_SASL_MECHANISM = "SCRAM-SHA-256";

    public static final String HEADER_SECURITY_PROTOCOL = "X-Kafka-Security-Protocol";
    public static final String HEADER_USERNAME = "X-Kafka-Username";
    public static final String HEADER_PASSWORD = "X-Kafka-Password";
    public static final String HEADER_SASL_MECHANISM = "X-Kafka-Sasl-Mechanism";

    public static final String ENV_BOOTSTRAP_SERVERS = "KAFKA_ADMIN_DEFAULT_BOOTSTRAP_SERVERS";
    public static final String ENV_SECURITY_PROTOCOL = "KAFKA_ADMIN_DEFAULT_SECURITY_PROTOCOL";
    public static final String ENV_USERNAME = "KAFKA_ADMIN_DEFAULT_USERNAME";
    public static final String ENV_PASSWORD = "KAFKA_ADMIN_DEFAULT_PASSWORD";
    public static final String ENV_SASL_MECHANISM = "KAFKA_ADMIN_DEFAULT_SASL_MECHANISM";

    public static final String API_V1_BASE = "/api/v1";
    public static final String TOPICS_PATH = "/topics";
    public static final String USERS_PATH = "/users";
    public static final String QUOTAS_PATH = "/quotas";
    public static final String ACLS_PATH = "/acls";
    public static final String CLUSTER_LINKS_PATH = "/cluster-links";

    public static final String PARAM_BOOTSTRAP_SERVERS = "bootstrapServers";
}
