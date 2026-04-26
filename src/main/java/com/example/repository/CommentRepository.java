package com.example.repository;

import com.example.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByTaskIdOrderByCreatedAtAsc(Long taskId);

    void deleteByTaskIdIn(List<Long> taskIds);
}