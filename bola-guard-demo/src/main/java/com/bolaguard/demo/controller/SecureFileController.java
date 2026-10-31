package com.bolaguard.demo.controller;

import com.bolaguard.demo.dto.ShareRequest;
import com.bolaguard.demo.entity.FileResource;
import com.bolaguard.demo.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@Profile("demo-secure")
@Tag(name = "File operations")
@SecurityRequirement(name = "bearerAuth")
public class SecureFileController {
    private final FileService fileService;
    public SecureFileController(FileService fileService) { this.fileService = fileService; }

    @GetMapping("/{fileId}")
    @Operation(summary = "Read a file", description = "Test file ID 101: Alice (owner) and Bob (viewer) should get 200; Charlie should get 403.")
    @PreAuthorize("hasPermission(#fileId, 'FileResource', 'READ')")
    public ResponseEntity<FileResource> getFile(@P("fileId") @PathVariable("fileId") Long fileId) {
        return ResponseEntity.ok(fileService.getFile(fileId));
    }

    @DeleteMapping("/{fileId}")
    @Operation(summary = "Delete a file", description = "Only the owner should be allowed to delete file 101.")
    @PreAuthorize("hasPermission(#fileId, 'FileResource', 'DELETE')")
    public ResponseEntity<Void> deleteFile(@P("fileId") @PathVariable("fileId") Long fileId) {
        fileService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{fileId}")
    @Operation(summary = "Update a file", description = "Owner and EDITOR can update. Bob is only a VIEWER of file 101 and should be denied.")
    @PreAuthorize("hasPermission(#fileId, 'FileResource', 'UPDATE')")
    public ResponseEntity<FileResource> updateFile(@P("fileId") @PathVariable("fileId") Long fileId, @RequestBody FileResource fileResource) {
        return ResponseEntity.ok(fileService.updateFile(fileId, fileResource));
    }

    @PostMapping("/{fileId}/share")
    @Operation(summary = "Share a file", description = "Only the owner should be allowed to share file 101.")
    @PreAuthorize("hasPermission(#fileId, 'FileResource', 'SHARE')")
    public ResponseEntity<Void> shareFile(@P("fileId") @PathVariable("fileId") Long fileId, @RequestBody ShareRequest request) {
        fileService.shareFile(fileId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    @Operation(summary = "Create a file", description = "Create a file for the authenticated demo user.")
    public ResponseEntity<FileResource> createFile(@RequestBody FileResource fileResource) {
        return ResponseEntity.ok(fileService.createFile(fileResource));
    }

    @GetMapping
    @Operation(summary = "List my files", description = "List files visible to the authenticated demo user.")
    public ResponseEntity<List<FileResource>> getMyFiles() {
        return ResponseEntity.ok(fileService.getMyFiles());
    }
}
