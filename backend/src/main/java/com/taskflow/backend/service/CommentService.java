package com.taskflow.backend.service;

import com.taskflow.backend.dto.CommentResponse;
import com.taskflow.backend.dto.CreateCommentRequest;
import com.taskflow.backend.entity.Comment;
import com.taskflow.backend.entity.Task;
import com.taskflow.backend.entity.User;
import com.taskflow.backend.repository.CommentRepository;
import com.taskflow.backend.repository.TaskRepository;
import com.taskflow.backend.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public CommentService(
            CommentRepository commentRepository,
            TaskRepository taskRepository,
            UserRepository userRepository
    ) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public CommentResponse addComment(
            Long taskId,
            CreateCommentRequest request,
            Authentication authentication
    ) {
        User user = getAuthenticatedUser(authentication);
        Task task = getTaskForCurrentUser(taskId, user);

        Comment comment = new Comment();
        comment.setContent(request.getContent().trim());
        comment.setTask(task);
        comment.setAuthor(user);
        comment.setCreatedAt(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);

        return toResponse(savedComment);
    }

    public List<CommentResponse> getComments(
            Long taskId,
            Authentication authentication
    ) {
        User user = getAuthenticatedUser(authentication);

        // Also verifies that the task exists and belongs to
        // a project owned by the authenticated user.
        getTaskForCurrentUser(taskId, user);

        return commentRepository.findByTaskId(taskId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required"
            );
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found"
                ));
    }

    private Task getTaskForCurrentUser(Long taskId, User user) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Task not found"
                ));

        Long projectOwnerId = task.getProject().getCreatedBy().getId();

        if (!projectOwnerId.equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have access to this task"
            );
        }

        return task;
    }

    private CommentResponse toResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getTask().getId(),
                comment.getContent(),
                comment.getAuthor().getId(),
                comment.getCreatedAt()
        );
    }
}