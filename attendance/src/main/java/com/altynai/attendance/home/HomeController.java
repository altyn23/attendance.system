package com.altynai.attendance.home;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.altynai.attendance.news.NewsRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    private final NewsRepository newsRepository;

    public HomeController(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        model.addAttribute("userId", userId);
        model.addAttribute("email", session.getAttribute("email"));
        model.addAttribute("fullName", session.getAttribute("fullName"));
        model.addAttribute("role", session.getAttribute("role"));

        model.addAttribute("newsList", newsRepository.findTop10ByOrderByCreatedAtDesc());

        return "dashboard";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }
}
