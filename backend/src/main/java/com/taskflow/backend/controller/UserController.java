package com.taskflow.backend.controller;

import com.taskflow.backend.dto.RegisterRequest;
import com.taskflow.backend.dto.UserResponse;
import com.taskflow.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.taskflow.backend.dto.LoginRequest;
import com.taskflow.backend.dto.LoginResponse;


@RestController
@RequestMapping("api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(
            @Valid @RequestBody RegisterRequest request
    ){
        return userService.register(request);
    }
    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request
    ){
        return  userService.login(request);
    }

}
