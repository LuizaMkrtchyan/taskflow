package com.taskflow.backend.service;

import com.taskflow.backend.dto.CommentResponse;
import com.taskflow.backend.dto.CreateCommentRequest;
import com.taskflow.backend.entity.Comment;
import com.taskflow.backend.entity.Project;
import com.taskflow.backend.entity.Task;
import com.taskflow.backend.entity.User;
import com.taskflow.backend.repository.CommentRepository;
import com.taskflow.backend.repository.TaskRepository;
import com.taskflow.backend.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CommentService commentService;

    private User user;
    private Task task;
    private Project project;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");

        User projectOwner = user;

        project = new Project();
        project.setId(10L);
        project.setCreatedBy(projectOwner);

        task = new Task();
        task.setId(20L);
        task.setProject(project);
    }

    @Test
    void shouldAddCommentForAuthenticatedProjectOwner() {
        when(authentication.getName()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));
        when(taskRepository.findById(20L))
                .thenReturn(Optional.of(task));

        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> {
                    Comment comment = invocation.getArgument(0);
                    comment.setId(30L);
                    return comment;
                });

        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("  Please review this task.  ");

        CommentResponse response = commentService.addComment(
                20L,
                request,
                authentication
        );

        assertEquals(30L, response.getId());
        assertEquals(20L, response.getTaskId());
        assertEquals("Please review this task.", response.getContent());
        assertEquals(1L, response.getAuthorId());
        assertNotNull(response.getCreatedAt());

        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void shouldReturnCommentsForTask() {
        when(authentication.getName()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));
        when(taskRepository.findById(20L))
                .thenReturn(Optional.of(task));

        Comment comment = new Comment();
        comment.setId(30L);
        comment.setTask(task);
        comment.setAuthor(user);
        comment.setContent("First comment");

        when(commentRepository.findByTaskId(20L))
                .thenReturn(List.of(comment));

        List<CommentResponse> responses = commentService.getComments(
                20L,
                authentication
        );

        assertEquals(1, responses.size());
        assertEquals("First comment", responses.get(0).getContent());
        assertEquals(20L, responses.get(0).getTaskId());

        verify(commentRepository).findByTaskId(20L);
    }
}