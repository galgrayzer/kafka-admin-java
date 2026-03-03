package com.kafka.admin.service;

import com.kafka.admin.client.KafkaAdminClientFactory;
import com.kafka.admin.config.KafkaAdminConfig;
import com.kafka.admin.model.request.FetchMessagesRequest;
import com.kafka.admin.model.request.ProduceMessagesRequest;
import com.kafka.admin.model.response.ConsumerOffsetResponse;
import com.kafka.admin.model.response.MessageResponse;
import jakarta.annotation.Nullable;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Service
public class MessageService {

    private final KafkaAdminClientFactory adminClientFactory;
    private final KafkaAdminConfig config;

    public MessageService(KafkaAdminClientFactory adminClientFactory, KafkaAdminConfig config) {
        this.adminClientFactory = adminClientFactory;
        this.config = config;
    }

    public List<ConsumerOffsetResponse> getTopicOffsets(
            String topicName,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws Exception {

        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, username, password, saslMechanism)) {

            DescribeTopicsResult topicResult = admin.describeTopics(Collections.singletonList(topicName));
            TopicDescription topicDesc = topicResult.allTopicNames().get().get(topicName);

            List<ConsumerOffsetResponse> responses = new ArrayList<>();
            for (TopicPartitionInfo tpInfo : topicDesc.partitions()) {
                TopicPartition tp = new TopicPartition(topicName, tpInfo.partition());
                ListOffsetsResult result = admin.listOffsets(Map.of(tp, OffsetSpec.latest()));
                ListOffsetsResult.ListOffsetsResultInfo offsetInfo = result.partitionResult(tp).get();

                ConsumerOffsetResponse response = new ConsumerOffsetResponse();
                response.setTopic(topicName);
                response.setPartition(tpInfo.partition());
                response.setCurrentOffset(offsetInfo.offset());
                responses.add(response);
            }
            return responses;
        }
    }

    public List<MessageResponse> fetchMessages(
            FetchMessagesRequest request,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws Exception {

        Properties props = createConsumerProperties(bootstrapServers, securityProtocol, username, password, saslMechanism);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, request.getMaxMessages() != null ? request.getMaxMessages() : 100);

        List<MessageResponse> messages = new ArrayList<>();

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            TopicPartition tp = new TopicPartition(request.getTopic(), 
                    request.getPartition() != null ? request.getPartition() : 0);

            if (request.getOffset() != null) {
                consumer.assign(Collections.singletonList(tp));
                consumer.seek(tp, request.getOffset());
            } else if ("earliest".equalsIgnoreCase(request.getStartingPosition())) {
                consumer.assign(Collections.singletonList(tp));
                consumer.seekToBeginning(Collections.singletonList(tp));
            } else if ("latest".equalsIgnoreCase(request.getStartingPosition())) {
                consumer.assign(Collections.singletonList(tp));
                consumer.seekToEnd(Collections.singletonList(tp));
            } else if (request.getTimestamp() != null) {
                ListOffsetsResult result = adminClientFactory.createAdminClient(
                        bootstrapServers, securityProtocol, username, password, saslMechanism)
                        .listOffsets(Map.of(tp, OffsetSpec.forTimestamp(request.getTimestamp())));
                ListOffsetsResult.ListOffsetsResultInfo offsetInfo = result.partitionResult(tp).get();
                consumer.assign(Collections.singletonList(tp));
                consumer.seek(tp, offsetInfo.offset());
            } else {
                consumer.assign(Collections.singletonList(tp));
                consumer.seekToBeginning(Collections.singletonList(tp));
            }

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
            for (ConsumerRecord<String, String> record : records) {
                MessageResponse response = new MessageResponse();
                response.setTopic(record.topic());
                response.setPartition(record.partition());
                response.setOffset(record.offset());
                response.setTimestamp(record.timestamp());
                response.setKey(record.key());
                response.setValue(record.value());
                messages.add(response);
            }
        }

        return messages;
    }

    public int produceMessages(
            ProduceMessagesRequest request,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws Exception {

        Properties props = createProducerProperties(bootstrapServers, securityProtocol, username, password, saslMechanism);

        int count = 0;
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            for (ProduceMessagesRequest.ProducerRecord record : request.getRecords()) {
                ProducerRecord<String, String> producerRecord;
                
                if (record.getHeaders() != null && !record.getHeaders().isEmpty()) {
                    var headers = new RecordHeaders();
                    record.getHeaders().forEach((key, value) -> headers.add(key, value.getBytes()));
                    
                    if (request.getPartition() != null) {
                        producerRecord = new ProducerRecord<>(request.getTopic(), request.getPartition(), 
                                record.getTimestamp(), record.getKey(), record.getValue(), headers);
                    } else {
                        producerRecord = new ProducerRecord<>(request.getTopic(), null, 
                                record.getTimestamp(), record.getKey(), record.getValue(), headers);
                    }
                } else {
                    if (request.getPartition() != null) {
                        producerRecord = new ProducerRecord<>(request.getTopic(), request.getPartition(), 
                                record.getTimestamp(), record.getKey(), record.getValue());
                    } else {
                        producerRecord = new ProducerRecord<>(request.getTopic(), record.getKey(), record.getValue());
                    }
                }
                producer.send(producerRecord);
                count++;
            }
            producer.flush();
        }

        return count;
    }

    private Properties createConsumerProperties(String bootstrapServers, String securityProtocol, 
            String username, String password, String saslMechanism) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "kafka-admin-consumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        applySecurityProperties(props, bootstrapServers, securityProtocol, username, password, saslMechanism);
        return props;
    }

    private Properties createProducerProperties(String bootstrapServers, String securityProtocol, 
            String username, String password, String saslMechanism) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        applySecurityProperties(props, bootstrapServers, securityProtocol, username, password, saslMechanism);
        return props;
    }

    private void applySecurityProperties(Properties props, String bootstrapServers, String securityProtocol,
            String username, String password, String saslMechanism) {
        
        if (securityProtocol == null) {
            securityProtocol = config.getDefaultSecurityProtocol();
        }
        if (saslMechanism == null) {
            saslMechanism = config.getDefaultSaslMechanism();
        }
        if (username == null) {
            username = config.getDefaultUsername();
        }
        if (password == null) {
            password = config.getDefaultPassword();
        }
        
        props.put("security.protocol", securityProtocol);
        
        if (securityProtocol.equals("SASL_PLAINTEXT") || securityProtocol.equals("SASL_SSL")) {
            props.put("sasl.mechanism", saslMechanism);
            
            if (username != null && password != null) {
                if ("PLAIN".equalsIgnoreCase(saslMechanism)) {
                    props.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                            "username=\"" + username + "\" password=\"" + password + "\";");
                } else {
                    props.put("sasl.jaas.config", "org.apache.kafka.common.security.scram.ScramLoginModule required " +
                            "username=\"" + username + "\" password=\"" + password + "\";");
                }
            }
        }
    }

    private Admin createAdminClient(String bootstrapServers, String securityProtocol,
            String username, String password, String saslMechanism) {
        return adminClientFactory.createAdminClient(bootstrapServers, securityProtocol, username, password, saslMechanism);
    }
}
