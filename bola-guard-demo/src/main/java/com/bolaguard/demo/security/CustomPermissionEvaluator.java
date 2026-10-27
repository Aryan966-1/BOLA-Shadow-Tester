package com.bolaguard.demo.security;

import com.bolaguard.demo.entity.FileResource;
import com.bolaguard.demo.entity.FileShare;
import com.bolaguard.demo.repository.FileResourceRepository;
import com.bolaguard.demo.repository.FileShareRepository;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final FileResourceRepository fileResourceRepository;
    private final FileShareRepository fileShareRepository;

    public CustomPermissionEvaluator(FileResourceRepository fileResourceRepository, FileShareRepository fileShareRepository) {
        this.fileResourceRepository = fileResourceRepository;
        this.fileShareRepository = fileShareRepository;
    }

    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
        if (auth == null || targetDomainObject == null || !(permission instanceof String)) {
            return false;
        }
        // Simplified for MVP, we rely on targetId
        return false;
    }

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
        if (auth == null || targetId == null || targetType == null || !(permission instanceof String perm)) {
            return false;
        }

        if (!(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            return false;
        }
        Long userId = userDetails.getUserId();
        Long fileId = toLong(targetId);
        if (userId == null || fileId == null) {
            return false;
        }

        if ("FileResource".equals(targetType)) {
            return evaluateFilePermission(userId, fileId, perm);
        }
        
        return false;
    }
    
    private boolean evaluateFilePermission(Long userId, Long fileId, String permission) {
        if (userId == null || fileId == null) {
            return false;
        }
        Optional<FileResource> fileOpt = fileResourceRepository.findById(Objects.requireNonNull(fileId));
        if (fileOpt.isEmpty()) {
            return false; // If not found, deny or let controller handle 404 (for security, 403 or 404 is fine. BOLA is about unauthorized access to *existing* resources)
        }
        
        FileResource file = fileOpt.get();
        if (file.getOwnerId() != null && file.getOwnerId().equals(userId)) {
            return true; // Owner can do anything (READ, UPDATE, DELETE, SHARE)
        }
        
        // Check shares
        Optional<FileShare> shareOpt = fileShareRepository.findByFileIdAndUserId(fileId, userId);
        if (shareOpt.isEmpty()) {
            return false;
        }
        
        FileShare share = shareOpt.get();
        
        return switch (permission) {
            case "READ" -> true; // Viewer or Editor can read
            case "UPDATE" -> share.getPermission() == FileShare.Permission.EDITOR;
            case "DELETE", "SHARE" -> false; // Only owner can delete or share
            default -> false;
        };
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.valueOf(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
