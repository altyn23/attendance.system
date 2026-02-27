package com.altynai.attendance.news;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/news")
public class NewsRestController {

    private final NewsRepository repo;

    public NewsRestController(NewsRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<News> all() {
        return repo.findTop10ByOrderByCreatedAtDesc();
    }

    @PostMapping
    public Object create(@RequestBody News n, HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role) && !"TEACHER".equals(role)) {
            return java.util.Map.of("error", "Unauthorized");
        }
        return repo.save(n);
    }
    
    @DeleteMapping("/{id}")
    public Object delete(@PathVariable String id, HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role) && !"TEACHER".equals(role)) {
            return java.util.Map.of("error", "Unauthorized");
        }
        repo.deleteById(id);
        return java.util.Map.of("success", true);
    }
}

@Controller
class NewsPageController {
    
    @GetMapping("/news")
    public String newsPage(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("userId", userId);
        model.addAttribute("fullName", session.getAttribute("fullName"));
        model.addAttribute("role", session.getAttribute("role"));
        
        return "news";
    }
}
