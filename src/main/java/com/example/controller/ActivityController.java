package com.example.controller;

import com.example.dto.activity.ActivityResponse;
import com.example.service.ActivityService;

import com.example.service.CurrentUserService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public List<ActivityResponse> getByTask(@RequestParam Long taskId) {
        return activityService.getByTask(taskId, currentUserService.get().getId());
    }
}