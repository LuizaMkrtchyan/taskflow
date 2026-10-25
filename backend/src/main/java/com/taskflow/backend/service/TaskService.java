
package com.taskflow.backend.service;

import com.taskflow.backend.dto.CreateTaskRequest;
import com.taskflow.backend.dto.TaskResponse;
import com.taskflow.backend.dto.UpdateTaskRequest;
import com.taskflow.backend.entity.Project;
import com.taskflow.backend.entity.Task;
import com.taskflow.backend.entity.User;
import com.taskflow.backend.repository.ProjectRepository;
import com.taskflow.backend.repository.TaskRepository;
import com.taskflow.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public TaskService(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository
    ) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public TaskResponse createTask(
            Long projectId,
            CreateTaskRequest request,
            Authentication authentication
    ) {
        User currentUser = getAuthenticatedUser(authentication);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Project not found"
                ));

        checkProjectOwnership(project, currentUser);

        User assignee = userRepository.findById(
                request.getAssigneeId()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Assignee not found"
        ));

        Task task = new Task();
        task.setProject(project);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(Task.Status.TODO);
        task.setPriority(request.getPriority());
        task.setAssignee(assignee);
        task.setDueDate(request.getDueDate());

        Task savedTask = taskRepository.save(task);

        return toTaskResponse(savedTask);
    }

    public List<TaskResponse> getTasks(
            Long projectId,
            Authentication authentication
    ) {
        User currentUser = getAuthenticatedUser(authentication);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Project not found"
                ));

        checkProjectOwnership(project, currentUser);

        return taskRepository.findByProjectId(projectId)
                .stream()
                .map(this::toTaskResponse)
                .toList();
    }

    public TaskResponse updateTask(
            Long taskId,
            UpdateTaskRequest request,
            Authentication authentication
    ) {
        User currentUser = getAuthenticatedUser(authentication);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Task not found"
                ));

        checkProjectOwnership(task.getProject(), currentUser);

        User assignee = userRepository.findById(
                request.getAssigneeId()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Assignee not found"
        ));

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setAssignee(assignee);
        task.setDueDate(request.getDueDate());

        Task updatedTask = taskRepository.save(task);

        return toTaskResponse(updatedTask);
    }

    public void deleteTask(
            Long taskId,
            Authentication authentication
    ) {
        User currentUser = getAuthenticatedUser(authentication);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Task not found"
                ));

        checkProjectOwnership(task.getProject(), currentUser);

        taskRepository.delete(task);
    }

    private User getAuthenticatedUser(
            Authentication authentication
    ) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found"
                ));
    }

    private void checkProjectOwnership(
            Project project,
            User user
    ) {
        if (!project.getCreatedBy().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have permission to access this project"
            );
        }
    }

    private TaskResponse toTaskResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getProject().getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getAssignee().getId(),
                task.getDueDate()
        );
    }
}
