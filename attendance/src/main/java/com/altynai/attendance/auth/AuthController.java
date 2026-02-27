package com.altynai.attendance.auth;

import com.altynai.attendance.account.User;
import com.altynai.attendance.account.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> req, HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        String actorRole = (String) session.getAttribute("role");
        if (!"ADMIN".equals(actorRole)) {
            res.put("error", "Тіркелу жабық. Қолданушыны тек әкімші қоса алады");
            return res;
        }
        
        String email = req.get("email");
        String password = req.get("password");
        String firstName = req.get("firstName");
        String lastName = req.get("lastName");
        String phone = req.get("phone");
        String department = req.get("department");
        String group = req.get("group");
        String role = req.get("role");

        if (email == null || password == null || firstName == null || lastName == null ||
                email.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            res.put("error", "Барлық міндетті өрістерді толтырыңыз");
            return res;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            res.put("error", "Қате email форматы");
            return res;
        }

        if (userRepository.findByEmail(email).isPresent()) {
            res.put("error", "Бұл email тіркелген");
            return res;
        }

        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        user.setPasswordHash(encoder.encode(password));
        user.setRole(resolveRole(role));
        user.setDepartment(department);
        user.setGroup(group);
        user.setRegistrationDate(java.time.LocalDateTime.now());

        user = userRepository.save(user);
        
        res.put("success", true);
        res.put("message", "Пайдаланушы сәтті қосылды");
        res.put("userId", user.getId());
        return res;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> req, HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        String email = req.get("email");
        String password = req.get("password");

        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            res.put("error", "Email және құпиясөзді енгізіңіз");
            return res;
        }

        var optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            res.put("error", "Қате email немесе құпиясөз");
            return res;
        }

        User user = optionalUser.get();
        
        if (!encoder.matches(password, user.getPasswordHash())) {
            res.put("error", "Қате email немесе құпиясөз");
            return res;
        }

        user.setLastLoginDate(java.time.LocalDateTime.now());
        userRepository.save(user);

        session.setAttribute("userId", user.getId());
        session.setAttribute("email", user.getEmail());
        session.setAttribute("fullName", user.getFullName());
        session.setAttribute("role", user.getRole());
        session.setAttribute("profileImage", user.getProfileImage());

        res.put("success", true);
        res.put("message", "Кіру сәтті өтті");
        res.put("user", Map.of(
            "id", user.getId(),
            "email", user.getEmail(),
            "firstName", user.getFirstName(),
            "lastName", user.getLastName(),
            "role", user.getRole(),
            "fullName", user.getFullName(),
            "profileImage", user.getProfileImage() != null ? user.getProfileImage() : ""
        ));
        return res;
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpSession session) {
        session.invalidate();
        Map<String, String> res = new HashMap<>();
        res.put("message", "Сәтті шықтыңыз");
        return res;
    }

    @GetMapping("/users")
    public Object allUsers(HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Map.of("error", "Unauthorized");
        }
        return userRepository.findAll();
    }

    @PutMapping("/role/{id}")
    public Map<String, String> changeRole(@PathVariable String id, @RequestBody Map<String, String> req, HttpSession session) {
        Map<String, String> res = new HashMap<>();
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            res.put("error", "Unauthorized");
            return res;
        }
        String newRole = req.get("role");

        userRepository.findById(id).ifPresentOrElse(user -> {
            user.setRole(newRole);
            userRepository.save(user);
            res.put("message", "Рөл жаңартылды: " + newRole);
        }, () -> res.put("error", "Қолданушы табылмады"));

        return res;
    }

    private String resolveRole(String role) {
        if ("ADMIN".equals(role) || "TEACHER".equals(role) || "STUDENT".equals(role)) {
            return role;
        }
        return "STUDENT";
    }
}
