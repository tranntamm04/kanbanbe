package com.example.repository;

import com.example.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    void deleteByWorkspaceId(Long workspaceId);
    List<Project> findByWorkspaceId(Long workspaceId);
}