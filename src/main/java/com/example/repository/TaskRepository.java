package com.example.repository;

import com.example.entity.Task;
import org.springframework.data.jpa.repository.*;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    @Modifying
    @Query("""
DELETE FROM Task t
WHERE t.project.workspace.id = :workspaceId
""")
    void deleteByWorkspaceId(Long workspaceId);

    void deleteByProjectId(Long projectId);

    void deleteByColumnId(Long columnId);

    List<Task> findByProjectId(Long projectId);

    List<Task> findByProjectWorkspaceId(Long workspaceId);

    List<Task> findByColumnIdOrderByPositionAsc(Long columnId);
}