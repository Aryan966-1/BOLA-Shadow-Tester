package com.bolaguard.demo.repository;

import com.bolaguard.demo.entity.FileResource;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FileResourceRepository extends JpaRepository<FileResource, Long> {
    List<FileResource> findByOwnerId(Long ownerId);
}
