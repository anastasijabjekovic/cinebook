package com.cinebook.movie.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    /**
     * {@code @LoadBalanced} instructs Spring Cloud LoadBalancer to resolve
     * {@code http://user-service} URIs through Eureka before making the call.
     * The builder is injected into {@link com.cinebook.movie.client.UserClient}
     * and built once during construction.
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
