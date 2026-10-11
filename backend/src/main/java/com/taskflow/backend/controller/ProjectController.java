package com.taskflow.backend.controller;

import com.taskflow.backend.dto.CreateProjectRequest;
import com.taskflow.backend.dto.ProjectResponse;
import com.taskflow.backend.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProject(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication
    ) {
        return projectService.createProject(
                request,
                authentication
        );
    }

    @GetMapping
    public List<ProjectResponse> getProjects(
            Authentication authentication
    ) {
        return projectService.getProjects(authentication);
    }
}