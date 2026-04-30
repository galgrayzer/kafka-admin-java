package com.kafka.admin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${springdoc.api-docs.title:Kafka Admin API}")
    private String apiTitle;

    @Value("${springdoc.api-docs.version:1.0.0}")
    private String apiVersion;

    @Bean
    public OpenAPI kafkaAdminOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(apiTitle)
                        .version(apiVersion)
                        .description("REST API for managing Kafka cluster operations including topics, users, quotas, ACLs, and cluster linking")
                        .contact(new Contact()
                                .name("Kafka Admin Team")
                                .email("admin@kafka.local")));
    }
}
