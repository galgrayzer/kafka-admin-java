package com.kafka.admin.service;

import com.kafka.admin.client.KafkaAdminClientFactory;
import com.kafka.admin.config.KafkaAdminConfig;
import com.kafka.admin.model.request.MessageRecord;
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
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);
    private static final Duration POLL_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration MAX_POLL_INTERVAL = Duration.ofMinutes(5);

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

        log.debug("Getting topic offsets: topic={}", topicName);
        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, username, password, saslMechanism)) {

            DescribeTopicsResult topicResult = admin.describeTopics(Collections.singletonList(topicName));
            TopicDescription topicDesc = topicResult.allTopicNames().get().get(topicName);

            Map<TopicPartition, OffsetSpec> offsetSpecs = new HashMap<>();
            List<ConsumerOffsetResponse> responses = new ArrayList<>();

            for (TopicPartitionInfo tpInfo : topicDesc.partitions()) {
                TopicPartition tp = new TopicPartition(topicName, tpInfo.partition());
                offsetSpecs.put(tp, OffsetSpec.latest());
            }

            if (offsetSpecs.isEmpty()) {
                return responses;
            }

            ListOffsetsResult result = admin.listOffsets(offsetSpecs);

            for (TopicPartitionInfo tpInfo : topicDesc.partitions()) {
                TopicPartition tp = new TopicPartition(topicName, tpInfo.partition());
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

    public List<MessageResponse> fetchFromOffset(
            String topicName,
            @Nullable Integer partition,
            long startOffset,
            int maxMessages,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws Exception {

        log.debug("Fetching messages from offset: topic={}, partition={}", topicName, partition);
        List<TopicPartition> partitions = getPartitions(topicName, partition, bootstrapServers,
                securityProtocol, username, password, saslMechanism);

        Properties props = createConsumerProperties(bootstrapServers, securityProtocol, username, password, saslMechanism);
        props.remove(ConsumerConfig.GROUP_ID_CONFIG);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxMessages);

        List<MessageResponse> messages = new ArrayList<>();

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.assign(partitions);

            for (TopicPartition tp : partitions) {
                long targetOffset = startOffset;

                if (startOffset < 0) {
                    long endOffset = consumer.position(tp);
                    targetOffset = Math.max(0, endOffset - maxMessages);
                }

                consumer.seek(tp, targetOffset);
            }

            messages = pollMessages(consumer, new HashSet<>(partitions), maxMessages);
        }

        return messages;
    }

    public List<MessageResponse> fetchFromTimestamp(
            String topicName,
            @Nullable Integer partition,
            long timestamp,
            int maxMessages,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws Exception {

        log.debug("Fetching messages from timestamp: topic={}, partition={}", topicName, partition);
        List<TopicPartition> partitions = getPartitions(topicName, partition, bootstrapServers,
                securityProtocol, username, password, saslMechanism);

        Map<TopicPartition, Long> startOffsets = getStartOffsetsByTimestamp(
                partitions, timestamp, bootstrapServers,
                securityProtocol, username, password, saslMechanism);

        Properties props = createConsumerProperties(bootstrapServers, securityProtocol, username, password, saslMechanism);
        props.remove(ConsumerConfig.GROUP_ID_CONFIG);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxMessages);

        List<MessageResponse> messages = new ArrayList<>();

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.assign(partitions);

            for (TopicPartition tp : partitions) {
                Long offset = startOffsets.get(tp);
                if (offset != null && offset >= 0) {
                    consumer.seek(tp, offset);
                } else {
                    consumer.seekToBeginning(Collections.singletonList(tp));
                }
            }

            messages = pollMessages(consumer, new HashSet<>(partitions), maxMessages);
        }

        return messages;
    }

    public List<MessageResponse> fetchEarliest(
            String topicName,
            @Nullable Integer partition,
            int maxMessages,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws Exception {

        return fetchFromOffset(topicName, partition, 0L, maxMessages,
                bootstrapServers, securityProtocol, username, password, saslMechanism);
    }

    public List<MessageResponse> fetchLatest(
            String topicName,
            @Nullable Integer partition,
            int maxMessages,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws Exception {

        return fetchFromOffset(topicName, partition, -1L, maxMessages,
                bootstrapServers, securityProtocol, username, password, saslMechanism);
    }

    private List<TopicPartition> getPartitions(
            String topicName,
            @Nullable Integer partition,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {

        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, username, password, saslMechanism)) {

            DescribeTopicsResult topicResult = admin.describeTopics(Collections.singletonList(topicName));
            TopicDescription topicDesc = topicResult.allTopicNames().get().get(topicName);

            List<TopicPartition> partitions = new ArrayList<>();
            if (partition != null) {
                partitions.add(new TopicPartition(topicName, partition));
            } else {
                for (TopicPartitionInfo tpInfo : topicDesc.partitions()) {
                    partitions.add(new TopicPartition(topicName, tpInfo.partition()));
                }
            }
            return partitions;
        }
    }

    private Map<TopicPartition, Long> getStartOffsetsByTimestamp(
            List<TopicPartition> partitions,
            long timestamp,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws Exception {

        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, username, password, saslMechanism)) {

            Map<TopicPartition, OffsetSpec> offsetSpecs = new HashMap<>();
            for (TopicPartition tp : partitions) {
                offsetSpecs.put(tp, OffsetSpec.forTimestamp(timestamp));
            }

            ListOffsetsResult result = admin.listOffsets(offsetSpecs);

            Map<TopicPartition, Long> startOffsets = new HashMap<>();

            for (TopicPartition tp : partitions) {
                ListOffsetsResult.ListOffsetsResultInfo info = result.partitionResult(tp).get();
                startOffsets.put(tp, info.offset());
            }
            return startOffsets;
        }
    }

    private List<MessageResponse> pollMessages(
            KafkaConsumer<String, String> consumer,
            Set<TopicPartition> partitions,
            int maxMessages) {

        List<MessageResponse> messages = new ArrayList<>();

        while (messages.size() < maxMessages) {
            ConsumerRecords<String, String> records = consumer.poll(POLL_TIMEOUT);

            if (records.isEmpty()) {
                break;
            }

            for (ConsumerRecord<String, String> record : records) {
                if (messages.size() >= maxMessages) {
                    break;
                }

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

        log.info("Producing messages: topic={}, recordCount={}", request.getTopic(), request.getRecords().size());
        Properties props = createProducerProperties(bootstrapServers, securityProtocol, username, password, saslMechanism);

        int count = 0;
        try (@SuppressWarnings("deprecation") KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            for (MessageRecord record : request.getRecords()) {
                var producerRecord = createProducerRecord(request, record);
                producer.send(producerRecord);
                count++;
            }
            producer.flush();
        }

        return count;
    }

    private ProducerRecord<String, String> createProducerRecord(
            ProduceMessagesRequest request, MessageRecord record) {

        if (record.getHeaders() != null && !record.getHeaders().isEmpty()) {
            var headers = new RecordHeaders();
            record.getHeaders().forEach((key, value) -> headers.add(key, value.getBytes()));

            if (request.getPartition() != null) {
                return new ProducerRecord<>(
                        request.getTopic(), request.getPartition(),
                        record.getKey(), record.getValue(), headers);
            } else {
                return new ProducerRecord<>(
                        request.getTopic(), null,
                        record.getKey(), record.getValue(), headers);
            }
        } else {
            if (request.getPartition() != null) {
                return new ProducerRecord<>(
                        request.getTopic(), request.getPartition(),
                        record.getKey(), record.getValue());
            } else {
                return new ProducerRecord<>(
                        request.getTopic(), record.getKey(), record.getValue());
            }
        }
    }

    private Properties createConsumerProperties(String bootstrapServers, String securityProtocol,
            String username, String password, String saslMechanism) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "kafka-admin-consumer-" + UUID.randomUUID());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, (int) MAX_POLL_INTERVAL.toMillis());

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
}