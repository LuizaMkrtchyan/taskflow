package com.taskflow.backend.dto;

public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private Long createdBy;

    public ProjectResponse() {}

    public ProjectResponse(
            Long id,
            String name,
            String description,
            Long createdBy
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getCreatedBy() {
        return createdBy;
    }
}