package com.altynai.attendance.ai;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/ai")
public class AiInsightsController {
    private final AiInsightsService aiInsightsService;

    public AiInsightsController(AiInsightsService aiInsightsService) {
        this.aiInsightsService = aiInsightsService;
    }

    @GetMapping("/smart-alerts")
    public Map<String, Object> smartAlerts(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        List<Map<String, Object>> alerts = aiInsightsService.smartAlerts(userId, role);
        return Map.of(
                "success", userId != null,
                "items", alerts,
                "generatedAt", LocalDateTime.now()
        );
    }

    @GetMapping("/risk-predictions")
    public Map<String, Object> riskPredictions(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        List<Map<String, Object>> items = aiInsightsService.riskPredictions(userId, role);
        return Map.of(
                "success", userId != null,
                "items", items,
                "generatedAt", LocalDateTime.now()
        );
    }

    @GetMapping("/clusters")
    public Map<String, Object> clusters(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        List<Map<String, Object>> items = aiInsightsService.reasonClusters(userId, role);
        return Map.of(
                "success", userId != null,
                "items", items,
                "generatedAt", LocalDateTime.now()
        );
    }

    @GetMapping("/admin-summary")
    public Map<String, Object> adminSummary(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        return aiInsightsService.adminSummary(userId, role);
    }

    @PostMapping("/message-draft")
    public Map<String, Object> messageDraft(@RequestBody(required = false) Map<String, String> req, HttpSession session) {
        Map<String, String> body = req != null ? req : Map.of();
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        String studentId = body.get("studentId");
        String type = body.getOrDefault("type", "GENERAL");
        String lang = body.getOrDefault("lang", "kz");
        String tone = body.getOrDefault("tone", "supportive");

        return aiInsightsService.teacherMessageDraft(userId, role, studentId, type, lang, tone);
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> req, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        String query = req != null ? req.get("query") : null;
        return aiInsightsService.chat(userId, role, query);
    }

    @GetMapping("/qr-anomalies")
    public Map<String, Object> qrAnomalies(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        List<Map<String, Object>> items = aiInsightsService.qrAnomalies(userId, role);
        return Map.of(
                "success", userId != null,
                "items", items,
                "generatedAt", LocalDateTime.now()
        );
    }
}
