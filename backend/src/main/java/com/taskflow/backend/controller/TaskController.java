
package com.taskflow.backend.controller;

import com.taskflow.backend.dto.CreateTaskRequest;
import com.taskflow.backend.dto.TaskResponse;
import com.taskflow.backend.dto.UpdateTaskRequest;
import com.taskflow.backend.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/projects/{projectId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateTaskRequest request,
            Authentication authentication
    ) {
        return taskService.createTask(
                projectId,
                request,
                authentication
        );
    }

    @GetMapping("/projects/{projectId}/tasks")
    public List<TaskResponse> getTasks(
            @PathVariable Long projectId,
            Authentication authentication
    ) {
        return taskService.getTasks(projectId, authentication);
    }

    @PutMapping("/tasks/{taskId}")
    public TaskResponse updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request,
            Authentication authentication
    ) {
        return taskService.updateTask(
                taskId,
                request,
                authentication
        );
    }
}
