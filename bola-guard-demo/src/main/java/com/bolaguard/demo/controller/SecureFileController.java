package com.bolaguard.demo.controller;

import com.bolaguard.demo.dto.ShareRequest;
import com.bolaguard.demo.entity.FileResource;
import com.bolaguard.demo.service.FileService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@Profile("demo-secure")
public class SecureFileController {

    private final FileService fileService;

    public SecureFileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/{fileId}")
    @PreAuthorize("hasPermission(#fileId, 'FileResource', 'READ')")
    public ResponseEntity<FileResource> getFile(@P("fileId") @PathVariable("fileId") Long fileId) {
        return ResponseEntity.ok(fileService.getFile(fileId));
    }

    @DeleteMapping("/{fileId}")
    @PreAuthorize("hasPermission(#fileId, 'FileResource', 'DELETE')")
    public ResponseEntity<Void> deleteFile(@P("fileId") @PathVariable("fileId") Long fileId) {
        fileService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{fileId}")
    @PreAuthorize("hasPermission(#fileId, 'FileResource', 'UPDATE')")
    public ResponseEntity<FileResource> updateFile(@P("fileId") @PathVariable("fileId") Long fileId, @RequestBody FileResource fileResource) {
        return ResponseEntity.ok(fileService.updateFile(fileId, fileResource));
    }

    @PostMapping("/{fileId}/share")
    @PreAuthorize("hasPermission(#fileId, 'FileResource', 'SHARE')")
    public ResponseEntity<Void> shareFile(@P("fileId") @PathVariable("fileId") Long fileId, @RequestBody ShareRequest request) {
        fileService.shareFile(fileId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<FileResource> createFile(@RequestBody FileResource fileResource) {
        return ResponseEntity.ok(fileService.createFile(fileResource));
    }

    @GetMapping
    public ResponseEntity<List<FileResource>> getMyFiles() {
        return ResponseEntity.ok(fileService.getMyFiles());
    }
}
