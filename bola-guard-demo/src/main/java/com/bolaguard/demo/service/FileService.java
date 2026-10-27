package com.bolaguard.demo.service;

import com.bolaguard.demo.dto.ShareRequest;
import com.bolaguard.demo.entity.FileResource;
import com.bolaguard.demo.entity.FileShare;
import com.bolaguard.demo.repository.FileResourceRepository;
import com.bolaguard.demo.repository.FileShareRepository;
import com.bolaguard.demo.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
public class FileService {

    private final FileResourceRepository fileResourceRepository;
    private final FileShareRepository fileShareRepository;

    public FileService(FileResourceRepository fileResourceRepository, FileShareRepository fileShareRepository) {
        this.fileResourceRepository = fileResourceRepository;
        this.fileShareRepository = fileShareRepository;
    }

    public FileResource getFile(Long id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }
        return fileResourceRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));
    }

    public void deleteFile(Long id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }
        Long safeId = Objects.requireNonNull(id);
        if (fileResourceRepository.existsById(safeId)) {
            fileResourceRepository.deleteById(safeId);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }
    }

    public FileResource updateFile(Long id, FileResource fileResource) {
        FileResource existing = getFile(id);
        existing.setContent(fileResource.getContent());
        existing.setName(fileResource.getName());
        return fileResourceRepository.save(existing);
    }

    public void shareFile(Long id, ShareRequest request) {
        getFile(id);
        
        FileShare share = new FileShare(id, request.getUserId(), request.getPermission());
        fileShareRepository.save(share);
    }
    
    public FileResource createFile(FileResource fileResource) {
        CustomUserDetails user = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        fileResource.setOwnerId(user.getUserId());
        return fileResourceRepository.save(fileResource);
    }

    public List<FileResource> getMyFiles() {
        CustomUserDetails user = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return fileResourceRepository.findByOwnerId(user.getUserId());
    }
}
