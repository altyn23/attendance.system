package com.altynai.attendance.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class MenuController {

    private boolean notLogged(HttpSession s) {
        return s.getAttribute("userId") == null;
    }

    @GetMapping("/scan")
    public String scan(HttpSession session) {
        if (notLogged(session)) return "redirect:/login";
        return "redirect:/qr-scan";
    }

    @GetMapping("/students")
    public String students(HttpSession session) {
        if (notLogged(session)) return "redirect:/login";
        return "redirect:/student-list";
    }

    @GetMapping("/analytics")
    public String analytics(HttpSession session) {
        if (notLogged(session)) return "redirect:/login";
        return "redirect:/system-reports";
    }

    @GetMapping("/reports")
    public String reports(HttpSession session) {
        if (notLogged(session)) return "redirect:/login";
        return "redirect:/attendance-report";
    }

    @GetMapping("/alerts")
    public String alerts(HttpSession session) {
        if (notLogged(session)) return "redirect:/login";
        return "redirect:/notifications";
    }
}
