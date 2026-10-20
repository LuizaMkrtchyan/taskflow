package com.taskflow.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="comments")
public class Comment {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="task_id",nullable=false)
    private Task task;

    @ManyToOne
    @JoinColumn(name="user_id",nullable=false)
    private User author;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Comment(){

    }
    public Long getId()
    {
        return id;
    }
    public void setId(Long id){
        this.id = id;
    }
    public Task getTask()
    {
        return task;
    }
    public void setTask(Task task)
    {
        this.task = task;
    }
    public User getAuthor()
    {
        return author;
    }
    public void setAuthor(User author)
    {
        this.author = author;
    }
    public String getContent()
    {
        return content;
    }
    public void setContent(String content)
    {
        this.content = content;
    }
    public LocalDateTime getCreatedAt()
    {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt)
    {
        this.createdAt = createdAt;
    }
}
