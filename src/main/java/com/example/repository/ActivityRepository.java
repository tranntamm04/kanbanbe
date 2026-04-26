package com.example.repository;

import com.example.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findByTaskIdOrderByCreatedAtDesc(Long taskId);

    void deleteByTaskIdIn(List<Long> taskIds);

    void deleteByProjectId(Long projectId);

    void deleteByProjectWorkspaceId(Long workspaceId);
}