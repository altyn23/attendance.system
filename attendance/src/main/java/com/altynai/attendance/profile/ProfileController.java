package com.altynai.attendance.profile;

import com.altynai.attendance.account.User;
import com.altynai.attendance.account.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        User user = userOpt.get();
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/api/profile/update")
    @ResponseBody
    public Map<String, Object> updateProfile(@RequestBody Map<String, String> req, HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            res.put("error", "Авторизацияны қайта өтіңіз");
            return res;
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            res.put("error", "Пайдаланушы табылмады");
            return res;
        }

        User user = userOpt.get();
        
        if (req.containsKey("firstName") && !req.get("firstName").isEmpty()) {
            user.setFirstName(req.get("firstName"));
        }
        if (req.containsKey("lastName") && !req.get("lastName").isEmpty()) {
            user.setLastName(req.get("lastName"));
        }
        if (req.containsKey("phone")) {
            user.setPhone(req.get("phone"));
        }
        if (req.containsKey("department")) {
            user.setDepartment(req.get("department"));
        }
        if (req.containsKey("group")) {
            user.setGroup(req.get("group"));
        }
        
        userRepository.save(user);
        
        session.setAttribute("fullName", user.getFullName());
        
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("email", user.getEmail());
        userMap.put("firstName", user.getFirstName());
        userMap.put("lastName", user.getLastName());
        userMap.put("phone", user.getPhone() != null ? user.getPhone() : "");
        userMap.put("department", user.getDepartment() != null ? user.getDepartment() : "");
        userMap.put("group", user.getGroup() != null ? user.getGroup() : "");
        userMap.put("role", user.getRole());
        userMap.put("fullName", user.getFullName());
        userMap.put("profileImage", user.getProfileImage() != null ? user.getProfileImage() : "");
        
        res.put("success", true);
        res.put("message", "Профиль сәтті жаңартылды");
        res.put("user", userMap);
        
        return res;
    }

    @GetMapping("/api/profile/{userId}")
    @ResponseBody
    public Map<String, Object> getProfile(@PathVariable String userId, HttpSession session) {
        Map<String, Object> res = new HashMap<>();

        String sessionUserId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (sessionUserId == null) {
            res.put("error", "Unauthorized");
            return res;
        }
        if (!"ADMIN".equals(role) && !sessionUserId.equals(userId)) {
            res.put("error", "Forbidden");
            return res;
        }
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            res.put("error", "Пайдаланушы табылмады");
            return res;
        }

        User user = userOpt.get();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("email", user.getEmail());
        userMap.put("firstName", user.getFirstName());
        userMap.put("lastName", user.getLastName());
        userMap.put("phone", user.getPhone() != null ? user.getPhone() : "");
        userMap.put("department", user.getDepartment() != null ? user.getDepartment() : "");
        userMap.put("group", user.getGroup() != null ? user.getGroup() : "");
        userMap.put("role", user.getRole());
        userMap.put("fullName", user.getFullName());
        userMap.put("registrationDate", user.getRegistrationDate() != null ? user.getRegistrationDate().toString() : "");
        userMap.put("lastLoginDate", user.getLastLoginDate() != null ? user.getLastLoginDate().toString() : "");
        userMap.put("profileImage", user.getProfileImage() != null ? user.getProfileImage() : "");
        
        res.put("success", true);
        res.put("user", userMap);
        
        return res;
    }

    @PostMapping("/api/profile/avatar")
    @ResponseBody
    public Map<String, Object> uploadAvatar(@RequestParam("file") MultipartFile file, HttpSession session) {
        Map<String, Object> res = new HashMap<>();

        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            res.put("error", "Авторизацияны қайта өтіңіз");
            return res;
        }

        if (file == null || file.isEmpty()) {
            res.put("error", "Файл таңдалмаған");
            return res;
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            res.put("error", "Тек сурет файлын жүктеуге болады");
            return res;
        }

        long maxBytes = 2 * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            res.put("error", "Сурет көлемі 2MB-тан аспауы керек");
            return res;
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            res.put("error", "Пайдаланушы табылмады");
            return res;
        }

        try {
            String base64 = Base64.getEncoder().encodeToString(file.getBytes());
            String dataUrl = "data:" + contentType + ";base64," + base64;

            User user = userOpt.get();
            user.setProfileImage(dataUrl);
            userRepository.save(user);

            res.put("success", true);
            res.put("message", "Аватар сәтті сақталды");
            res.put("profileImage", dataUrl);
            return res;
        } catch (Exception e) {
            res.put("error", "Аватарды сақтау мүмкін болмады");
            return res;
        }
    }

    @DeleteMapping("/api/profile/avatar")
    @ResponseBody
    public Map<String, Object> deleteAvatar(HttpSession session) {
        Map<String, Object> res = new HashMap<>();

        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            res.put("error", "Авторизацияны қайта өтіңіз");
            return res;
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            res.put("error", "Пайдаланушы табылмады");
            return res;
        }

        User user = userOpt.get();
        user.setProfileImage(null);
        userRepository.save(user);

        res.put("success", true);
        res.put("message", "Аватар өшірілді");
        return res;
    }
}
