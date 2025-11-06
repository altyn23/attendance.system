package com.altynai.attendance.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    // если хочешь — пусть index просто указывает на login
    @GetMapping("/index")
    public String index() {
        return "index";   // или "login"
    }

    // ВАЖНО:
    // НИКАКИХ @GetMapping("/dashboard") здесь быть не должно
}
