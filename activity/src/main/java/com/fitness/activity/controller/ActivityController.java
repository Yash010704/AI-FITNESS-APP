package com.fitness.activity.controller;


import com.fitness.activity.dto.ActivityRequest;
import com.fitness.activity.dto.ActivityResponse;
import com.fitness.activity.service.ActivityService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@AllArgsConstructor
public class ActivityController {


    private ActivityService activityService;


    @PostMapping
    public ResponseEntity<ActivityResponse> trackActivity(@RequestBody ActivityRequest request , @RequestHeader("X-USER-ID") String userId){

        request.setUserId(userId);
        return ResponseEntity.ok(activityService.trackActivity(request));
    }

    @GetMapping
    public ResponseEntity<List<ActivityResponse>> getUserActivities(@RequestHeader("X-USER-ID") String userId){

        return ResponseEntity.ok(activityService.getUserActivites(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityResponse> getActivityById(@PathVariable String id){
        return ResponseEntity.ok(activityService.getActivityById(id));
    }


}