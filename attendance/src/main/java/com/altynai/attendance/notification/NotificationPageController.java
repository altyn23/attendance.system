package com.altynai.attendance.notification;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NotificationPageController {

    @GetMapping("/notifications")
    public String notificationsPage(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        model.addAttribute("userId", userId);
        model.addAttribute("fullName", session.getAttribute("fullName"));
        model.addAttribute("role", session.getAttribute("role"));
        return "notifications";
    }
}
