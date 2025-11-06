package com.altynai.attendance.controller;

import com.altynai.attendance.model.Class;
import com.altynai.attendance.repository.ClassRepository;
import com.altynai.attendance.account.User;
import com.altynai.attendance.account.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.time.DayOfWeek;
import java.util.*;

@Controller
public class ScheduleController {

    @Autowired
    private ClassRepository classRepository;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/schedule")
    public String schedulePage(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }
        
        List<Class> classes;
        String role = (String) session.getAttribute("role");
        
        if ("TEACHER".equals(role)) {
            classes = classRepository.findByTeacherId(userId);
        } else if ("STUDENT".equals(role)) {
            String userGroup = user.getGroup();
            if (userGroup != null && !userGroup.isEmpty()) {
                classes = classRepository.findByGroup(userGroup);
            } else {
                classes = classRepository.findAll();
            }
        } else {
            classes = classRepository.findAll();
        }
        
        model.addAttribute("classes", classes);
        model.addAttribute("userId", userId);
        model.addAttribute("fullName", session.getAttribute("fullName"));
        model.addAttribute("role", role);
        
        return "schedule";
    }
}
