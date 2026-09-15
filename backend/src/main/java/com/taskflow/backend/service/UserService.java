package com.taskflow.backend.service;

import com.taskflow.backend.dto.RegisterRequest;
import com.taskflow.backend.repository.UserRepository;
import jdk.jshell.spi.ExecutionControl;
import com.taskflow.backend.dto.UserResponse;
import com.taskflow.backend.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    public UserResponse register(RegisterRequest request) {
        if(userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new RuntimeException("Email is already registered");
        }
        User user =new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.Member);

        User savedUser=userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole().name()
        );

    }
}
