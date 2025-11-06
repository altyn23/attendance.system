package com.altynai.attendance.account;

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

    @PostMapping("/register")
    public Map<String, String> register(@RequestBody Map<String, String> req) {
        Map<String, String> res = new HashMap<>();
        String email = req.get("email");
        String firstName = req.get("firstName");
        String lastName = req.get("lastName");
        String password = req.get("password");

        if (email == null || password == null || firstName == null || lastName == null) {
            res.put("error", "Барлық өрістерді толтырыңыз");
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
        user.setPasswordHash(encoder.encode(password));
        userRepository.save(user);

        res.put("message", "Тіркелу сәтті өтті");
        return res;
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> req) {
        Map<String, String> res = new HashMap<>();
        String email = req.get("email");
        String password = req.get("password");

        Optional<User> opt = userRepository.findByEmail(email);
        if (opt.isEmpty()) {
            res.put("error", "Мұндай қолданушы жоқ");
            return res;
        }

        User user = opt.get();
        if (!encoder.matches(password, user.getPasswordHash())) {
            res.put("error", "Қате құпиясөз");
            return res;
        }

        res.put("message", "Кіру сәтті өтті");
        res.put("role", user.getRole());
        return res;
    }

    @GetMapping
    public List<User> getAll(@RequestParam(required = false) String q) {
        List<User> all = userRepository.findAll();
        if (q != null && !q.isEmpty()) {
            all = all.stream()
                    .filter(u -> u.getFullName().toLowerCase().contains(q.toLowerCase()) ||
                                 u.getEmail().toLowerCase().contains(q.toLowerCase()))
                    .toList();
        }
        return all;
    }

    @PutMapping("/{id}")
    public User update(@PathVariable String id, @RequestBody User newUser) {
        return userRepository.findById(id).map(u -> {
            u.setFirstName(newUser.getFirstName());
            u.setLastName(newUser.getLastName());
            u.setEmail(newUser.getEmail());
            if (newUser.getPasswordHash() != null)
                u.setPasswordHash(encoder.encode(newUser.getPasswordHash()));
            return userRepository.save(u);
        }).orElseThrow();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        userRepository.deleteById(id);
    }

    @PostMapping("/reset")
    public Map<String, String> reset(@RequestBody Map<String, String> req) {
        Map<String, String> res = new HashMap<>();
        String email = req.get("email");

        Optional<User> opt = userRepository.findByEmail(email);
        if (opt.isEmpty()) {
            res.put("error", "Қолданушы табылмады");
            return res;
        }

        User user = opt.get();
        String code = String.valueOf(new Random().nextInt(100000, 999999));
        user.setResetCode(code);
        user.setResetCodeExpires(System.currentTimeMillis() + 10 * 60 * 1000);
        userRepository.save(user);

        res.put("message", "Қалпына келтіру коды: " + code);
        return res;
    }
}
