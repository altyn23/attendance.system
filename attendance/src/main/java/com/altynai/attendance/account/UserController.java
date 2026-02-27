package com.altynai.attendance.account;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private boolean isAdmin(HttpSession session) {
        return "ADMIN".equals(session.getAttribute("role"));
    }

    @GetMapping
    public Object getAll(@RequestParam(required = false) String q, HttpSession session) {
        if (!isAdmin(session)) {
            return Map.of("error", "Unauthorized");
        }

        List<User> all = userRepository.findAll();
        if (q != null && !q.isEmpty()) {
            String query = q.toLowerCase();
            all = all.stream()
                    .filter(u -> u.getFullName().toLowerCase().contains(query) ||
                                 u.getEmail().toLowerCase().contains(query))
                    .toList();
        }
        return all;
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody User reqUser, HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        if (!isAdmin(session)) {
            res.put("error", "Unauthorized");
            return res;
        }

        if (reqUser.getEmail() == null || reqUser.getEmail().isBlank()
                || reqUser.getFirstName() == null || reqUser.getFirstName().isBlank()
                || reqUser.getLastName() == null || reqUser.getLastName().isBlank()
                || reqUser.getPasswordHash() == null || reqUser.getPasswordHash().isBlank()) {
            res.put("error", "Барлық міндетті өрістерді толтырыңыз");
            return res;
        }

        if (userRepository.findByEmail(reqUser.getEmail()).isPresent()) {
            res.put("error", "Бұл email тіркелген");
            return res;
        }

        User user = new User();
        user.setFirstName(reqUser.getFirstName());
        user.setLastName(reqUser.getLastName());
        user.setEmail(reqUser.getEmail());
        user.setPhone(reqUser.getPhone());
        user.setRole(resolveRole(reqUser.getRole()));
        user.setGroup(reqUser.getGroup());
        user.setDepartment(reqUser.getDepartment());
        user.setPasswordHash(encoder.encode(reqUser.getPasswordHash()));
        user.setRegistrationDate(java.time.LocalDateTime.now());

        user = userRepository.save(user);
        res.put("success", true);
        res.put("user", user);
        return res;
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable String id, @RequestBody User newUser, HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        if (!isAdmin(session)) {
            res.put("error", "Unauthorized");
            return res;
        }

        Optional<User> updated = userRepository.findById(id).map(u -> {
            if (newUser.getEmail() != null && !newUser.getEmail().isBlank()
                    && !newUser.getEmail().equalsIgnoreCase(u.getEmail())
                    && userRepository.findByEmail(newUser.getEmail()).isPresent()) {
                return null;
            }

            u.setFirstName(newUser.getFirstName());
            u.setLastName(newUser.getLastName());
            u.setEmail(newUser.getEmail());
            u.setPhone(newUser.getPhone());
            u.setRole(resolveRole(newUser.getRole()));
            u.setGroup(newUser.getGroup());
            u.setDepartment(newUser.getDepartment());
            if (newUser.getPasswordHash() != null && !newUser.getPasswordHash().isBlank()) {
                u.setPasswordHash(encoder.encode(newUser.getPasswordHash()));
            }
            return userRepository.save(u);
        }).filter(Objects::nonNull);
        if (updated.isEmpty()) {
            res.put("error", "Қолданушы табылмады немесе email қолданыста");
            return res;
        }

        res.put("success", true);
        res.put("user", updated.get());
        return res;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable String id, HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        if (!isAdmin(session)) {
            res.put("error", "Unauthorized");
            return res;
        }
        if (!userRepository.existsById(id)) {
            res.put("error", "Қолданушы табылмады");
            return res;
        }

        userRepository.deleteById(id);
        res.put("success", true);
        return res;
    }

    @PostMapping("/reset")
    public Map<String, String> reset(@RequestBody Map<String, String> req) {
        Map<String, String> res = new HashMap<>();
        String email = req.get("email");
        if (email == null || email.isBlank()) {
            res.put("error", "Email енгізіңіз");
            return res;
        }

        userRepository.findByEmail(email).ifPresent(user -> {
            String code = String.valueOf(new Random().nextInt(100000, 999999));
            user.setResetCode(code);
            user.setResetCodeExpires(System.currentTimeMillis() + 10 * 60 * 1000);
            userRepository.save(user);
        });

        // Avoid account enumeration and leaking reset code in API responses.
        res.put("message", "Егер email бар болса, қалпына келтіру коды жіберілді");
        return res;
    }

    private String resolveRole(String role) {
        if ("ADMIN".equals(role) || "TEACHER".equals(role) || "STUDENT".equals(role)) {
            return role;
        }
        return "STUDENT";
    }
}
