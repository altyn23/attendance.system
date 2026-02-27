package com.altynai.attendance.ai;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AiInsightsPageController {

    @GetMapping("/ai-insights")
    public String aiInsightsPage(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if (userId == null) {
            return "redirect:/login";
        }

        if (!"ADMIN".equals(role) && !"TEACHER".equals(role)) {
            return "redirect:/dashboard";
        }

        model.addAttribute("userId", userId);
        model.addAttribute("fullName", session.getAttribute("fullName"));
        model.addAttribute("role", role);
        return "ai-insights";
    }
}
