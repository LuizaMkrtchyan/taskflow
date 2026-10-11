package com.taskflow.backend.dto;

public class LoginResponse {
    private Long id;
    private String name;
    private String role;
    private String email;
    private String token;

    public  LoginResponse() {
    }
    public LoginResponse(Long id, String name, String role, String email,String token) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.email = email;
        this.token=token;
    }
    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getRole() {
        return role;
    }
    public String getEmail() {
        return email;
    }
    public String getToken() {
        return token;
    }
}
