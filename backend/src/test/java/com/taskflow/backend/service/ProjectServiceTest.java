package com.taskflow.backend.service;

import com.taskflow.backend.dto.CreateProjectRequest;
import com.taskflow.backend.dto.ProjectResponse;
import com.taskflow.backend.entity.Project;
import com.taskflow.backend.entity.User;
import com.taskflow.backend.repository.ProjectRepository;
import com.taskflow.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void shouldCreateProjectForAuthenticatedUser() {

        // Arrange

        User user = new User();
        user.setId(1L);
        user.setName("Luiza");
        user.setEmail("luiza@test.com");

        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("TaskFlow");
        request.setDescription("Task management application");

        Authentication authentication =
                org.mockito.Mockito.mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("luiza@test.com");

        when(userRepository.findByEmail("luiza@test.com"))
                .thenReturn(Optional.of(user));

        Project savedProject = new Project();
        savedProject.setId(1L);
        savedProject.setName("TaskFlow");
        savedProject.setDescription("Task management application");
        savedProject.setCreatedBy(user);

        when(projectRepository.save(any(Project.class)))
                .thenReturn(savedProject);

        // Act

        ProjectResponse response =
                projectService.createProject(
                        request,
                        authentication
                );

        // Assert

        assertNotNull(response);

        assertEquals(1L, response.getId());

        assertEquals(
                "TaskFlow",
                response.getName()
        );

        assertEquals(
                "Task management application",
                response.getDescription()
        );

        assertEquals(
                1L,
                response.getCreatedBy()
        );

        verify(userRepository)
                .findByEmail("luiza@test.com");

        verify(projectRepository)
                .save(any(Project.class));
    }

    @Test
    void shouldReturnOnlyAuthenticatedUsersProjects() {

        // Arrange

        User user = new User();
        user.setId(1L);
        user.setName("Luiza");
        user.setEmail("luiza@test.com");

        Authentication authentication =
                org.mockito.Mockito.mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("luiza@test.com");

        when(userRepository.findByEmail("luiza@test.com"))
                .thenReturn(Optional.of(user));

        Project project1 = new Project();
        project1.setId(1L);
        project1.setName("TaskFlow");
        project1.setDescription("Task management application");
        project1.setCreatedBy(user);

        Project project2 = new Project();
        project2.setId(2L);
        project2.setName("Website");
        project2.setDescription("Website project");
        project2.setCreatedBy(user);

        when(projectRepository.findByCreatedById(1L))
                .thenReturn(List.of(project1, project2));

        // Act

        List<ProjectResponse> responses =
                projectService.getProjects(authentication);

        // Assert

        assertNotNull(responses);

        assertEquals(2, responses.size());

        assertEquals(
                "TaskFlow",
                responses.get(0).getName()
        );

        assertEquals(
                "Website",
                responses.get(1).getName()
        );

        assertEquals(
                1L,
                responses.get(0).getCreatedBy()
        );

        assertEquals(
                1L,
                responses.get(1).getCreatedBy()
        );

        verify(userRepository)
                .findByEmail("luiza@test.com");

        verify(projectRepository)
                .findByCreatedById(1L);
    }
}