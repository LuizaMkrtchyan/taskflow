package com.taskflow.backend.entity;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name="tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="project_id",nullable = false)
    private Project project;

    @Column(nullable=false)
    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private Priority priority;

    @ManyToOne
    @JoinColumn(name="user_id",nullable=false)
    private User assignee;

    private LocalDate dueDate;

    public Task(){

    }
    public Long getId(){
        return id;
    }
    public void setId(Long id){
        this.id = id;
    }
    public Project getProject(){
        return project;
    }
    public void setProject(Project project){
        this.project = project;
    }
    public String getTitle(){
        return title;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public String getDescription(){
        return description;
    }
    public void setDescription(String description){
        this.description = description;
    }
    public Status getStatus(){
        return status;
    }
    public void setStatus(Status status){
        this.status = status;
    }
    public Priority getPriority(){
        return priority;
    }
    public void setPriority(Priority priority){
        this.priority = priority;
    }
    public User getAssignee(){
        return assignee;
    }
    public void setAssignee(User assignee){
        this.assignee = assignee;
    }
    public LocalDate getDueDate(){
        return dueDate;
    }
    public void setDueDate(LocalDate dueDate){
        this.dueDate = dueDate;
    }
    public enum Status{
        TODO,
        IN_PROGRESS,
        DONE
    }
    public enum Priority{
        LOW,
        MEDIUM,
        HIGH
    }
}
