
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TaskService taskService;

    @Test
    void shouldCreateTaskForProjectOwner() {
        User user = new User();
        user.setId(1L);
        user.setName("Luiza");
        user.setEmail("luiza@test.com");

        Project project = new Project();
        project.setId(1L);
        project.setCreatedBy(user);

        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Implement login");
        request.setDescription("Login functionality");
        request.setPriority(Task.Priority.HIGH);
        request.setAssigneeId(1L);

        when(authentication.getName())
                .thenReturn("luiza@test.com");
        when(userRepository.findByEmail("luiza@test.com"))
                .thenReturn(Optional.of(user));
        when(projectRepository.findById(1L))
                .thenReturn(Optional.of(project));
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> {
                    Task task = invocation.getArgument(0);
                    task.setId(10L);
                    return task;
                });

        TaskResponse response = taskService.createTask(
                1L, request, authentication
        );

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(1L, response.getProjectId());
        assertEquals("Implement login", response.getTitle());
        assertEquals(Task.Status.TODO, response.getStatus());
        assertEquals(Task.Priority.HIGH, response.getPriority());
        assertEquals(1L, response.getAssigneeId());

        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void shouldUpdateTaskStatus() {
        User user = new User();
        user.setId(1L);
        user.setName("Luiza");
        user.setEmail("luiza@test.com");

        Project project = new Project();
        project.setId(1L);
        project.setCreatedBy(user);

        Task task = new Task();
        task.setId(10L);
        task.setProject(project);
        task.setTitle("Implement login");
        task.setDescription("Original description");
        task.setStatus(Task.Status.TODO);
        task.setPriority(Task.Priority.HIGH);
        task.setAssignee(user);

        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("Implement login");
        request.setDescription("Updated description");
        request.setStatus(Task.Status.IN_PROGRESS);
        request.setPriority(Task.Priority.MEDIUM);
        request.setAssigneeId(1L);

        when(authentication.getName())
                .thenReturn("luiza@test.com");
        when(userRepository.findByEmail("luiza@test.com"))
                .thenReturn(Optional.of(user));
        when(taskRepository.findById(10L))
                .thenReturn(Optional.of(task));
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.updateTask(
                10L, request, authentication
        );

        assertEquals(10L, response.getId());
        assertEquals(
                Task.Status.IN_PROGRESS,
                response.getStatus()
        );
        assertEquals(
                Task.Priority.MEDIUM,
                response.getPriority()
        );
        assertEquals(
                "Updated description",
                response.getDescription()
        );

        verify(taskRepository).save(task);
    }
}
