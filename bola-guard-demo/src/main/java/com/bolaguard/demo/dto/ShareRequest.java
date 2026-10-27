package com.bolaguard.demo.dto;

import com.bolaguard.demo.entity.FileShare;

public class ShareRequest {
    private Long userId;
    private FileShare.Permission permission;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public FileShare.Permission getPermission() { return permission; }
    public void setPermission(FileShare.Permission permission) { this.permission = permission; }
}
