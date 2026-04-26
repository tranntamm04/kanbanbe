package com.example.service;

import com.example.dto.activity.ActivityResponse;
import com.example.entity.*;

import java.util.List;

public interface ActivityService {

    void log(String type, String message, Task task, Project project, User user);

    List<ActivityResponse> getByTask(Long taskId, Long userId);
}