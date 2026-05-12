package com.fitness.aiservice.service;


import com.fitness.aiservice.Repository.RecommendationRepository;
import com.fitness.aiservice.model.Recommendation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;


    public List<Recommendation> getUserRecommendation(String userId) {
        return recommendationRepository.findByUserId(userId);
    }

    public Recommendation getActivityRecommendation(String activityId) {
        return recommendationRepository.findByActivityId(activityId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No Recommendation found for this activity: " + activityId));
    }
}