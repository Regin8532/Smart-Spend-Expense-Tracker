package com.example.ExpenseTracker.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class AppNotification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false)
    private User user;

    @Column(nullable=false, length=500)
    private String message;

    private boolean readFlag = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public boolean isReadFlag() {
        return readFlag;
    }
    public void setReadFlag(boolean readFlag) {
        this.readFlag = readFlag;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
