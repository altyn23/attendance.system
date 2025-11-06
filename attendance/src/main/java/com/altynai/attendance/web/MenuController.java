package com.altynai.attendance.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class MenuController {

    private boolean notLogged(HttpSession s) {
        return s.getAttribute("username") == null;
    }

    @GetMapping("/scan")
    public String scan(HttpSession session) {
        if (notLogged(session)) return "redirect:/login";
        return "scan";     // templates/scan.html
    }

    @GetMapping("/students")
    public String students(HttpSession session) {
        if (notLogged(session)) return "redirect:/login";
        return "students"; // templates/students.html
    }

    @GetMapping("/analytics")
    public String analytics(HttpSession session) {
        if (notLogged(session)) return "redirect:/login";
        return "analytics";
    }

    @GetMapping("/reports")
    public String reports(HttpSession session) {
        if (notLogged(session)) return "redirect:/login";
        return "reports";
    }
}
