package com.bolaguard.demo.controller;

import com.bolaguard.demo.dto.ShareRequest;
import com.bolaguard.demo.entity.FileResource;
import com.bolaguard.demo.service.FileService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@Profile("demo-vulnerable")
public class VulnerableFileController {

    private final FileService fileService;

    public VulnerableFileController(FileService fileService) {
        this.fileService = fileService;
    }

    // VULNERABILITY: No object-level authorization check.
    // Anyone authenticated can read any file if they know the ID.
    @GetMapping("/{fileId}")
    public ResponseEntity<FileResource> getFile(@PathVariable("fileId") Long fileId) {
        return ResponseEntity.ok(fileService.getFile(fileId));
    }

    // VULNERABILITY: No object-level authorization check.
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable("fileId") Long fileId) {
        fileService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }

    // VULNERABILITY: No object-level authorization check.
    @PutMapping("/{fileId}")
    public ResponseEntity<FileResource> updateFile(@PathVariable("fileId") Long fileId, @RequestBody FileResource fileResource) {
        return ResponseEntity.ok(fileService.updateFile(fileId, fileResource));
    }

    // VULNERABILITY: No object-level authorization check.
    @PostMapping("/{fileId}/share")
    public ResponseEntity<Void> shareFile(@PathVariable("fileId") Long fileId, @RequestBody ShareRequest request) {
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
