package com.taskflow.backend.controller;

import com.taskflow.backend.dto.CommentResponse;
import com.taskflow.backend.dto.CreateCommentRequest;
import com.taskflow.backend.service.CommentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse addComment(
            @PathVariable Long taskId,
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication
    ) {
        return commentService.addComment(
                taskId,
                request,
                authentication
        );
    }

    @GetMapping
    public List<CommentResponse> getComments(
            @PathVariable Long taskId,
            Authentication authentication
    ) {
        return commentService.getComments(taskId, authentication);
    }
}