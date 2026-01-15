package com.ai.mvp.config;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobWorker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Zeebe Client Configuration for Camunda Platform 8 Integration (Phase 6)
 *
 * Configures the Zeebe client to connect to the Camunda 8 workflow engine.
 * The Zeebe client is used to:
 * - Deploy BPMN processes
 * - Start process instances
 * - Create job workers
 * - Query process status
 *
 * @author AI MVP Team
 * @version 1.0
 * @since Phase 6
 */
@Configuration
public class ZeebeClientConfiguration {

    @Value("${zeebe.client.broker.gateway-address}")
    private String gatewayAddress;

    @Value("${zeebe.client.security.plaintext:true}")
    private boolean usePlaintext;

    /**
     * Creates a Zeebe Client bean for connecting to the Camunda 8 workflow engine.
     *
     * Configuration:
     * - Gateway Address: localhost:26500 (gRPC)
     * - Security: Plaintext (no TLS) for development
     * - Request Timeout: 10 seconds
     *
     * The client is autocloseable and will be properly closed when the application shuts down.
     *
     * @return ZeebeClient instance configured for the Camunda 8 cluster
     */
    @Bean
    public ZeebeClient zeebeClient() {
        return ZeebeClient.newClientBuilder()
                .gatewayAddress(gatewayAddress)
                .usePlaintext()  // No TLS/authentication for development
                .defaultRequestTimeout(Duration.ofSeconds(10))
                .build();
    }
}
