package com.altynai.attendance.config;

import com.altynai.attendance.account.User;
import com.altynai.attendance.account.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class UserBootstrapConfig {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final boolean enabled;

    public UserBootstrapConfig(
            UserRepository userRepository,
            @Value("${app.bootstrap-users.enabled:true}") boolean enabled
    ) {
        this.userRepository = userRepository;
        this.enabled = enabled;
    }

    @PostConstruct
    public void initUsers() {
        if (!enabled) {
            return;
        }
        ensureAdmin();
        ensureTeachers();
    }

    private void ensureAdmin() {
        boolean hasAdmin = userRepository.findAll().stream().anyMatch(u -> "ADMIN".equals(u.getRole()));
        if (hasAdmin) {
            return;
        }

        User admin = new User();
        admin.setEmail("admin@attendance.local");
        admin.setFirstName("System");
        admin.setLastName("Admin");
        admin.setRole("ADMIN");
        admin.setPasswordHash(encoder.encode("Admin123!"));
        admin.setRegistrationDate(LocalDateTime.now());
        userRepository.save(admin);
    }

    private void ensureTeachers() {
        boolean hasTeacher = userRepository.findAll().stream().anyMatch(u -> "TEACHER".equals(u.getRole()));
        if (hasTeacher) {
            return;
        }

        List<User> teachers = List.of(
                buildTeacher("teacher1@attendance.local", "Aidar", "Muratov"),
                buildTeacher("teacher2@attendance.local", "Ainur", "Sarsembayeva")
        );
        userRepository.saveAll(teachers);
    }

    private User buildTeacher(String email, String firstName, String lastName) {
        User teacher = new User();
        teacher.setEmail(email);
        teacher.setFirstName(firstName);
        teacher.setLastName(lastName);
        teacher.setRole("TEACHER");
        teacher.setPasswordHash(encoder.encode("Teacher123!"));
        teacher.setRegistrationDate(LocalDateTime.now());
        return teacher;
    }
}
