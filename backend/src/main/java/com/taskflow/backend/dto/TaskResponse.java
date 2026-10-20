
package com.taskflow.backend.dto;

import com.taskflow.backend.entity.Task;

import java.time.LocalDate;

public class TaskResponse {

    private Long id;
    private Long projectId;
    private String title;
    private String description;
    private Task.Status status;
    private Task.Priority priority;
    private Long assigneeId;
    private LocalDate dueDate;

    public TaskResponse() {
    }

    public TaskResponse(
            Long id,
            Long projectId,
            String title,
            String description,
            Task.Status status,
            Task.Priority priority,
            Long assigneeId,
            LocalDate dueDate
    ) {
        this.id = id;
        this.projectId = projectId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.assigneeId = assigneeId;
        this.dueDate = dueDate;
    }

    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Task.Status getStatus() {
        return status;
    }

    public Task.Priority getPriority() {
        return priority;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }
}
