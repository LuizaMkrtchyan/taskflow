package com.taskflow.backend.service;

import com.taskflow.backend.dto.LoginRequest;
import com.taskflow.backend.dto.LoginResponse;
import com.taskflow.backend.dto.RegisterRequest;
import com.taskflow.backend.repository.UserRepository;
import jdk.jshell.spi.ExecutionControl;
import com.taskflow.backend.dto.UserResponse;
import com.taskflow.backend.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.taskflow.backend.security.JwtService;
import com.taskflow.backend.dto.LoginRequest;
import com.taskflow.backend.dto.LoginResponse;
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new RuntimeException("Invalid email or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }
        String token = jwtService.generateToken(user.getEmail());
        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                token
        );
    }
}
