package com.bolaguard.demo.repository;

import com.bolaguard.demo.entity.FileShare;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FileShareRepository extends JpaRepository<FileShare, Long> {
    List<FileShare> findByUserId(Long userId);
    Optional<FileShare> findByFileIdAndUserId(Long fileId, Long userId);
}
