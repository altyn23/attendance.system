package com.altynai.attendance.profile;

import com.altynai.attendance.account.User;
import com.altynai.attendance.account.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

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
        
        res.put("success", true);
        res.put("message", "Профиль сәтті жаңартылды");
        res.put("user", userMap);
        
        return res;
    }

    @GetMapping("/api/profile/{userId}")
    @ResponseBody
    public Map<String, Object> getProfile(@PathVariable String userId) {
        Map<String, Object> res = new HashMap<>();
        
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
        
        res.put("success", true);
        res.put("user", userMap);
        
        return res;
    }
}
