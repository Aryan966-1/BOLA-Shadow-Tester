package com.bolaguard.demo.controller;

import com.bolaguard.demo.dto.ShareRequest;
import com.bolaguard.demo.entity.FileResource;
import com.bolaguard.demo.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@Profile("demo-vulnerable")
@Tag(name = "File operations")
@SecurityRequirement(name = "bearerAuth")
public class VulnerableFileController {
    private final FileService fileService;
    public VulnerableFileController(FileService fileService) { this.fileService = fileService; }

    @GetMapping("/{fileId}")
    @Operation(summary = "Read a file — intentionally vulnerable", description = "In vulnerable mode, Charlie can read Alice's file 101. A 200 response demonstrates the BOLA flaw.")
    public ResponseEntity<FileResource> getFile(@PathVariable("fileId") Long fileId) {
        return ResponseEntity.ok(fileService.getFile(fileId));
    }

    @DeleteMapping("/{fileId}")
    @Operation(summary = "Delete a file — intentionally vulnerable", description = "No object ownership check is performed. Use only disposable demo data.")
    public ResponseEntity<Void> deleteFile(@PathVariable("fileId") Long fileId) {
        fileService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{fileId}")
    @Operation(summary = "Update a file — intentionally vulnerable", description = "No object ownership check is performed.")
    public ResponseEntity<FileResource> updateFile(@PathVariable("fileId") Long fileId, @RequestBody FileResource fileResource) {
        return ResponseEntity.ok(fileService.updateFile(fileId, fileResource));
    }

    @PostMapping("/{fileId}/share")
    @Operation(summary = "Share a file — intentionally vulnerable", description = "No object ownership check is performed.")
    public ResponseEntity<Void> shareFile(@PathVariable("fileId") Long fileId, @RequestBody ShareRequest request) {
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
