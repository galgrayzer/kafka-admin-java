package com.kafka.admin.service;

import com.kafka.admin.client.KafkaAdminClientFactory;
import com.kafka.admin.model.request.CreateClusterLinkRequest;
import com.kafka.admin.model.request.CreateMirrorTopicsRequest;
import com.kafka.admin.model.request.FailoverRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClusterLinkServiceTest {

    @Test
    void testListClusterLinksThrowsUnsupportedOperationException() {
        ClusterLinkService service = new ClusterLinkService(null);
        assertThrows(UnsupportedOperationException.class, () ->
            service.listClusterLinks("localhost:9092", null, null, null, null)
        );
    }

    @Test
    void testCreateClusterLinkThrowsUnsupportedOperationException() {
        ClusterLinkService service = new ClusterLinkService(null);
        CreateClusterLinkRequest request = new CreateClusterLinkRequest();
        request.setLinkName("test-link");
        request.setSourceBootstrapServers("source:9092");
        
        assertThrows(UnsupportedOperationException.class, () ->
            service.createClusterLink(request, "localhost:9092", null, null, null, null)
        );
    }

    @Test
    void testDeleteClusterLinkThrowsUnsupportedOperationException() {
        ClusterLinkService service = new ClusterLinkService(null);
        assertThrows(UnsupportedOperationException.class, () ->
            service.deleteClusterLink("test-link", "localhost:9092", null, null, null, null)
        );
    }

    @Test
    void testCreateMirrorTopicsThrowsUnsupportedOperationException() {
        ClusterLinkService service = new ClusterLinkService(null);
        CreateMirrorTopicsRequest request = new CreateMirrorTopicsRequest();
        request.setTopics(List.of("topic1"));
        
        assertThrows(UnsupportedOperationException.class, () ->
            service.createMirrorTopics("test-link", request, "localhost:9092", null, null, null, null)
        );
    }

    @Test
    void testReverseAndStartThrowsUnsupportedOperationException() {
        ClusterLinkService service = new ClusterLinkService(null);
        assertThrows(UnsupportedOperationException.class, () ->
            service.reverseAndStart("test-link", "localhost:9092", null, null, null, null)
        );
    }

    @Test
    void testTruncateAndRestoreThrowsUnsupportedOperationException() {
        ClusterLinkService service = new ClusterLinkService(null);
        assertThrows(UnsupportedOperationException.class, () ->
            service.truncateAndRestore("test-link", "localhost:9092", null, null, null, null)
        );
    }

    @Test
    void testFailoverThrowsUnsupportedOperationException() {
        ClusterLinkService service = new ClusterLinkService(null);
        FailoverRequest request = new FailoverRequest();
        request.setPrimaryClusterId("cluster-1");
        
        assertThrows(UnsupportedOperationException.class, () ->
            service.failover("test-link", request, "localhost:9092", null, null, null, null)
        );
    }

    @Test
    void testPromoteThrowsUnsupportedOperationException() {
        ClusterLinkService service = new ClusterLinkService(null);
        assertThrows(UnsupportedOperationException.class, () ->
            service.promote("test-link", "localhost:9092", null, null, null, null)
        );
    }
}
