package com.fitness.aiservice.service;

import com.fitness.aiservice.Repository.RecommendationRepository;
import com.fitness.aiservice.model.Activity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.aiservice.model.Recommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityMessageListner {

    private final ObjectMapper objectMapper;

    private final ActivityAIService activityAIService;
    private final RecommendationRepository recommendationRepository;

    @KafkaListener(topics = "${kafka.topic.name}", groupId = "activity-processor-group")
    public void processActivity(String message) {
        log.info("Raw message received from Kafka: {}", message);
        try {
            Activity activity = objectMapper.readValue(message, Activity.class);
            log.info("Received Activity for processing userId: {}", activity.getUserId());
            Recommendation recommendation = activityAIService.generateRecommendation(activity);
            recommendationRepository.save(recommendation);
        } catch (Exception e) {
            log.error("Error deserializing activity message: {}", e.getMessage());
        }
    }
}