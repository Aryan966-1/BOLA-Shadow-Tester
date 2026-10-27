package com.bolaguard.demo;

import com.bolaguard.demo.entity.FileResource;
import com.bolaguard.demo.entity.FileShare;
import com.bolaguard.demo.entity.User;
import com.bolaguard.demo.repository.FileResourceRepository;
import com.bolaguard.demo.repository.FileShareRepository;
import com.bolaguard.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class DataSeeder {

    private final UserRepository userRepository;
    private final FileResourceRepository fileResourceRepository;
    private final FileShareRepository fileShareRepository;

    public DataSeeder(UserRepository userRepository, 
                      FileResourceRepository fileResourceRepository,
                      FileShareRepository fileShareRepository) {
        this.userRepository = userRepository;
        this.fileResourceRepository = fileResourceRepository;
        this.fileShareRepository = fileShareRepository;
    }

    public synchronized void resetData() {
        fileShareRepository.deleteAll();
        fileResourceRepository.deleteAll();

        // Seed / ensure users
        User alice = userRepository.findByUsername("alice")
                .orElseGet(() -> userRepository.save(new User("alice", "alice@example.com", "USER")));
        User bob = userRepository.findByUsername("bob")
                .orElseGet(() -> userRepository.save(new User("bob", "bob@example.com", "USER")));
        userRepository.findByUsername("charlie")
                .orElseGet(() -> userRepository.save(new User("charlie", "charlie@example.com", "USER")));
        userRepository.findByUsername("admin")
                .orElseGet(() -> userRepository.save(new User("admin", "admin@example.com", "ADMIN")));

        // Seed Files
        FileResource file101 = new FileResource(101L, "Alice Confidential Report", "Top secret data", alice.getId());
        FileResource file102 = new FileResource(102L, "Bob Project", "Project data", bob.getId());
        
        fileResourceRepository.save(file101);
        fileResourceRepository.save(file102);

        // Seed Permissions: Bob -> File 101 -> VIEWER
        fileShareRepository.save(new FileShare(101L, bob.getId(), FileShare.Permission.VIEWER));
    }

    @Bean
    public CommandLineRunner seedDatabase() {
        return args -> resetData();
    }
}
