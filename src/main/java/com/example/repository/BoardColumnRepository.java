package com.example.repository;

import com.example.entity.BoardColumn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardColumnRepository extends JpaRepository<BoardColumn, Long> {

    List<BoardColumn> findByProjectIdOrderByPositionAsc(Long projectId);

    long countByProjectId(Long projectId);

    void deleteByProjectId(Long projectId);

    void deleteByProjectWorkspaceId(Long workspaceId);
}
