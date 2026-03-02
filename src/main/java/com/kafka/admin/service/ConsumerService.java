package com.kafka.admin.service;

import com.kafka.admin.client.KafkaAdminClientFactory;
import com.kafka.admin.model.request.CopyConsumerOffsetsRequest;
import com.kafka.admin.model.request.ResetConsumerOffsetsByTimeRequest;
import com.kafka.admin.model.request.ResetConsumerOffsetsRequest;
import com.kafka.admin.model.response.ConsumerOffsetResponse;
import jakarta.annotation.Nullable;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class ConsumerService {

    private final KafkaAdminClientFactory adminClientFactory;

    public ConsumerService(KafkaAdminClientFactory adminClientFactory) {
        this.adminClientFactory = adminClientFactory;
    }

    public List<ConsumerOffsetResponse> getConsumerOffsets(
            String groupId,
            String topic,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {

        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, username, password, saslMechanism)) {

            ListConsumerGroupOffsetsResult result = admin.listConsumerGroupOffsets(groupId);
            Map<TopicPartition, OffsetAndMetadata> offsets = result.partitionsToOffsetAndMetadata().get();

            List<ConsumerOffsetResponse> responses = new ArrayList<>();
            for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : offsets.entrySet()) {
                if (topic == null || entry.getKey().topic().equals(topic)) {
                    ConsumerOffsetResponse response = new ConsumerOffsetResponse();
                    response.setTopic(entry.getKey().topic());
                    response.setPartition(entry.getKey().partition());
                    response.setCurrentOffset(entry.getValue().offset());
                    responses.add(response);
                }
            }
            return responses;
        }
    }

    public void resetConsumerOffsets(
            String groupId,
            ResetConsumerOffsetsRequest request,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {

        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, username, password, saslMechanism)) {

            Map<TopicPartition, OffsetAndMetadata> offsetMap = new HashMap<>();
            List<TopicPartition> partitionsToReset = new ArrayList<>();

            if (request.getPartition() != null) {
                partitionsToReset.add(new TopicPartition(request.getTopic(), request.getPartition()));
            } else {
                DescribeTopicsResult topicResult = admin.describeTopics(Collections.singletonList(request.getTopic()));
                TopicDescription topicDesc = topicResult.allTopicNames().get().get(request.getTopic());
                for (TopicPartitionInfo tpInfo : topicDesc.partitions()) {
                    partitionsToReset.add(new TopicPartition(request.getTopic(), tpInfo.partition()));
                }
            }

            if ("earliest".equalsIgnoreCase(request.getResetStrategy())) {
                for (TopicPartition tp : partitionsToReset) {
                    ListOffsetsResult result = admin.listOffsets(Map.of(tp, OffsetSpec.earliest()));
                    long offset = result.partitionResult(tp).get().offset();
                    offsetMap.put(tp, new OffsetAndMetadata(offset));
                }
            } else if ("latest".equalsIgnoreCase(request.getResetStrategy())) {
                for (TopicPartition tp : partitionsToReset) {
                    ListOffsetsResult result = admin.listOffsets(Map.of(tp, OffsetSpec.latest()));
                    long offset = result.partitionResult(tp).get().offset();
                    offsetMap.put(tp, new OffsetAndMetadata(offset));
                }
            } else if (request.getOffset() != null) {
                for (TopicPartition tp : partitionsToReset) {
                    offsetMap.put(tp, new OffsetAndMetadata(Long.parseLong(request.getOffset())));
                }
            }

            AlterConsumerGroupOffsetsResult result = admin.alterConsumerGroupOffsets(groupId, offsetMap);
            result.all().get();
        }
    }

    public void resetConsumerOffsetsByTimestamp(
            String groupId,
            ResetConsumerOffsetsByTimeRequest request,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {

        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, username, password, saslMechanism)) {

            Map<TopicPartition, OffsetAndMetadata> offsetMap = new HashMap<>();
            List<TopicPartition> partitionsToReset = new ArrayList<>();

            if (request.getPartition() != null) {
                partitionsToReset.add(new TopicPartition(request.getTopic(), request.getPartition()));
            } else {
                DescribeTopicsResult topicResult = admin.describeTopics(Collections.singletonList(request.getTopic()));
                TopicDescription topicDesc = topicResult.allTopicNames().get().get(request.getTopic());
                for (TopicPartitionInfo tpInfo : topicDesc.partitions()) {
                    partitionsToReset.add(new TopicPartition(request.getTopic(), tpInfo.partition()));
                }
            }

            for (TopicPartition tp : partitionsToReset) {
                ListOffsetsResult result = admin.listOffsets(Map.of(tp, OffsetSpec.forTimestamp(request.getTimestamp())));
                long offset = result.partitionResult(tp).get().offset();
                offsetMap.put(tp, new OffsetAndMetadata(offset));
            }

            AlterConsumerGroupOffsetsResult alterResult = admin.alterConsumerGroupOffsets(groupId, offsetMap);
            alterResult.all().get();
        }
    }

    public void copyConsumerOffsets(
            String groupId,
            CopyConsumerOffsetsRequest request,
            String bootstrapServers,
            @Nullable String securityProtocol,
            @Nullable String username,
            @Nullable String password,
            @Nullable String saslMechanism) throws ExecutionException, InterruptedException {

        try (Admin admin = adminClientFactory.createAdminClient(
                bootstrapServers, securityProtocol, username, password, saslMechanism)) {

            ListConsumerGroupOffsetsResult sourceResult = admin.listConsumerGroupOffsets(request.getSourceGroup());
            Map<TopicPartition, OffsetAndMetadata> sourceOffsets = sourceResult.partitionsToOffsetAndMetadata().get();

            Map<TopicPartition, OffsetAndMetadata> targetOffsets = new HashMap<>();
            for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : sourceOffsets.entrySet()) {
                if (entry.getKey().topic().equals(request.getTopic())) {
                    TopicPartition targetTp = new TopicPartition(groupId, entry.getKey().partition());
                    targetOffsets.put(targetTp, entry.getValue());
                }
            }

            AlterConsumerGroupOffsetsResult result = admin.alterConsumerGroupOffsets(groupId, targetOffsets);
            result.all().get();
        }
    }
}
