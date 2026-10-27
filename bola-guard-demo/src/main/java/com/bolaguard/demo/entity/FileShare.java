package com.bolaguard.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "file_shares")
public class FileShare {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "file_id")
    private Long fileId;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    private Permission permission;
    
    public enum Permission {
        VIEWER, EDITOR
    }
    
    // Constructors, Getters, Setters
    public FileShare() {}
    public FileShare(Long fileId, Long userId, Permission permission) {
        this.fileId = fileId;
        this.userId = userId;
        this.permission = permission;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Permission getPermission() { return permission; }
    public void setPermission(Permission permission) { this.permission = permission; }
}
