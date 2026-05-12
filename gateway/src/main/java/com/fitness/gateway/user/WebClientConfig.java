package com.fitness.gateway.user;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced  // enables service-name based calls via Eureka e.g. http://user-service/...
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }


    public WebClient UserServiceWebClient(WebClient.Builder webClientBuilder){

        return webClientBuilder.baseUrl("http://USER-SERVICES").build();
    }

}