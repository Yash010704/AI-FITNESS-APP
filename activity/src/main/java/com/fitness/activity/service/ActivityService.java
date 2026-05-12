package com.fitness.activity.service;


import com.fitness.activity.dto.ActivityRequest;
import com.fitness.activity.dto.ActivityResponse;
import com.fitness.activity.model.Activity;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserValidationService userValidationService;

    private final KafkaTemplate<String , Activity> kafkaTemplate;


    @Value("${kafka.topic.name}")
    private String topicName;


    public  ActivityResponse trackActivity(ActivityRequest request) {

        boolean isValidUser = userValidationService.validateUser(request.getUserId());

        if(!isValidUser){
            throw new RuntimeException("Invalid User" + request.getUserId());
        }

        Activity activity = Activity.builder().
                userId(request.getUserId()).
                type(request.getType())
                .duration(request.getDuration()).caloriesBurned(request.getCaloriesBurned()).startTime(request.getStartTime())
                .additionalMetrics(request.getAdditionalMetrics())
                .build();


        Activity savedActivity = activityRepository.save(activity);

        try {
            kafkaTemplate.send(topicName , savedActivity.getUserId() , savedActivity);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return mapToResponse(savedActivity);

    }

    private ActivityResponse mapToResponse(Activity activity) {

        ActivityResponse response = new ActivityResponse();
        response.setId(activity.getId());
        response.setUserId(activity.getUserId());
        response.setType(activity.getType());
        response.setDuration(activity.getDuration());
        response.setCaloriesBurned(activity.getCaloriesBurned());
        response.setStartTime(activity.getStartTime());
        response.setAdditionalMetrics(activity.getAdditionalMetrics());
        response.setCreatedAt(activity.getCreatedAt());
        response.setUpdatedAt(activity.getUpdatedAt());
        return response;
    }

    public List<ActivityResponse> getUserActivites(String userId) {

        List<Activity> activityList = activityRepository.findByUserId(userId);

        return activityList.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public ActivityResponse getActivityById(String id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activity not found: " + id));
        return mapToResponse(activity);
    }
}