package com.taskflow.backend.dto;

import java.time.LocalDateTime;

public class CommentResponse {

    private Long id;
    private Long taskId;
    private String content;
    private Long authorId;
    private LocalDateTime createdAt;

    public CommentResponse(
            Long id,
            Long taskId,
            String content,
            Long authorId,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.taskId = taskId;
        this.content = content;
        this.authorId = authorId;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public String getContent() {
        return content;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}